# Spring Boot Security Customize Login and Logout

## 1. Customize Login Details

In case you want to use your own login page, specify URL of the custom login page using this code:

```
@Override
protected void configure(HttpSecurity http) throws Exception {
 
    http.formLogin().loginPage("/login");
    ...
}
```

For this you must configure Spring MVC review resolver to map the URL /login with a view name, for example: or alteratively create a controller method to map the login url path

```
package net.codejava;
 
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
 
@Configuration
public class MvcConfig implements WebMvcConfigurer {
 
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
   
        registry.addViewController("/login").setViewName("login");
     
    }
 
}
```

And write code for the customized login page as follows:

```
<!DOCTYPE html>
<html xmlns:th="http:/www.thymeleaf.org">
<head>
<meta charset="ISO-8859-1">
<title>Login - Company ABC</title>
</head>
<body>
<div>
<form th:action="@{/login}" method="post" style="max-width: 400px; margin: 0 auto;">
    <p>
        E-mail: <input type="email" name="username" required />  
    </p>
    <p>
        Password: <input type="password" name="password" required />
    </p>
    <p>
        <input type="submit" value="Login" />
    </p>
</form>
</div>
</body>
```

By default, Spring Security uses the field names username and password, and the action of the form is /login. If you want to use different field names and URL, specify them using Java code as below:

```
http.formLogin()
    .loginPage("/login")
    .usernameParameter("email")
    .passwordParameter("pass")
    .loginProcessingUrl("/doLogin");
```

And update code of the login form accordingly:

```
<form th:action="@{/doLogin}" method="post">
    <p>
        E-mail: <input type="email" name="email" required /> 
    </p>
    <p>
        Password: <input type="password" name="pass" required />
    </p>
    <p>
        <input type="submit" value="Login" />
    </p>
</form>
```

### Login Default Success URL:

Spring Security will redirect the users to the page he has visited prior to login. For example, if a user visits the create new product page that requires authentication, he will be redirected to the login page. And after login succeeded, he will be sent back to the create new product page.

In case you want to show user a separate page after he visits an unsecured page prior to login, specify the default success URL as follows:

```
http.formLogin()
    .defaultSuccessUrl("/login_success");
```

### Login Failure URL:

By default, Spring Security will redirects to /login?error if the user failed to login. If you want to change this behavior, e.g. showing your own page that displays login error message to the user – then specify the custom login error page using this code:

```
http.formLogin()
    .failureUrl("/login_error");
```


### Login Success Forward URL:

If you want to execute some extra code after the user has logged in successfully, e.g. logging or auditing, then specify the success forward URL like this:

```
http.formLogin()
    .successForwardUrl("/login_success_handler");
```

For this to work, you must write a corresponding handler method in the controller class. For example:

```
@Controller
public class AppController {
 
    @PostMapping("/login_success_handler")
    public String loginSuccessHandler() {
        System.out.println("Logging user login success...");
 
        return "index";
    }  
}
```


### Login Failure Forward URL:

```
http.formLogin()
    .failureForwardUrl("/login_failure_handler");
```

And code the handler method in the controller like this:

```
@PostMapping("/login_failure_handler")
public String loginFailureHandler() {
    System.out.println("Login failure handler....");
   
    return "login";
}
```


### Login Success Handler:

Similar to login success forward URL, you can also write an authentication success handler as follows:

```
http.formLogin()
.successHandler(new AuthenticationSuccessHandler() {
   
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
     
        System.out.println("Logged user: " + authentication.getName());
     
        response.sendRedirect("/");
    }
});
```


### Login Failure Handler:

```
http.formLogin()
.failureHandler(new AuthenticationFailureHandler() {
   
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        System.out.println("Login failed");
        System.out.println(exception);
     
        response.sendRedirect("/login_error");
    }
});
```


## 2. Customize Logout Details

Besides login customization, Spring Security also allows programmers to customize the logout process. Basically you code the Logout button like this:


```
<form th:action="@{/logout}" method="post">
    <input type="submit" value="Logout" />
</form>
```


### Logout URL:

By default, Spring Security processes the /logout URL via HTTP POST method. You can configure the HttpSecurity object to change this URL as below:

```
http.logout()
    .logoutUrl("/doLogout");
```


### Logout Success URL:

By default, the user will see the login page after logging out of the application. If you want to show a customized page to the user, specify the logout success URL as follows:

```
http.logout()
    .logoutSuccessUrl("/logout_success");
```

### Logout Success Handler:

If you want to perform extra steps after the user logged out successfully, use a logout success hander like this:

```
http.logout()
.logoutSuccessHandler(new LogoutSuccessHandler() {
   
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication)
            throws IOException, ServletException {
     
        System.out.println("This user logged out: " + authentication.getName());
     
        response.sendRedirect("/logout_success");
    }
});
```


### Use Logout link instead of button:

The reason you should use Logout button because Spring Security automatically generates a security token to prevent CSRF (Cross-Site Request Forgery) attack in the login page. For example:

```
<input type="hidden" name="_csrf" value="8ec12704-5ab6-4f0c-a758-2fc36f2c9368"/>
```

That’s why the logout request must be sent via HTTP POST method. You can disable CSRF prevention to use a logout link (via HTTP GET method) using this code:

```
http.csrf().disable();
```

However, this is not recommended for applications that go on production. So the best way is to hide the logout form and use Javascript for the logout link, as shown in the following example:

```
<form name="logoutForm" th:hidden="true" method="post" th:action="@{/doLogout}">
    <input type="submit" value="Logout" />
</form>
<a href="javascript: logoutForm.submit();">Sign Out</a>
```
