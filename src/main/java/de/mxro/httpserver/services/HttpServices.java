package de.mxro.httpserver.services;

import java.util.Map;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.internal.services.ConcurrentWorkerThreadService;
import de.mxro.httpserver.internal.services.DispatchService;
import de.mxro.httpserver.internal.services.EchoService;
import de.mxro.httpserver.internal.services.FilterService;
import de.mxro.httpserver.internal.services.PropertiesAsJSONService;
import de.mxro.httpserver.internal.services.ProxyService;
import de.mxro.httpserver.internal.services.ResourceService;
import de.mxro.httpserver.internal.services.SafeShutdownGuard;
import de.mxro.httpserver.internal.services.ShutdownService;
import de.mxro.httpserver.internal.services.StateService;
import de.mxro.httpserver.internal.services.StaticDataService;
import de.mxro.httpserver.internal.services.requesttimes.RequestTimeEnforcerService;
import de.mxro.httpserver.internal.services.requesttimes.TrackRequestTimeService;
import de.mxro.httpserver.resources.ResourceProvider;
import de.mxro.server.ServerComponent;
import delight.async.Value;
import delight.async.properties.PropertyNode;
import delight.concurrency.Concurrency;
import delight.functional.Function;
import delight.state.StateRegistry;

public final class HttpServices {

    public final static HttpService dispatcher(final Concurrency conn, final Map<String, HttpService> serviceMap) {
        return HttpServices.safeShutdown(new DispatchService(conn, serviceMap));
    }

    public static HttpService safeShutdown(final HttpService service) {
        return new SafeShutdownGuard(service);
    }

    public static HttpService state(String rootPath, StateRegistry registry) {
    	return new StateService(rootPath, registry);
    }
    
    public static HttpService limitTime(final long maxCallTimeInMs, final HttpService decoratedService) {
        return new RequestTimeEnforcerService(maxCallTimeInMs, decoratedService);
    }

    public static HttpService withParallelWorkerThreads(final String threadName, final int maxWorkers,
            final int timeout, final HttpService decorated) {

        return new ConcurrentWorkerThreadService(threadName, maxWorkers, timeout, decorated);
    }

    /**
     * <p>
     * Create a service, which renders the provided properties.
     * 
     * @param metrics
     * @return
     */
    public static HttpService asJSON(final PropertyNode metrics) {
        return new PropertiesAsJSONService(metrics);
    }

    public static HttpService trackRequestTimes(final PropertyNode metrics, final String metricId,
            final HttpService decorated) {
        return new TrackRequestTimeService(metricId, metrics, decorated);
    }

    public final static HttpService filter(final Function<Request, Boolean> test, final HttpService primary,
            final HttpService secondary) {
        return new FilterService(test, primary, secondary);
    }

    /**
     * A service which returns what was sent to it.
     * 
     * @return
     */
    public final static HttpService echo() {
        return new EchoService();
    }

    /**
     * A service which returns what was sent to it with a delay.
     * 
     * @return
     */
    public final static HttpService delayedEcho(final int delayInMs) {
        final EchoService echoService = new EchoService();
        echoService.setDelay(delayInMs);
        return echoService;
    }

    /**
     * Allows to serve the same byte array for every request. Useful for
     * robots.txt etc.
     * 
     * @param data
     * @param contentType
     * @return
     */
    public final static HttpService data(final byte[] data, final String contentType) {
        return new StaticDataService(data, contentType);
    }

    /**
     * Allows to server static files from a directory or the classpath.
     * 
     * @param provider
     * @return
     */
    public static HttpService resources(final ResourceProvider provider) {
        return new ResourceService(provider);
    }

    public static HttpService forward(final String destinationHost, final int destinationPort) {

        return new ProxyService(destinationHost, destinationPort);
    }

    public static HttpService shutdown(final String secret, final ServerComponent serverToShutdown,
            final Value<ServerComponent> ownServer) {
        return new ShutdownService(secret, serverToShutdown, ownServer);
    }

}
