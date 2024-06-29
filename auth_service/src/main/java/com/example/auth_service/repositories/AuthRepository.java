package com.example.auth_service.repositories;

import com.example.auth_service.entities.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {

    Auth findByUsername(String username);

    // You can add more custom queries if needed

}
