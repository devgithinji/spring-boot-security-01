# How to Get Logged-in User's Details with Spring Security

## 1. Display the default username

To show the default username of the currently logged-in user in a view template using Thymeleaf, we can print the value of remoteUser object associated with the current request:

```
<span>[[${#request.remoteUser}]]</span>
```

Or using Thymeleaf Extras for Spring Security:

```
<span sec:authentication="name">Username</span>

```

However, this way always prints the value returned from the getUsername() method in the UserDetails class

## 2. Display any user’s information (first name, last name, fullname…)

Suppose that we want to display the name of the currently logged-in user instead of email. Add a getter method in the UserDetails class as shown below:

```
public class MyUserDetails implements UserDetails {

    private User user;

    public MyUserDetails(User user) {
        this.user = user;
    }

// other override methods from user details

    public String getName() {
        return user.getName();
    }
}
```

Then in the view, you can display full name of the user as follows:

```
<p sec:authentication="principal.name"></p>
```

Here, the principal object is actually a UserDetails object returned by the loadUserByUsername() method - So we can access any properties in this object. For example, if we have first name:

```
<span sec:authentication="principal.firstname">Firstname</span>
```

Note that to use sec:authentication attribute, you must declare this dependency:

```
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
</dependency>
```

And declare an XML namespace:

```
<html xmlns:th="http://www.thymeleaf.org"
    xmlns:sec="https://www.thymeleaf.org/thymeleaf-extras-springsecurity5">

```

## 3. Display user’s assigned roles

To show the assigned roles (authorities) of the currently logged-in user, write the following line of code:

```
<span sec:authentication="principal.authorities">Roles</span>
```

It will call the getAuthorities() method of the UserDetails class.

## 4. Get UserDetails object in Spring Controller

Certainly there will be a case in which we want to get the UserDetails object that represent the currently logged-in user in Java code, e.g. a handler method a Spring controller class. The simplest way is using the @AuthenticationPrincipal annotation as shown in the example below:

```
package net.codejava;
 
import org.springframework.security.core.annotation.AuthenticationPrincipal;
 
@Controller
public class AccountController {
 
    @Autowired
    private UserServices service;
   
    @GetMapping("/account")
    public String viewUserAccountForm(
            @AuthenticationPrincipal ShopmeUserDetails userDetails,
            Model model) {
        String userEmail = userDetails.getUsername();
        User user = service.getByEmail(userEmail);
   
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Account Details");
   
        return "users/account_form";
    }
}
```

Other Examples

```
@Controller
public class SecurityController {
 
    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Authentication authentication) {
        return authentication.getName();
    }
}
```


```
@Controller
public class SecurityController {
 
    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Principal principal) {
        return principal.getName();
    }
}
```

## 5. Get UserDetails object from anywhere in the spring project

The API of the Authentication class is very open so that the framework remains as flexible as possible. Because of this, the Spring Security principal can only be retrieved as an Object and needs to be cast to the correct UserDetails instance:

```
  Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        System.out.println(myUserDetails.getName());
```
