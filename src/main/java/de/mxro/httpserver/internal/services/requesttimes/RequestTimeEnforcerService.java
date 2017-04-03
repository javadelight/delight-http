package de.mxro.httpserver.internal.services.requesttimes;

import delight.async.callbacks.SimpleCallback;
import delight.concurrency.jre.JreConcurrency;
import delight.concurrency.schedule.timeout.TimeoutWatcher;
import delight.concurrency.wrappers.SimpleAtomicBoolean;
import delight.functional.Closure;
import delight.functional.Function;
import delight.functional.SuccessFail;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import de.mxro.service.callbacks.ShutdownCallback;

public final class RequestTimeEnforcerService implements HttpService {

    private final HttpService decorated;

    private final TimeoutWatcher timeoutWatcher;
    private final long timeout;

    @Override
    public void process(final Request request, final Response response, final Closure<SuccessFail> callback) {

        final SimpleAtomicBoolean isCompleted = new JreConcurrency().newAtomicBoolean(false);
        final SimpleAtomicBoolean isFailed = new JreConcurrency().newAtomicBoolean(false);

        timeoutWatcher.watch((int) timeout, new Function<Void, Boolean>() {

            @Override
            public Boolean apply(final Void input) {
                return isCompleted.get();
            }

        }, new Runnable() {

            @Override
            public void run() {
                isFailed.set(true);
                System.err.println(RequestTimeEnforcerService.this + ": Message not processed in timeout: " + request);
                response.setResponseCode(524);
                response.setMimeType("text/plain");
                response.setContent(
                        "The call could not be completed since it took longer than the maximum allowed time. Service: "
                                + decorated);
                new JreConcurrency().newTimer().scheduleOnce(1, new Runnable() {

                    @Override
                    public void run() {
                        callback.apply(SuccessFail.success());
                    }

                });

            }

        });

        decorated.process(request, response, new Closure<SuccessFail>() {

            @Override
            public void apply(final SuccessFail o) {
                if (isFailed.get()) {
                    System.err.println(RequestTimeEnforcerService.this
                            + ": Trying to call callback for already timed out message: " + request);
                    return;
                }

                if (isCompleted.get()) {
                    System.err.println(RequestTimeEnforcerService.this + ": Trying to call callback twice for message: "
                            + request);
                    return;
                }

                isCompleted.set(true);
                callback.apply(o);
            }
        });

    }

    @Override
    public void stop(final SimpleCallback callback) {

        decorated.stop(new ShutdownCallback() {

            @Override
            public void onSuccess() {
                timeoutWatcher.shutdown(callback);
            }

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }
        });

    }

    @Override
    public String toString() {
        return super.toString() + " wraps " + this.decorated;
    }

    @Override
    public void start(final SimpleCallback callback) {

        this.decorated.start(callback);
    }

    public RequestTimeEnforcerService(final long maxTime, final HttpService decorated) {
        super();

        this.decorated = decorated;
        this.timeoutWatcher = new TimeoutWatcher(new JreConcurrency());
        this.timeout = maxTime;
    }

}
