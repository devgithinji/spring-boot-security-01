# Spring Security Authentication Success Handler Examples

This comes in handy when you want to run methods after successful login example

* Log user’s information (for auditing purpose)
* Request to change password if expired, or request to update user’s details
* Clear previous failed login attempts (for limit login attempts functionality)
* Clear One-Time Password (for OTP functionality)
* any custom logics you want to execute after successful authentication

![login success image](../../Documents/spring_security_login_success_handler.png "login success")


## 1. Simple Authentication Success Handler

In this way, we create an anonymous class of type AuthenticationSuccessHandler as parameter for the successHandler() method of a FormLoginConfigurer class in a Spring security configuration class, as below:

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
                .successHandler(new AuthenticationSuccessHandler() {
 
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                            Authentication authentication) throws IOException, ServletException {
                        // run custom logics upon successful login
                    }
                })
            ...
    }
 
}
```


The callback method onAuthenticationSuccess() will be invoked by Spring Security right after a user has logged in successfully to the application.

This approach is suitable for simple use case, e.g. logging information. For example:

```
.formLogin()
    .loginPage("/login")
    .usernameParameter("email")
    .permitAll()
    .successHandler(new AuthenticationSuccessHandler() {
     
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws IOException, ServletException {
            // run custom logics upon successful login
         
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
         
            System.out.println("The user " + username + " has logged in.");
         
            response.sendRedirect(request.getContextPath());
        }
    })
```

It’s recommended to extend the SavedRequestAwareAuthenticationSuccessHandler class which will automatically redirect the user to the secured page prior to login. For example:

```
successHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // run custom logics upon successful login
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        System.out.println("The user " + username + " has logged in.");
     
        super.onAuthenticationSuccess(request, response, authentication);
    }                  
})
```

It’s better to use this implementation because Spring Security saved the URL prior to login and redirect the user back to that URL upon successful authentication.


## 2. Advanced Authentication Success Handler


In case the authentication success handler class needs to use another business class (a dependency) to perform the custom logics, we need to configure spring security differently.

First, create a separate handler class that extends SavedRequestAwareAuthenticationSuccessHandler class as follows:

```
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
 
    @Autowired
    private CustomerServices customerService;
   
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
 
        CustomerUserDetails customerDetails = (CustomerUserDetails) authentication.getPrincipal();
        Customer customer = customerDetails.getCustomer();
     
        if (customer.isOTPRequired()) {
            customerService.clearOTP(customer);
        }
     
        super.onAuthenticationSuccess(request, response, authentication);
    }
 
}
```


Here, the @Component annotation is used so its instances will be managed by Spring framework and injectable into other components if needed. And as you can see, this handler depends on CustomerService class to perform the custom logics in the callback method onAuthenticationSuccess().

And in the Spring Security configuration class, we need to autowire an instance of the handler class as follows:


```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
    ...
   
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .formLogin()
                    .loginPage("/login")
                    .usernameParameter("email")
                    .permitAll()
                    .successHandler(loginSuccessHandler)
        ...
    }
 
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
 
}
```
