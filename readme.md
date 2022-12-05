# Spring Boot Security Role-based Authorization Tutorial

Create and Design Tables

For role-based authorization with credentials and authorities stored in database, we have to create the following 3 tables:

![](/home/dennis/Documents/users_and_roles_relationship.png "user roles and relationship")

You can exceute the following mysql script

```
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `password` varchar(64) NOT NULL,
  `enabled` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
);

CREATE TABLE `roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `users_roles` (
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  KEY `user_fk_idx` (`user_id`),
  KEY `role_fk_idx` (`role_id`),
  CONSTRAINT `role_fk` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `user_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);


INSERT INTO `users` (`email`,`name`, `password`, `enabled`) VALUES ('patrick@gmail.com','patrick', '$2a$10$qrqCXjqaXLt3JnKgATZ1p.fmhwjEZ5A5EfDaJsqKO5o4t.id9ccVO', '1');
INSERT INTO `users` (`email`,`name`, `password`, `enabled`) VALUES ('alex@gmail.com','alex', '$2a$10$qrqCXjqaXLt3JnKgATZ1p.fmhwjEZ5A5EfDaJsqKO5o4t.id9ccVO', '1');
INSERT INTO `users` (`email`,`name`, `password`, `enabled`) VALUES ('john@gmail.com','john', '$2a$10$qrqCXjqaXLt3JnKgATZ1p.fmhwjEZ5A5EfDaJsqKO5o4t.id9ccVO', '1');
INSERT INTO `users` (`email`,`name`, `password`, `enabled`) VALUES ('namhm@gmail.com','namhm', '$2a$10$qrqCXjqaXLt3JnKgATZ1p.fmhwjEZ5A5EfDaJsqKO5o4t.id9ccVO', '1');
INSERT INTO `users` (`email`,`name`, `password`, `enabled`) VALUES ('admin@gmail.com','admin', '$2a$10$qrqCXjqaXLt3JnKgATZ1p.fmhwjEZ5A5EfDaJsqKO5o4t.id9ccVO', '1');

INSERT INTO `roles` (`name`) VALUES ('USER');
INSERT INTO `roles` (`name`) VALUES ('CREATOR');
INSERT INTO `roles` (`name`) VALUES ('EDITOR');
INSERT INTO `roles` (`name`) VALUES ('ADMIN');

INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (1, 1);
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (2, 2);
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (3, 3);
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (4, 2);
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (4, 3);
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (5, 4);
```

#### Code the entity classes

#### Role Entity class

```
@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
}
```

#### User entity class

```
@Entity
@Table(name = "users")
@Data
public class User {
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
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

}
```

#### User Details Implementation

```
public class MyUserDetails implements UserDetails {

    private User user;

    public MyUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return user.getName();
    }
}
```

#### Configure Authorization

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/new").hasAnyAuthority( "ADMIN", "CREATOR")
                .antMatchers("/edit/**").hasAnyAuthority("ADMIN","EDITOR")
                .antMatchers("/delete/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/login") // custom login url
                .usernameParameter("u") // custom login form username name
                .passwordParameter("p") //custom login form password name
                .permitAll()
//                .failureUrl("/loginerror") //custom error login redirection page
//                .defaultSuccessUrl("/loginsuccess") //custom success login redirection page
                .and().logout().permitAll();
//                .logoutSuccessUrl("/logoutsuccess"); //custom logout redirection page
    }
}
```

#### Implement Authorization using Thymeleaf integration

To use Thymeleaf with spring security for the view make sure you declare the relavant

```
<html xmlns:th="http://www.thymeleaf.org"
    xmlns:sec="https://www.thymeleaf.org/thymeleaf-extras-springsecurity5">
```

To display username of a logged in user use the following code:

`<span sec:authentication="name">Username</span>`

To show all the roles (authorities/permissions/rights) of the current user, use the following code:

`<span sec:authentication="principal.authorities">Roles</span>`

To show a section that is for only authenticated users, use the following code:

```
<div sec:authorize="isAuthenticated()">
        Welcome <b><span sec:authentication="name">name</span></b>
        <p sec:authentication="principal.name"></p>
         
        <i><span sec:authentication="principal.authorities">Roles</span></i>
    </div>
```

The users with role EDITOR or ADMIN can see the links to edit/update products, thus the following code:

```
<div sec:authorize="hasAnyAuthority('ADMIN', 'EDITOR')">
    <a th:href="/@{'/edit/' + ${product.id}}">Edit</a>
</div>
```

```
<div sec:authorize="hasAuthority('ADMIN')">
    <a th:href="/@{'/delete/' + ${product.id}}">Delete</a>
</div>
```
