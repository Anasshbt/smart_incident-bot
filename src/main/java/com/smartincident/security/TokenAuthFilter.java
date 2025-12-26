package com.smartincident.security;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Simple token-based authentication filter.
 * Validates the X-API-Token header against configured token.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TokenAuthFilter implements ContainerRequestFilter {

    private static final String AUTH_HEADER = "X-API-Token";
    
    // Paths that don't require authentication
    private static final String[] PUBLIC_PATHS = {
        "/q/health",
        "/q/metrics",
        "/q/openapi",
        "/q/swagger-ui"
    };

    @ConfigProperty(name = "app.security.api-token")
    String configuredToken;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // Skip authentication for public paths
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath) || path.equals(publicPath.substring(1))) {
                return;
            }
        }

        // Get token from header
        String token = requestContext.getHeaderString(AUTH_HEADER);
        
        if (token == null || token.isBlank()) {
            Log.debugf("Missing API token for path: %s", path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Missing API token\", \"message\": \"Provide X-API-Token header\"}")
                    .build()
            );
            return;
        }

        if (!token.equals(configuredToken)) {
            Log.warnf("Invalid API token attempt for path: %s", path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid API token\"}")
                    .build()
            );
        }
    }
}
