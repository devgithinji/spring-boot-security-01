# Spring Security Authentication Failure Handler Examples

This handler method helps in:

* Log the failed login attempt (for auditing purpose)
* Record the failed login to implement limit failed login attempts feature (to prevent brute force attack)
* Display a custom login failure page
* any custom logics we want to perform upon authentication failure.

We can easily implement that, thanks to the highly customizable and flexible APIs provided by Spring Security. The following diagram explains the process:

![](/home/dennis/Documents/spring_security_login_failure_handler.png)


## 1. Simple Authentication Failure Handler

Suppose that you have an existing Spring Boot application in which Spring Security is used for authentication. The following code snippet shows you the simplest way of implementing an authentication failure handler using an anonymous class:

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
            .failureHandler(new AuthenticationFailureHandler() {
 
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException exception) throws IOException, ServletException {
                    String email = request.getParameter("email");
                    String error = exception.getMessage();
                    System.out.println("A failed login attempt with email: "
                                        + email + ". Reason: " + error);
 
                    String redirectUrl = request.getContextPath() + "/login?error";
                    response.sendRedirect(redirectUrl);
                }
            })
            .permitAll()
            ...
    }
}
```


As you can see, the AuthenticationFailureHandler interface defines the method onAuthenticationFailure() which will be called by Spring Security upon a failed login attempt. The code in this example just logs the information (email and error message) and then redirects the user to the login error page.

However, it is recommended to have the handler class extends the SimpleUrlAuthenticationFailureHandler class which will redirect the user to the default failure URL and also forward the exception object, as shown in the following example:

```
.formLogin()
    .loginPage("/login")
    .usernameParameter("email")
    .failureHandler(new SimpleUrlAuthenticationFailureHandler() {
     
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException exception) throws IOException, ServletException {
            String email = request.getParameter("email");
            String error = exception.getMessage();
            System.out.println("A failed login attempt with email: "
                                + email + ". Reason: " + error);
         
            super.setDefaultFailureUrl("/login?error");
            super.onAuthenticationFailure(request, response, exception);
        }
    })
```

Here, we need to specify the default failure URL, otherwise it will give 404 error. But in the login page we can display the exception message.


## 2. Advanced Authentication Failure Handler

In case the authentication failure handler needs to depend on a business/service class in order to perform the custom logics upon failed login, we should create a separate authentication failure handler class, as shown in the example code below:

```
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
 
    @Autowired
    private CustomerServices customerService;
   
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("email");
     
        String redirectURL = "/login?error&email=" + email;
     
        if (exception.getMessage().contains("OTP")) {
            redirectURL = "/login?otp=true&email=" + email;
        } else {
            Customer customer = customerService.getCustomerByEmail(email);
            if (customer.isOTPRequired()) {
                redirectURL = "/login?otp=true&email=" + email;
            }
        }
     
        super.setDefaultFailureUrl(redirectURL);
     
     
        super.onAuthenticationFailure(request, response, exception);
    }
 
}
```


Here, you see the handler class is annotated with the @Component annotation – so Spring framework will manage instances (beans) of this class. And it depends on an instance of the CustomerService class, which will be injected by Spring framework because the @Autowired annotation is used. And you can see code in the onAuthenticationFailure() callback method needs to use CustomerService.

And update the Spring security configuration class as follows:

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
            .failureHandler(failureHandler)
        ...
    }
   
    @Autowired
    private LoginFailureHandler failureHandler;
}
```
