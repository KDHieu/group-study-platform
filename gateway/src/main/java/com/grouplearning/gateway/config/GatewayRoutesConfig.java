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
            @Value("${services.backend.url}") String backendUrl
    ) {
        return builder.routes()

                .route(
                        "auth-service",
                        route -> route
                                .path("/api/auth/**")
                                .uri(backendUrl)
                )

                .build();
    }
}