# Spring Security Logout Success Handler Example

The logout success handler helps to do the following

* Log the logged-out user (for auditing purpose)
* Display different logout success pages based on user’s roles
* Clean up resources that are used temporarily by the user
* Any custom logics upon successful logout

![spring_security_logout_success_handler](https://user-images.githubusercontent.com/34215705/205951149-6cedde3b-9da5-4c62-82d3-1172578bc498.png)


## 1. Simple Logout Success Handler Example

The simplest way to use a logout success handler is create an anonymous class of type LogoutSuccessHandler as argument to the method logoutSuccessHandler() of the LogoutConfigurer class, as shown below:

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
    ...
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        ...
        .formLogin()
            .loginPage("/login")
            .usernameParameter("email")
            .permitAll()
        .and()
        .logout()
            .logoutSuccessHandler(new LogoutSuccessHandler() {
 
                @Override
                public void onLogoutSuccess(HttpServletRequest request,
                            HttpServletResponse response, Authentication authentication)
                        throws IOException, ServletException {
                    CustomerUserDetails userDetails = (CustomerUserDetails) authentication.getPrincipal();
                    String username = userDetails.getUsername();
 
                    System.out.println("The user " + username + " has logged out.");
 
                    response.sendRedirect(request.getContextPath());
                }
            })
            .permitAll();
        ...
    }
   
 
}
```


As you can see, the onLogoutSuccess() method will be invoked by Spring Security upon successful logout of a user. In this example, we just get username of the logged-out user and print it to the standard output, and redirect the user to the application’s context path.

It’s recommended to use a subtype of LogoutSuccessHandler like SimpleUrlLogoutSuccessHandler which handles the navigation on logout automatically. For example:

```
.logout()
    .logoutSuccessHandler(new SimpleUrlLogoutSuccessHandler() {
     
        @Override
        public void onLogoutSuccess(HttpServletRequest request,
                    HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
         
            CustomerUserDetails userDetails = (CustomerUserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
         
            System.out.println("The user " + username + " has logged out.");
         
            super.onLogoutSuccess(request, response, authentication);
        }
    })
    .permitAll()
```

Spring Security also provides the HttpStatusReturningLogoutSuccessHandler which returns an HTTP status code instead of redirection, which is useful for RESTful webservices.


## 2. Advanced Logout Success Handler Example

In case the Logout success handler needs to depend on a business/service class to perform the custom logics, we need to create a separate class as follows:

```
@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
 
    @Autowired
    private CustomerServices customerService;
 
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
     
        CustomerUserDetails userDetails = (CustomerUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
     
        Customer customer = customerService.getCustomerByEmail(username);
     
        // process logics with customer
     
        super.onLogoutSuccess(request, response, authentication);
    }  
}
```


Here, the @Component annotation tells Spring framework to manage instances of this class as beans. And the @Autowired annotation tells Spring framework to inject an instance of the business class into this handler class.

Then configure the Spring Security configuration class like this:

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
    ...
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        ...
        .formLogin()
            .loginPage("/login")
            .usernameParameter("email")
            .permitAll()
        .and()
        .logout()
            .logoutSuccessHandler(logoutSuccessHandler)
            .permitAll();
        ...
    }
   
    @Autowired
    private CustomLogoutSuccessHandler logoutSuccessHandler;   
 
}
```
