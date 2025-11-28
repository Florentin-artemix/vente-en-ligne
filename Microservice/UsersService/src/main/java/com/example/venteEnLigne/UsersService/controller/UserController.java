package com.example.venteEnLigne.UsersService.controller;

import com.example.venteEnLigne.UsersService.dto.*;
import com.example.venteEnLigne.UsersService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Requête d'inscription reçue pour: {}", request.getEmail());
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<UserResponse> verifyToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Vérification du token");
        String token = authHeader.replace("Bearer ", "");
        UserResponse user = userService.verifyTokenAndGetUser(token);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Récupération de l'utilisateur: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("Récupération de l'utilisateur par email: {}", email);
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, 
                                                    @Valid @RequestBody UserUpdateRequest request) {
        log.info("Mise à jour de l'utilisateur: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Suppression de l'utilisateur: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
