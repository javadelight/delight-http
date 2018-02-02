package de.mxro.httpserver.internal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import delight.async.callbacks.SimpleCallback;
import delight.concurrency.Concurrency;
import delight.functional.Closure;
import delight.functional.SuccessFail;
import delight.simplelog.Log;
import delight.trie.TrieMap;

public final class DispatchService implements HttpService {
	
	private final boolean ENABLE_TRACE = false;
	
    private static final boolean ENABLE_LOG = false;

    private final TrieMap<HttpService> serviceMap;

    @Override
    public final void process(final Request request, final Response response, final Closure<SuccessFail> callback) {
        
        final String uri = request.getRequestUri();
        
       
        
        
        HttpService service = serviceMap.getValueForBestMatchingKey(uri);
        if (ENABLE_TRACE) {
        	Log.trace(this, "Processing URI: "+uri);
        	Log.trace(this, "Found match: "+service);
        }
        
        if (service != null) {
        	service.process(request, response, callback);
        	return;
        }
        
        

        HttpService defaultService = serviceMap.get("*");
        if (defaultService != null) {
        	defaultService.process(request, response, callback);
        	return;
        }
        
        callback.apply(SuccessFail.fail(new Exception("No service defined for path ["+uri+"]")));

    }

    @Override
    public void stop(final SimpleCallback callback) {

        final ArrayList<HttpService> services = new ArrayList<HttpService>();

        for (final Entry<String, HttpService> e : serviceMap.entrySet()) {
            services.add(e.getValue());
        }

        stop(services, 0, callback);
    }

    private static void stop(final List<HttpService> services, final int serviceIdx, final SimpleCallback callback) {
        if (serviceIdx >= services.size()) {
            callback.onSuccess();
            return;
        }

        if (ENABLE_LOG) {
            System.out.println(DispatchService.class + ": Stopping service " + services.get(serviceIdx));
        }

        services.get(serviceIdx).stop(new SimpleCallback() {

            @Override
            public void onSuccess() {

                if (ENABLE_LOG) {
                    System.out.println(DispatchService.class + ": Stopped service " + services.get(serviceIdx));
                }
                stop(services, serviceIdx + 1, callback);
            }

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public DispatchService(Concurrency conn, final Map<String, HttpService> serviceMap) {
        super();
        this.serviceMap = new TrieMap<HttpService>(conn, serviceMap);
    }

    @Override
    public void start(final SimpleCallback callback) {

        final ArrayList<HttpService> services = new ArrayList<HttpService>();

        for (final Entry<String, HttpService> e : serviceMap.entrySet()) {
            services.add(e.getValue());
        }

        start(services, 0, callback);
    }

    private static void start(final List<HttpService> services, final int serviceIdx, final SimpleCallback callback) {
        if (serviceIdx >= services.size()) {
            callback.onSuccess();
            return;
        }

        services.get(serviceIdx).start(new SimpleCallback() {

            @Override
            public void onSuccess() {
                start(services, serviceIdx + 1, callback);
            }

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }
        });
    }

}
