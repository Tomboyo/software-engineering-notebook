defmodule Bench do
    def bench({duration, :ms}, mfa = {_m, _f, _a}) do
        start = :erlang.monotonic_time(:nanosecond)
        {finish, calls} = probe_until(start + duration * 1_000_000, mfa)
        elapsed = finish - start
        {elapsed, calls}
    end

    def display(title, {elapsed, calls}) do
        IO.puts("#{title}")
        IO.puts("  elapsed time: #{elapsed} ns")
        IO.puts("                #{elapsed / 1_000_000} ms")
        IO.puts("  total calls:  #{calls}")
        ns_per_call = elapsed / calls
        IO.puts("  call cost:    #{ns_per_call} ns/call")
        IO.puts("                #{ns_per_call / 1_000} us/call")
        rate = calls / elapsed
        IO.puts("  call rate:    #{rate} calls/ns")
        IO.puts("                #{rate * 1_000_000} calls/ms") 
    end

    defp probe_until(time, mfa = {m, f, a}, calls \\ 0) do
        now = :erlang.monotonic_time(:nanosecond)
        if now < time do
            apply(m, f, a)
            probe_until(time, mfa, calls + 1)
        else
            {now, calls}
        end
    end
end
