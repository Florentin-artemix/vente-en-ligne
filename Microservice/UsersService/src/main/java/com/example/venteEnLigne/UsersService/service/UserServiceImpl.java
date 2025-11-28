package com.example.venteEnLigne.UsersService.service;

import com.example.venteEnLigne.UsersService.dto.*;
import com.example.venteEnLigne.UsersService.exception.FirebaseAuthenticationException;
import com.example.venteEnLigne.UsersService.exception.UserAlreadyExistsException;
import com.example.venteEnLigne.UsersService.exception.UserNotFoundException;
import com.example.venteEnLigne.UsersService.model.User;
import com.example.venteEnLigne.UsersService.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    @Override
    @Transactional
    public AuthResponse registerUser(UserRegistrationRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.getEmail());
        
        // Vérifier si l'utilisateur existe déjà dans notre base de données
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        try {
            // Créer l'utilisateur dans Firebase Authentication
            UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getPrenom() + " " + request.getNom())
                    .setEmailVerified(false);

            UserRecord firebaseUser = firebaseAuth.createUser(firebaseRequest);
            String firebaseUid = firebaseUser.getUid();

            // Ajouter des custom claims pour le rôle
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", request.getRole().name());
            firebaseAuth.setCustomUserClaims(firebaseUid, claims);

            // Créer l'utilisateur dans notre base de données PostgreSQL
            User user = User.builder()
                    .id(firebaseUid)
                    .nom(request.getNom())
                    .prenom(request.getPrenom())
                    .email(request.getEmail())
                    .role(request.getRole())
                    .telephone(request.getTelephone())
                    .adresse(request.getAdresse())
                    .photoProfil(request.getPhotoProfil())
                    .build();

            User savedUser = userRepository.save(user);
            log.info("Utilisateur créé avec succès: {}", savedUser.getId());

            // Générer un token personnalisé pour l'utilisateur
            String customToken = firebaseAuth.createCustomToken(firebaseUid, claims);

            return AuthResponse.builder()
                    .token(customToken)
                    .user(mapToUserResponse(savedUser))
                    .message("Inscription réussie!")
                    .build();

        } catch (FirebaseAuthException e) {
            log.error("Erreur Firebase lors de l'inscription: {}", e.getMessage());
            throw new FirebaseAuthenticationException("Erreur lors de la création du compte Firebase: " + e.getMessage(), e);
        }
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            user.setPrenom(request.getPrenom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }
        if (request.getAdresse() != null) {
            user.setAdresse(request.getAdresse());
        }
        if (request.getPhotoProfil() != null) {
            user.setPhotoProfil(request.getPhotoProfil());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
            // Mettre à jour les custom claims Firebase
            try {
                Map<String, Object> claims = new HashMap<>();
                claims.put("role", request.getRole().name());
                firebaseAuth.setCustomUserClaims(id, claims);
            } catch (FirebaseAuthException e) {
                log.error("Erreur lors de la mise à jour des claims Firebase: {}", e.getMessage());
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour: {}", updatedUser.getId());
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        try {
            // Supprimer de Firebase
            firebaseAuth.deleteUser(id);
        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la suppression de l'utilisateur Firebase: {}", e.getMessage());
        }

        // Supprimer de la base de données
        userRepository.delete(user);
        log.info("Utilisateur supprimé: {}", id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse verifyTokenAndGetUser(String token) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String uid = decodedToken.getUid();
            
            User user = userRepository.findById(uid)
                    .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + uid));
            
            return mapToUserResponse(user);
        } catch (FirebaseAuthException e) {
            log.error("Erreur de vérification du token: {}", e.getMessage());
            throw new FirebaseAuthenticationException("Token invalide ou expiré", e);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .photoProfil(user.getPhotoProfil())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
