package de.mxro.httpserver.internal.services;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import delight.async.AsyncCommon;
import delight.async.callbacks.SimpleCallback;
import delight.concurrency.jre.JreConcurrency;
import delight.concurrency.wrappers.SimpleExecutor;
import delight.functional.Closure;
import delight.functional.SuccessFail;
import delight.simplelog.Log;

public final class ConcurrentWorkerThreadService implements HttpService {

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
            Log.warn(this + ": WARNING thread queue is getting long for " + threadName
                    + ". Currently waiting: " + executor.pendingTasks());
        }

        executor.execute(new Runnable() {

            @Override
            public void run() {

                decorated.process(request, response, callback);

            }

        }, this.taskTimeout, new Runnable() {

            @Override
            public void run() {
                Log.warn(this + ": Processing of service timed out. Service: " + decorated);

                response.setResponseCode(524);
                response.setMimeType("text/plain");
                response.setContent(
                        "The call could not be completed since the thread ran longer than the maximum allowed time. Service: "
                                + decorated);

                callback.apply(SuccessFail.success());

            }

        });

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
