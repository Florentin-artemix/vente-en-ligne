package com.example.venteEnLigne.UsersService.dto;

import com.example.venteEnLigne.UsersService.model.Adresse;
import com.example.venteEnLigne.UsersService.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
    private String telephone;
    private Adresse adresse;
    private String photoProfil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
