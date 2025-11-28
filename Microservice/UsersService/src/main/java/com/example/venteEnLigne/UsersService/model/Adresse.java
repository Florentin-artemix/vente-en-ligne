package com.example.venteEnLigne.UsersService.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {
    private String pays;
    private String province;
    private String ville;
    private String commune;
    private String quartier;
    private String avenue;
    private String reference;
}
