defmodule TokenBucket.AgentBucket.DynamicSupervisor do
    use DynamicSupervisor
    alias TokenBucket.AgentBucket

    def start_link(init_arg) do
        DynamicSupervisor.start_link(__MODULE__, init_arg, name: __MODULE__)
    end

    @impl true
    def init(_init_arg) do
        DynamicSupervisor.init(strategy: :one_for_one)
    end

    def create_bucket(max_tokens, refresh) do
        opts = [max_tokens: max_tokens, refresh: refresh]
        with {:ok, pid} <- DynamicSupervisor.start_child(
            __MODULE__, {AgentBucket.Agent, opts})
        do
            {:ok, %AgentBucket{pid: pid}}
        end
    end

    def destroy_bucket(bucket) do
        DynamicSupervisor.terminate_child(__MODULE__, bucket)
    end
end
