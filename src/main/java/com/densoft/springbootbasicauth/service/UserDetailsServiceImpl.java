package com.densoft.springbootbasicauth.service;

import com.densoft.springbootbasicauth.auth.MyUserDetails;
import com.densoft.springbootbasicauth.model.User;
import com.densoft.springbootbasicauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("could not find user"));
        return new MyUserDetails(user);
    }
}
