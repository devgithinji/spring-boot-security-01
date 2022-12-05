package com.densoft.springbootbasicauth.service;

import com.densoft.springbootbasicauth.model.Role;
import com.densoft.springbootbasicauth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;


    public List<Role> getRoles(){
        return roleRepository.findAll();
    }
}
