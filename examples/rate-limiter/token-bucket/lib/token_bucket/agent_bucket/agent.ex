defmodule TokenBucket.AgentBucket.Agent do
    use Agent

    defstruct [:max_tokens, :tokens]

    def start_link(opts) do
        max_tokens = Keyword.fetch!(opts, :max_tokens)
        refresh = Keyword.fetch!(opts, :refresh)

        with {:ok, pid} <- Agent.start_link(fn -> init(max_tokens) end) do
            with(
                {:ok, _} <- if(
                refresh == :never,
                do: {:ok, nil},
                else: configure_timer(refresh, pid)))
            do
                {:ok, pid}
            else
                error ->
                    Agent.stop(pid)
                    error
            end
        end
    end

    defp init(max_tokens) do
        %__MODULE__{max_tokens: max_tokens, tokens: max_tokens}
    end

    defp configure_timer(opts, pid) do
        ms = Keyword.fetch!(opts, :ms)
        tokens = Keyword.fetch!(opts, :tokens)
        :timer.apply_interval(ms, __MODULE__, :give, [pid, tokens])
    end

    def guard(pid, tokens, callback) do
        case take(pid, tokens) do
            :granted -> {:granted, callback.()}
            :denied -> :denied
        end
    end

    def take(pid, tokens) do
        if tokens < 1 do
            raise ArgumentError, "Must request one or more tokens"
        end

        Agent.get_and_update(pid, fn state ->
            if tokens <= state.tokens do
                {:granted, %{state | tokens: state.tokens - tokens}}
            else
                {:denied, state}
            end
        end)
    end

    def give(pid, tokens) do
        if tokens < 1 do
            raise ArgumentError, "May only give one or more tokens"
        end

        Agent.update(pid, fn state ->
            %{state | tokens: min(state.max_tokens, state.tokens + tokens)}
        end)
    end
end
