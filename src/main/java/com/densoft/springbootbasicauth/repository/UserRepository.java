package com.densoft.springbootbasicauth.repository;


import com.densoft.springbootbasicauth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String token);

    @Modifying
    @Query("UPDATE  User u SET u.failedAttempt = ?1 WHERE u.email = ?2")
    void updateFailedAttempts(int attempts, String email);
}
