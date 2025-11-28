package com.example.venteEnLigne.UsersService.dto;

import com.example.venteEnLigne.UsersService.model.Adresse;
import com.example.venteEnLigne.UsersService.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String nom;
    private String prenom;
    private String telephone;
    private Adresse adresse;
    private String photoProfil;
    private Role role;
}
