package com.densoft.springbootbasicauth.service;

import com.densoft.springbootbasicauth.model.Role;
import com.densoft.springbootbasicauth.model.User;
import com.densoft.springbootbasicauth.repository.RoleRepository;
import com.densoft.springbootbasicauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public void registerUser(User user) {
        boolean isNewUser = user.getId() != null;
        if (isNewUser) {
            Role role = roleRepository.findByName("USER").get();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.addRole(role);
        } else {
            User existingUser = userRepository.findById(user.getId()).get();

            if (user.getPassword().isBlank() || user.getPassword() == null) {
                user.setPassword(existingUser.getPassword());
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

        }
        userRepository.save(user);
    }


    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
    }

}
