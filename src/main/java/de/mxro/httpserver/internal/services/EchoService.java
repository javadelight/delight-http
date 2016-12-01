package de.mxro.httpserver.internal.services;

import delight.async.callbacks.SimpleCallback;
import delight.functional.Closure;
import delight.functional.SuccessFail;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;

public class EchoService implements HttpService {

    int delay = 0;

    @Override
    public void stop(final SimpleCallback callback) {
        callback.onSuccess();
    }

    @Override
    public void start(final SimpleCallback callback) {
        callback.onSuccess();
    }

    public void setDelay(final int delay) {
        this.delay = delay;
    }

    @Override
    public void process(final Request request, final Response response, final Closure<SuccessFail> callback) {

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        response.setContent(request.getData());

        response.setHeader("REQUEST-URI", request.getRequestUri());
        callback.apply(SuccessFail.success());
    }

}
