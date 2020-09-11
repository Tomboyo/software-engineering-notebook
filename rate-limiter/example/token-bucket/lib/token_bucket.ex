defmodule TokenBucket do
    defprotocol Bucket do
        def guard(bucket, tokens, callback)
        def take(bucket, tokens)
        def give(bucket, tokens)
    end

    @callback new(integer, keyword) :: term
    @callback close(term) :: term

    def new(impl, max_tokens, refresh) do
        impl.new(max_tokens, refresh)
    end

    def close(impl, bucket) do
        impl.close(bucket)
    end

    defdelegate guard(bucket, tokens, callback), to: Bucket

    defdelegate take(bucket, tokens), to: Bucket

    defdelegate give(bucket, tokens), to: Bucket
end
