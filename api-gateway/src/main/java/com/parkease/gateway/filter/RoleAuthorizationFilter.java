package com.parkease.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@Component
@Slf4j
public class RoleAuthorizationFilter implements GlobalFilter, Ordered {

    @Value("${gateway.public-paths}")
    private List<String> publicPaths;

    @Value("${gateway.admin-paths}")
    private List<String> adminPaths;

    @Value("${gateway.manager-paths}")
    private List<String> managerPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String role = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Role");

        if (role == null) {
            return onForbidden(exchange, "Role information missing");
        }

        if ("ADMIN".equals(role)) {
            return chain.filter(exchange);
        }

        if (isAdminPath(path)) {
            log.warn("Access denied: role {} tried to access admin path: {}", role, path);
            return onForbidden(exchange, "Access denied: Admin role required for " + path);
        }

        if (isManagerPath(path) && !"MANAGER".equals(role)) {
            log.warn("Access denied: role {} tried to access manager path: {}", role, path);
            return onForbidden(exchange, "Access denied: Manager or Admin role required for " + path);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .anyMatch(p -> path.equals(p) || path.startsWith(p.endsWith("/") ? p : p + "/"));
    }

    private boolean isAdminPath(String path) {
        return matchesAny(path, adminPaths);
    }

    private boolean isManagerPath(String path) {
        return matchesAny(path, managerPaths);
    }

    private boolean matchesAny(String path, List<String> patterns) {
        return patterns.stream().anyMatch(p -> {
            try {
                String regex = p.replace("**", ".*").replace("*", "[^/]*");
                return path.matches(regex);
            } catch (PatternSyntaxException e) {
                log.error("Invalid path pattern in config: {}", p);
                return false;
            }
        });
    }

    private Mono<Void> onForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String body = """
                {
                  "timestamp": "%s",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "%s"
                }
                """.formatted(LocalDateTime.now().toString(), message);

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
