package de.mxro.httpserver.internal.services;

import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import delight.async.callbacks.SimpleCallback;
import delight.functional.Closure;
import delight.functional.SuccessFail;
import mx.gwtutils.Base64Coder;

public final class RequireBasicAuthService implements HttpService {

	private final String userName;
	private final String password;
	private final HttpService decorated;

	@Override
	public void stop(SimpleCallback callback) {
		decorated.stop(callback);
	}

	@Override
	public void start(SimpleCallback callback) {
		decorated.start(callback);
	}

	@Override
	public void process(Request request, Response response, Closure<SuccessFail> callback) {
		
		if (request.getHeaders().containsKey("Authorization")) {
			String authHeader = request.getHeader("Authorization");
			String usernpass = Base64Coder.decodeString(authHeader.substring(6));
			String userName = usernpass.substring(0, usernpass.indexOf(":"));
			String password = usernpass.substring(usernpass.indexOf(":") + 1);
			
			if (userName.equals(this.userName) && password.equals(this.password)) {
				decorated.process(request, response, callback);
				return;
			}
			
		} 
		
		response.setResponseCode(401);
		response.setHeader("WWW-Authenticate",
			"Basic realm=\"This resource requires you to authenticate.\"");
		response.setMimeType("text/html");
		response.setContent("Please provide authentication.");
		callback.apply(SuccessFail.success());
		
	}

	public RequireBasicAuthService(final String userName, final String password, final HttpService decorated) {
		super();
		this.userName = userName;
		this.password = password;
		this.decorated = decorated;
	}
	
	
	
}
