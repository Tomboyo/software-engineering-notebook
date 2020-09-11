defmodule TokenBucket.AgentBucket do
    alias TokenBucket.AgentBucket
    alias TokenBucket.AgentBucket
    @behaviour TokenBucket

    defstruct [:pid]

    @impl TokenBucket
    defdelegate new(max_tokens, refresh),
    to: AgentBucket.DynamicSupervisor,
    as: :create_bucket

    @impl TokenBucket
    def close(bucket) do
        AgentBucket.DynamicSupervisor.destroy_bucket(bucket.pid)
    end

    defimpl TokenBucket.Bucket do
        def guard(bucket, tokens, callback) do
            AgentBucket.Agent.guard(bucket.pid, tokens, callback)
        end

        def take(bucket, tokens) do
            AgentBucket.Agent.take(bucket.pid, tokens)
        end

        def give(bucket, tokens) do
            AgentBucket.Agent.give(bucket.pid, tokens)
        end
    end
end