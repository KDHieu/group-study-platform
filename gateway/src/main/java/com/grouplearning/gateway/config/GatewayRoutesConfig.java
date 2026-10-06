package com.grouplearning.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,

            @Value("${services.backend.url}")
            String backendUrl,

            @Value("${services.backend.ws-url}")
            String backendWsUrl
    ) {
        return builder.routes()

                /*
                 * WebSocket route.
                 *
                 * Client:
                 * ws://localhost:8080/ws
                 *
                 * Gateway:
                 * ws://localhost:8081/ws
                 */
                .route("websocket-service", route -> route
                        .path(
                                "/ws",
                                "/ws/**"
                        )
                        .uri(backendWsUrl)
                )

                .route("auth-service", route -> route
                        .path("/api/auth/**")
                        .uri(backendUrl)
                )

                .route("group-service", route -> route
                        .path("/api/groups/**")
                        .uri(backendUrl)
                )

                .route("user-service", route -> route
                        .path("/api/users/**")
                        .uri(backendUrl)
                )

                .route("friend-service", route -> route
                        .path("/api/friends/**")
                        .uri(backendUrl)
                )

                .route("message-service", route -> route
                        .path("/api/messages/**")
                        .uri(backendUrl)
                )

                .build();
    }
}