# Spring Security - How to Fix WebSecurityConfigurerAdapter Deprecated

So, why Spring Security deprecates the use of WebSecurityConfigurerAdapter?, and what is the alternative?

Well, it’s because the developers of Spring framework encourage users to move towards a component-based security configuration.

So, instead of extending WebSecurityConfigurerAdapter and overriding methods for configuring HttpSecurity and WebSecurity as in the old way - Now you to declare two beans of type SecurityFilterChain and WebSecurityCustomizer as follows:

```
@Configuration
public class SecurityConfiguration {
   
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
   
    }
   
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
   
    }
   
}
```

Config without the WebSecurityConfigurerAdapter

```
@Configuration
public class WebSecurityConfig {

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;


    public DaoAuthenticationProvider daoAuthenticationProvider() {
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


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authenticationProvider(daoAuthenticationProvider())
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/creator-home").hasAuthority("CREATOR")
                .antMatchers("/editor-home").hasAuthority("EDITOR")
                .antMatchers("/new").hasAnyAuthority("ADMIN", "CREATOR")
                .antMatchers("/edit/**").hasAnyAuthority("ADMIN", "EDITOR")
                .antMatchers("/delete/**", "/admin-home").hasAuthority("ADMIN")
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


        return httpSecurity.build();
    }
}
```

### Declare a bean of type AuthenticationManager:

```
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
}
```

### Declare a bean of type AuthenticationProvider:

In case you need to expose a bean of type AuthenticationProvider, such as DaoAuthenticationProvider, use the following code:

```
 @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
```

and specify this authentication provider for HttpSecurity in the code of SecurityFilterChain as follows:

`http.authenticationProvider(authenticationProvider());`
