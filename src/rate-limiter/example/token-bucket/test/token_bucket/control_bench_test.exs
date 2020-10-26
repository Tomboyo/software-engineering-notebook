# Benchmarks the throughput of a "control" implementtion of the TokenBucket
# which is a no-op, useful to see what level of performance is lost just to
# the test suite.
defmodule TokenBucket.ControlBenchTest do
    use TokenBucket.BenchTemplate, impl: ControlBucket
end
