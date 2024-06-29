package com.example.auth_service.controllers;

import com.example.auth_service.entities.Auth;
import com.example.auth_service.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Value("${server.port}")
    private int serverPort;
    private final AuthService authService;
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Auth> registerUser(@RequestBody Auth user) {
        // Perform validation and error handling as needed
        Auth savedUser = authService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Auth> updateUser(@PathVariable Long id, @RequestBody Auth user) {
        Optional<Auth> existingUserOpt = authService.findById(id);
        if (!existingUserOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Auth existingUser = existingUserOpt.get();
        user.setId(id); // Ensure the ID is set correctly
        Auth updatedUser = authService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<Auth> existingUserOpt = authService.findById(id);
        if (!existingUserOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auth> getUserById(@PathVariable Long id) {
        System.out.println("we got getUserById and the port "+serverPort);
        Optional<Auth> user = authService.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    // You can add more endpoints as needed

}
