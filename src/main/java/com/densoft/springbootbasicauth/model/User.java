package com.densoft.springbootbasicauth.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    private static final long PASSWORD_EXPIRATION_TIME = 30L * 24L * 60L * 60L * 1000L;    // 30 days

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
    private String password;
    private boolean enabled;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "password_change_time")
    private Date passwordChangedTime;

    public boolean isPasswordExpired() {
        if (this.passwordChangedTime == null) return false;

        long currentTime = System.currentTimeMillis();
        long lastChangedTime = this.passwordChangedTime.getTime();

        return currentTime > lastChangedTime + PASSWORD_EXPIRATION_TIME;
    }
}
