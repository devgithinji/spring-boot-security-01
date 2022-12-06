# Spring Security Remember Me - How to Add Remember Login Function to Existing Spring Boot Web Application

## 1. Update Custom Login Page

If you’re using a [custom login page](https://www.codejava.net/frameworks/spring-boot/spring-security-custom-login-page), you need to add the following HTML code to the login HTML file, for displaying the “Remember Me” option:

```
<input type="checkbox" name="remember-me" /> Remember Me
```

Note that the name of the checkbox must be remember-me which is required by Spring Security. The login page will look something like this:

![](/home/dennis/Documents/Custom_Login_Page_with_Remember_Me_option.png "Custom Login page remeber me")

In case the default login page is used, you can skip this step, as Spring Security will generate code for the default login page, which looks like below:

### 2. Implement Remember Me function with Cookies only (Hash-based Token)

The simplest way to add Remember login function to an existing Spring Boot web application is putting a call  **rememberMe** () in a security configuration class like this:

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/users").authenticated()
            .anyRequest().permitAll()
            .and()
            .formLogin()
                .loginPage("/login") // custom login page
                .usernameParameter("email")
                .defaultSuccessUrl("/users")
                .permitAll()
            .and()
            .rememberMe()
            .and()
            ...
    }
   
}
```

In this approach, an additional cookie will be created in the user’s web browser, for storing the user’s credentials – besides the session cookie named JSESSIONID:

![](/home/dennis/Documents/Remember_me_Cookie.png)

This new cookie named remember-me, which stores username, password and expiration time in base64 encoding. A private key is used to prevent modification of the remember-me token, and [username, password, private key] are hashed using MD5 algorithm.

The default expiration time is 14 days. You can override this value in the configuration class like this:

```
.rememberMe().tokenValiditySeconds(7 * 24 * 60 * 60) // expiration time: 7 days
```

Also note that, by default, the remember-me cookie won’t survive when the application restarted. That means when the application restarts, all previous cookies become invalid and the user must login manually. You can override this default behavior by supplying a fixed key like this:

It’s because by default, Spring Security supplies a random key at application’s startup. So if you fix the key, remember-me cookies are still valid until expire.

```
.rememberMe()
    .tokenValiditySeconds(7 * 24 * 60 * 60) // expiration time: 7 days
    .key("AbcdefghiJklmNoPqRstUvXyz")   // cookies will survive if restarted

```


## 3. Implement Remember Me function with Database (Persistent Token)

The second approach for implementing Remember Login function in a Spring Boot web application is using persistent token, which stores user’s credentials in database – besides a simpler remember-me cookie in the user’s web browser.

To implement the Remember me feature with database, you need to create a new table named persistent_logins using the following SQL script (MySQL):

```
CREATE TABLE `persistent_logins` (
  `username` VARCHAR(64) NOT NULL,
  `series` VARCHAR(64) NOT NULL,
  `token` VARCHAR(64) NOT NULL,
  `last_used` TIMESTAMP NOT NULL,
  PRIMARY KEY (`series`));
```

Then update the Spring security configuration class as follows:

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            ...
            .and()
            .formLogin()
                .loginPage("/login") // custom login page
                .permitAll()
            .and()
            .rememberMe().tokenRepository(persistentTokenRepository())
            ...
    }
   
 
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepo = new JdbcTokenRepositoryImpl();
        tokenRepo.setDataSource(dataSource);
        return tokenRepo;
    }
}
```
