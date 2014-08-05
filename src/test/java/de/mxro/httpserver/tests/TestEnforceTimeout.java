package de.mxro.httpserver.tests;

import java.util.concurrent.CountDownLatch;

import io.nextweb.fn.Closure;
import io.nextweb.fn.SuccessFail;

import org.junit.Test;

import de.mxro.httpserver.HttpServer;
import de.mxro.httpserver.Request;
import de.mxro.httpserver.Response;
import de.mxro.httpserver.HttpService;
import de.mxro.httpserver.internal.services.RequestTimeEnforcerService;
import de.mxro.service.callbacks.ShutdownCallback;
import de.mxro.service.callbacks.StartCallback;

public class TestEnforceTimeout {

	
	@Test
	public void testNoTimeout() throws InterruptedException {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		RequestTimeEnforcerService testService = new RequestTimeEnforcerService(100, new HttpService() {
			
			@Override
			public void process(Request request, Response response,
					Closure<SuccessFail> callback) {
				
				callback.apply(SuccessFail.success());
			}

			@Override
			public void stop(ShutdownCallback callback) {
				callback.onShutdownComplete();
			}

			@Override
			public void start(StartCallback callback) {
				callback.onStarted();
			}
		});
		
		testService.process(HttpServer.createDummyRequest(), HttpServer.createResponse(), new Closure<SuccessFail>() {

			@Override
			public void apply(SuccessFail o) {
				latch.countDown();
			}
		});
		
		latch.await();
		
		
	}
	
}
