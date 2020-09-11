defmodule TokenBucket.BenchTemplate do
    defmacro __using__(impl: impl) do
        quote do
            use ExUnit.Case
            import Bench
        
            @moduletag :bench
        
            defp noop do
            end
        
            test "#{unquote(impl)} :granted throughput" do
                {:ok, bucket} = TokenBucket.new(
                    unquote(impl),
                    1_000_000_000,
                    :never)
                bench(
                    {5_000, :ms},
                    {TokenBucket, :guard, [bucket, 1, &noop/0]})
                |> (&display("#{unquote(impl)} :granted throughput", &1)).()
            end
        
            test "#{unquote(impl)} :denied throughput" do
                {:ok, bucket} = TokenBucket.new(
                    unquote(impl),
                    0,
                    :never)
                bench(
                    {5_000, :ms},
                    {TokenBucket, :guard, [bucket, 1, &noop/0]})
                |> (&display("#{unquote(impl)} :denied throughput", &1)).()
            end
        end
    end
end
