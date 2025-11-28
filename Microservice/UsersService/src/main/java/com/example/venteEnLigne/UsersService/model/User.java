package com.example.venteEnLigne.UsersService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id; // Firebase UID

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "telephone")
    private String telephone;

    @Embedded
    private Adresse adresse;

    @Column(name = "photo_profil")
    private String photoProfil; // URL de la photo de profil

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
