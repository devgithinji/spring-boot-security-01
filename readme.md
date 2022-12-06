# Spring Security: Prevent User from Going Back to Login Page if Already logged in

A typical scenario is that a user has just logged in to the website and somehow he clicks the Back button in the browser unintentionally (or type the /login URL). Spring Security doesn’t handle this situation, so we need to write a little bit extra code, e.g. redirecting the logged-in user to the homepage in case he accidentally visits the login page again.

Suppose that you configure Spring Security to use a custom login page at the /login URL in the Spring security configuration class as below:

```
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            ...
            .formLogin()
                .loginPage("/login")           
                .permitAll()
            ...
    }
}
```

And to prevent the user from going back to the login page if he already logged in, you need to write a simple handler method for the /login URL in a Spring MVC controller as follows:

```
@Controller
public class AppController {
   
    @GetMapping("/login")
    public String showLoginForm(Model model) {
     
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
 
        return "redirect:/";
    }
}
```
