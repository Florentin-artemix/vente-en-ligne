package com.example.venteEnLigne.UsersService.service;

import com.example.venteEnLigne.UsersService.dto.*;
import com.example.venteEnLigne.UsersService.model.User;

import java.util.List;

public interface UserService {
    AuthResponse registerUser(UserRegistrationRequest request);
    UserResponse getUserById(String id);
    UserResponse getUserByEmail(String email);
    UserResponse updateUser(String id, UserUpdateRequest request);
    void deleteUser(String id);
    List<UserResponse> getAllUsers();
    UserResponse verifyTokenAndGetUser(String token);
}
