defmodule TokenBucket.Application do
    use Application

    def start(_type, _args) do
        children = [TokenBucket.AgentBucket.DynamicSupervisor]
        Supervisor.start_link(children, strategy: :one_for_one)
    end
end
