package com.densoft.springbootbasicauth.service;

import com.densoft.springbootbasicauth.model.User;
import com.densoft.springbootbasicauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUser(String email) {
        return userRepository.getUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("no user found"));
    }
}
