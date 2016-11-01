package de.mxro.httpserver.internal.services;

import delight.async.AsyncCommon;
import delight.async.callbacks.SimpleCallback;
import delight.concurrency.jre.JreConcurrency;
import delight.concurrency.wrappers.SimpleExecutor;
import delight.functional.Closure;
import delight.functional.SuccessFail;

import java.util.concurrent.Callable;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;

public class ConcurrentWorkerThreadService implements HttpService {

    private final int maxThreads;
    private final HttpService decorated;
    private SimpleExecutor executor;
    private final String threadName;
    private final int taskTimeout;

    @Override
    public void stop(final SimpleCallback callback) {

        decorated.stop(AsyncCommon.embed(callback, new Runnable() {

            @Override
            public void run() {
                executor.shutdown(callback);
            }

        }));

    }

    @Override
    public void start(final SimpleCallback callback) {

        executor = new JreConcurrency().newExecutor().newParallelExecutor(maxThreads, threadName + "@" + this);

        decorated.start(callback);
    }

    @Override
    public void process(final Request request, final Response response, final Closure<SuccessFail> callback) {

        if (executor.pendingTasks() > 200) {
            System.out.println(this + ": WARNING thread queue is getting long for " + threadName
                    + ". Currently waiting: " + executor.pendingTasks());
        }

        executor.execute(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                decorated.process(request, response, callback);
                return null;
            }

        }, this.taskTimeout);

    }

    public ConcurrentWorkerThreadService(final String threadName, final int maxThreads, final int taskTimeout,
            final HttpService decorated) {
        super();
        this.threadName = threadName;
        this.maxThreads = maxThreads;
        this.decorated = decorated;
        this.taskTimeout = taskTimeout;
    }

}
