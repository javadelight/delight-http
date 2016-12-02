package de.mxro.httpserver.tests;

import delight.async.AsyncCommon;
import delight.async.Operation;
import delight.async.callbacks.SimpleCallback;
import delight.async.callbacks.ValueCallback;
import delight.async.jre.Async;
import delight.functional.Closure;
import delight.functional.Success;
import delight.functional.SuccessFail;

import org.junit.Test;

import de.mxro.httpserver.HttpServer;
import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import de.mxro.httpserver.internal.services.requesttimes.RequestTimeEnforcerService;

public class TestEnforceTimeout {

    @Test
    public void testNoTimeout() throws InterruptedException {

        final RequestTimeEnforcerService testService = new RequestTimeEnforcerService(100, new HttpService() {

            @Override
            public void process(final Request request, final Response response, final Closure<SuccessFail> callback) {
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
                callback.apply(SuccessFail.success());
            }

            @Override
            public void stop(final SimpleCallback callback) {
                callback.onSuccess();
            }

            @Override
            public void start(final SimpleCallback callback) {
                callback.onSuccess();
            }
        });

        Async.waitFor(new Operation<Success>() {

            @Override
            public void apply(final ValueCallback<Success> callback) {
                testService.start(AsyncCommon.asSimpleCallback(callback));
            }

        });

        Async.waitFor(new Operation<Success>() {

            @Override
            public void apply(final ValueCallback<Success> callback) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Response response = HttpServer.createResponse();
                        testService.process(HttpServer.createRequest(), response, new Closure<SuccessFail>() {

                            @Override
                            public void apply(final SuccessFail o) {
                                if (response.getResponseCode() != 524) {
                                    callback.onFailure(new Exception("Wrong reponse code."));
                                } else {
                                    callback.onSuccess(Success.INSTANCE);
                                }

                            }
                        });
                    }

                }).start();

            }

        });

        Async.waitFor(new Operation<Success>() {

            @Override
            public void apply(final ValueCallback<Success> callback) {
                testService.stop(AsyncCommon.asSimpleCallback(callback));
            }

        });

    }

}
