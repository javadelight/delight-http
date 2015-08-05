package de.mxro.httpserver;

import delight.functional.Closure;
import delight.functional.SuccessFail;

public interface HttpProcessor {
    public void process(Request request, Response response, Closure<SuccessFail> callback);
}
