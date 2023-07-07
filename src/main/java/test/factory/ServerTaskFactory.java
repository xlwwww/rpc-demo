package test.factory;

import test.rpc.task.ServerTaskRunner;
import test.rpc.task.ServerTaskRunnerImpl;

public class ServerTaskFactory {
    private static final ServerTaskRunner runner = new ServerTaskRunnerImpl();

    public static ServerTaskRunner getServerTaskRunner() {
        return runner;
    }
}
