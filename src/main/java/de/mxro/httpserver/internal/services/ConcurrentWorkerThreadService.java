package de.mxro.httpserver.internal.services;

import delight.async.AsyncCommon;
import delight.async.callbacks.SimpleCallback;
import delight.concurrency.jre.JreConcurrency;
import delight.concurrency.wrappers.SimpleExecutor;
import delight.functional.Closure;
import delight.functional.SuccessFail;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;

public class ConcurrentWorkerThreadService implements HttpService {

    private final int maxThreads;
    private final HttpService decorated;
    private SimpleExecutor executor;
    private final String threadName;

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

        executor.execute(new Runnable() {

            @Override
            public void run() {
                decorated.process(request, response, callback);
            }
        });

    }

    public ConcurrentWorkerThreadService(final String threadName, final int maxThreads, final HttpService decorated) {
        super();
        this.threadName = threadName;
        this.maxThreads = maxThreads;
        this.decorated = decorated;
    }

}
