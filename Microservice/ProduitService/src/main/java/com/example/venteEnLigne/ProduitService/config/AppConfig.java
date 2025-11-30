package com.example.venteEnLigne.ProduitService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class AppConfig {
    // CORS est géré par l'API Gateway
}
