package com.gateway.filter;


import com.gateway.config.GatewayJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component("JwtAuthFilter")
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends AbstractGatewayFilterFactory<Object> {

    private final GatewayJwtService jwtService;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getPath().value();

            // 1. Allow public endpoints without JWT
            if (isPublicPath(path)) {
                log.debug("Public path accessed without auth: {}", path);
                return chain.filter(exchange);
            }

            // 2. Extract Authorization header
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            // 3. Validate token
            if (!jwtService.isTokenValid(token)) {
                log.warn("Invalid or expired JWT token for path: {}", path);
                return unauthorized(exchange, "Invalid or expired token");
            }

            String username = jwtService.extractUsername(token);
            log.info("Gateway auth success for user [{}] on path {}", username, path);

            // (Optional) Add username as header to pass to downstream services
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> headers.add("X-Authenticated-User", username)))
                    .build();

            return chain.filter(mutatedExchange);
        };
    }

    private boolean isPublicPath(String path) {
        // adjust as per your service
        return path.startsWith("/api/auth/")   // login, register, refresh
                || path.startsWith("/actuator");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.error("Unauthorized access: {} - path: {}", message, exchange.getRequest().getPath().value());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "status": 401,
                  "error": "UNAUTHORIZED",
                  "message": "%s"
                }
                """.formatted(message);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }
}
