package com.example.venteEnLigne.APIGateway.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${FIREBASE_CREDENTIALS:}")
    private String firebaseCredentialsJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getFirebaseCredentials();
                
                if (serviceAccount != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase initialized successfully for API Gateway");
                } else {
                    logger.warn("Firebase credentials not found - authentication will fail");
                }
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    private InputStream getFirebaseCredentials() throws IOException {
        // Option 1: Variable d'environnement avec JSON
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isEmpty()) {
            logger.info("Using Firebase credentials from environment variable");
            return new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        // Option 2: Fichier dans les ressources
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            try {
                Resource resource = new ClassPathResource(credentialsPath);
                if (resource.exists()) {
                    logger.info("Using Firebase credentials from classpath: {}", credentialsPath);
                    return resource.getInputStream();
                }
            } catch (Exception e) {
                logger.warn("Could not load credentials from path: {}", credentialsPath);
            }
        }

        // Option 3: Fichier par défaut
        String[] defaultPaths = {
            "firebase-credentials.json",
            "firebase-adminsdk.json",
            "serviceAccountKey.json"
        };

        for (String path : defaultPaths) {
            try {
                Resource resource = new ClassPathResource(path);
                if (resource.exists()) {
                    logger.info("Using Firebase credentials from: {}", path);
                    return resource.getInputStream();
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }

        logger.warn("No Firebase credentials found");
        return null;
    }
}
