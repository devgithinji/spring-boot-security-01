package com.densoft.springbootbasicauth.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name= "users")
@Data
public class User {
    @Id
    @Column(name= "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
    private boolean enabled;

}
