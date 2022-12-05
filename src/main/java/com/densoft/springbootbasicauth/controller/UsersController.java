package com.densoft.springbootbasicauth.controller;

import com.densoft.springbootbasicauth.model.User;
import com.densoft.springbootbasicauth.service.RoleService;
import com.densoft.springbootbasicauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping
    public String showUsers(Model model) {
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("roles", roleService.getRoles());
        return "users";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model) {
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("user", userService.getUser(id));
        return "add_new_user";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.registerUser(user);
        return "redirect:/users";
    }

}
