package de.mxro.httpserver.internal.services;

import java.util.List;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import delight.async.callbacks.SimpleCallback;
import delight.functional.Closure;
import delight.functional.SuccessFail;
import delight.json.JSON;
import delight.json.JSONArray;
import delight.state.StateProvider;
import delight.state.StateRegistry;

public class StateService implements HttpService {

	private final String rootPath;
	private final StateRegistry registry;

	@Override
	public void stop(SimpleCallback callback) {
		callback.onSuccess();
	}

	@Override
	public void start(SimpleCallback callback) {
		callback.onSuccess();
	}

	@Override
	public void process(Request request, Response response, Closure<SuccessFail> callback) {
		String requestUri = request.getRequestUri();

		assert requestUri.startsWith(rootPath);

		String path = requestUri.substring(rootPath.length());

		List<StateProvider> providers = registry.getProviders(path);

		if (providers.size() == 0) {
			response.setContent("Resource [" + path + "] does not exist or has not been initialized.");
			response.setResponseCode(404);
			callback.apply(SuccessFail.success());
			return;
		}

		if (providers.size() == 1) {

			response.setContent(providers.get(0).get().render());
			response.setMimeType("text/json");
			response.setResponseCode(200);

			callback.apply(SuccessFail.success());
			return;
		}
		
		JSONArray ar = JSON.createArray();

		for (StateProvider provider: providers) {
			ar.push(provider.get());
		}
		
		response.setContent(ar.render());
		response.setMimeType("text/json");
		response.setResponseCode(200);

		callback.apply(SuccessFail.success());
		
	}

	public StateService(String rootPath, StateRegistry registry) {
		super();
		this.rootPath = rootPath;
		this.registry = registry;
	}

}
