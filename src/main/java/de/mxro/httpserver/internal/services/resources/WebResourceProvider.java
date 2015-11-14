package de.mxro.httpserver.internal.services.resources;

import de.mxro.httpserver.resources.Resource;
import de.mxro.httpserver.resources.ResourceProvider;

public class WebResourceProvider implements ResourceProvider {

    private final ResourceProvider decorated;

    @Override
    public Resource getResource(final String rawPath) {
        final String path;

        if (!rawPath.contains("?")) {
            path = rawPath;
        } else {
            path = rawPath.substring(0, rawPath.lastIndexOf("?"));
        }

        final Resource resource = decorated.getResource(path);

        if (resource == null) {
            return null;
        }

        String mimetype = resource.mimetype();
        if (path.endsWith(".html")) {
            mimetype = "text/html";
        } else if (path.endsWith(".js")) {
            mimetype = "text/javascript"; // correct would be
            // application/javascript but
            // text/javascript more compatible
        } else if (path.endsWith(".png")) {
            mimetype = "image/png";
        } else if (path.endsWith(".gif")) {
            mimetype = "image/gif";
        } else if (path.endsWith(".css")) {
            mimetype = "text/css";
        } else if (path.endsWith(".rpc")) {
            mimetype = "text/plain";
        } else if (path.endsWith(".svg")) {
            mimetype = "image/svg+xml";
        }

        final String closedMimetype = mimetype;
        return new Resource() {

            @Override
            public byte[] data() {
                return resource.data();
            }

            @Override
            public String mimetype() {
                return closedMimetype;
            }

            @Override
            public long lastModified() {
                return resource.lastModified();
            }

        };
    }

    public WebResourceProvider(final ResourceProvider decorated) {
        super();
        this.decorated = decorated;
    }

}
