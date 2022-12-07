package com.densoft.springbootbasicauth.repository;

import com.densoft.springbootbasicauth.auth.MyUserDetails;
import com.densoft.springbootbasicauth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> getUserByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
}
