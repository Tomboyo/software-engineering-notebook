defmodule TokenBucket.AtomicBucket do
    @behaviour TokenBucket

    defstruct [:max_tokens, :atomics, :timer]

    @impl TokenBucket
    def new(max_tokens, refresh) do
        atomics = :atomics.new(1, signed: false)
        :atomics.put(atomics, 1, max_tokens)
        
        with {:ok, timer} <- configure_timer(refresh, atomics)
        do
            {:ok, %__MODULE__{
                max_tokens: max_tokens,
                atomics: atomics,
                timer: timer
            }}
        end
    end

    defp configure_timer(:never, _), do: {:ok, nil}
    defp configure_timer(opts, atomics) do
        ms = Keyword.fetch!(opts, :ms)
        tokens = Keyword.fetch!(opts, :tokens)
        :timer.apply_interval(ms, :atomics, :add, [atomics, 1, tokens])
    end

    @impl TokenBucket
    def close(bucket) do
        :timer.cancel(bucket.timer)
    end

    defimpl TokenBucket.Bucket do
        def guard(bucket, tokens, callback) do
            with :granted <- take(bucket, tokens) do
                {:granted, callback.()}
            end
        end

        # tag::take[]
        def take(bucket, tokens) do
            if tokens < 1 do
                raise ArgumentError, "Must request one or more tokens"
            end

            take_validated(bucket, tokens)
        end

        defp take_validated(bucket, tokens) do
            available = :atomics.get(bucket.atomics, 1)
            if available >= tokens do
                if :ok == :atomics.compare_exchange(
                        bucket.atomics, 1, available, available - tokens)
                do
                    :granted
                else
                    take_validated(bucket, tokens)
                end
            else
                :denied
            end
        end
        # end::take[]

        def give(bucket, tokens) do
            if tokens < 1 do
                raise ArgumentError, "May only give one or more tokens"
            end

            give_validated(bucket, tokens)
        end

        defp give_validated(bucket, tokens) do
            available = :atomics.get(bucket.atomics, 1)
            desired = min(bucket.max_tokens, available + tokens)
            if :ok != :atomics.compare_exchange(
                    bucket.atomics, 1, available, desired)
            do
                give_validated(bucket, tokens)
            end
        end
    end
end
