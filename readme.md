# Spring Security Redirect Users After Login Based on Roles


## Implement hasRole method in User class

The application will need to check if the currently logged-in user has a specific role or not. So code the **hasRole()**method in the User entity class as follows:

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
    @JsonProperty(access = WRITE_ONLY)
    private String password;
    private boolean enabled;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(String roleName){
        return this.roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

}
```

The hasRole() method will return true if the user is assigned with the specified role, or false otherwise. And also update your custom UserDetails class – adding the hasRole() method as shown below:


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

    public boolean hasRole(String roleName){
        return user.hasRole(roleName);
    }
}
```


## Code Authentication Success Handler

Next, code a class that extends an implementation of AuthenticationSuccessHandler, such as SavedRequestAwareAuthenticationSuccessHander like the following code:

You know, the onAuthenticationSuccess() method will be invoked by Spring Security upon user’s successful login. So it’s very logically to put the redirection code in this method, for redirecting the authenticated users based on their roles.

```
@Configuration
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        String redirectUrl = request.getContextPath();
        if (myUserDetails.hasRole("ADMIN")) {
            redirectUrl = "admin-home";
        } else if (myUserDetails.hasRole("CREATOR")) {
            redirectUrl = "creator-home";
        } else if (myUserDetails.hasRole("EDITOR")) {
            redirectUrl = "editor-home";
        }

        response.sendRedirect(redirectUrl);
    }
}

```


## Configure Spring Security to Use Success Handler


And update the Spring Security configuration class to use the authentication success handler class as follows:

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;


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
                .antMatchers("/creator-home").hasAuthority("CREATOR")
                .antMatchers("/editor-home").hasAuthority("EDITOR")
                .antMatchers("/new").hasAnyAuthority( "ADMIN", "CREATOR")
                .antMatchers("/edit/**").hasAnyAuthority("ADMIN","EDITOR")
                .antMatchers("/delete/**","/admin-home").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/login") // custom login url
                .usernameParameter("u") // custom login form username name
                .passwordParameter("p") //custom login form password name
                .successHandler(loginSuccessHandler)
                .permitAll()
//                .failureUrl("/loginerror") //custom error login redirection page
//                .defaultSuccessUrl("/loginsuccess") //custom success login redirection page
                .and().logout().permitAll();
//                .logoutSuccessUrl("/logoutsuccess"); //custom logout redirection page
    }
}

```



## Update View Layer

This part is optional. If the role-based view pages (editor home, admin dashboard, etc) do not have corresponding handler methods in controller layer, you can configure the view name resolution in a Spring MVC configuration as follows:

```
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//      view resolvers used instead of writing controller methods to map requests
        registry.addViewController("/403").setViewName("403");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/admin-home").setViewName("admin_home");
        registry.addViewController("/creator-home").setViewName("creator_home");
        registry.addViewController("/editor-home").setViewName("editor_home");
    }
}

```
