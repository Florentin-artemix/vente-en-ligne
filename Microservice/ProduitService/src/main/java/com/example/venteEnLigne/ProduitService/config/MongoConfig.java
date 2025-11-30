package com.example.venteEnLigne.ProduitService.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/produits_db}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        // Force l'utilisation de la variable d'environnement
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri == null || uri.isEmpty()) {
            uri = mongoUri;
        }
        
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        
        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri == null || uri.isEmpty()) {
            uri = mongoUri;
        }
        
        // Extraire le nom de la base de données de l'URI
        String databaseName = uri.substring(uri.lastIndexOf("/") + 1);
        if (databaseName.contains("?")) {
            databaseName = databaseName.substring(0, databaseName.indexOf("?"));
        }
        
        return new MongoTemplate(mongoClient(), databaseName);
    }
}
