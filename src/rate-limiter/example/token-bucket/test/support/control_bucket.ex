defmodule ControlBucket do
    @behaviour TokenBucket

    defstruct [:guard, :take]

    def new(tokens, _opts) do
        if tokens > 0 do
            {:ok, %__MODULE__{guard: {:granted, nil}, take: :granted}}
        else
            {:ok, %__MODULE__{guard: :denied, take: :denied}}
        end
    end

    def close(_bucket) do
    end

    defimpl TokenBucket.Bucket do
        def guard(bucket, _tokens, _callback) do
            bucket.guard
        end
        
        def take(bucket, _tokens) do
            bucket.guard
        end

        def give(_bucket, _tokens) do
        end
    end
end
