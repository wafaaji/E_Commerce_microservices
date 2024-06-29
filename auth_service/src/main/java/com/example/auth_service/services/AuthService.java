package com.example.auth_service.services;

import com.example.auth_service.entities.Auth;
import com.example.auth_service.repositories.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public Auth findByUsername(String username) {
        return authRepository.findByUsername(username);
    }
    public Optional<Auth> findById(Long id) {
        return authRepository.findById(id);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Auth> getAuthById(@PathVariable Long id) {
        Optional<Auth> auth = authRepository.findById(id);
        return auth.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    public Auth saveUser(Auth user) {
        // Encode the user's password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return authRepository.save(user);
    }
    public Auth updateUser(Auth user) {
        // Perform any logic before updating the user, if needed
        return authRepository.save(user);
    }

    public void deleteUser(Long userId) {
        authRepository.deleteById(userId);
    }

    // You can add more methods as needed

}
