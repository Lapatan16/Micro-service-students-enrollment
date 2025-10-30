package com.example.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    @Value("${gateway.api.key}")
    private String apiKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip authentication for Eureka and actuator endpoints (optional)
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator") || path.startsWith("/eureka")) {
            return chain.filter(exchange);
        }

        // Read the header
        String requestKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");

        // Validate the key
        if (requestKey == null || !requestKey.equals(apiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Continue the chain
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run before most filters (low order value = high priority)
        return -1;
    }
}
