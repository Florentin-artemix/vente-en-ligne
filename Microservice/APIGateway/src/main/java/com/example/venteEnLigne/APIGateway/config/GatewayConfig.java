package com.example.venteEnLigne.APIGateway.config;

import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> usersServiceRoute() {
        return route("users-service")
                .route(path("/api/users/**"), HandlerFunctions.http())
                .filter(lb("UsersService"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> produitServiceRoute() {
        return route("produit-service")
                .route(path("/api/produits/**"), HandlerFunctions.http())
                .filter(lb("ProduitService"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> carteServiceRoute() {
        return route("carte-service")
                .route(path("/api/carte/**"), HandlerFunctions.http())
                .filter(lb("CarteService"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return route("order-service")
                .route(path("/api/orders/**"), HandlerFunctions.http())
                .filter(lb("OrderService"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> paiementServiceRoute() {
        return route("paiement-service")
                .route(path("/api/paiements/**"), HandlerFunctions.http())
                .filter(lb("paiement"))
                .build();
    }
}
