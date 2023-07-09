package rpc.factory;

import rpc.rpc.task.ServerTaskRunner;
import rpc.rpc.task.ServerTaskRunnerImpl;

public class ServerTaskFactory {
    private static final ServerTaskRunner runner = new ServerTaskRunnerImpl();

    public static ServerTaskRunner getServerTaskRunner() {
        return runner;
    }
}
