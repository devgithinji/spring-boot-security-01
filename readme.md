# Spring Security Password Expiration Tutorial


Suppose that you have an existing Spring Boot application in which authentication is already implemented, and now it needs to be updated for implementing password expiration functionality, with the following requirements:

- Users must change password after 30 days since the last time they updated their passwords.
- The application will require a user to change his password when it found that the password expires, during the time he’s using the website (including upon successful login).


## 1. Update Database table and Entity class

Suppose that the user information is stored in a table named customers, so you need to alter the table for adding a new column called password_changed_time.

The type of the column is DATETIME, so it will store time that is precise to seconds. Your application should update value for this column somehow, e.g. upon user registration or activation.

Then update the corresponding entity class as follows:

```
@Entity
@Table(name = "customers")
public class Customer {
    private static final long PASSWORD_EXPIRATION_TIME
            = 30L * 24L * 60L * 60L * 1000L;    // 30 days
   
    @Column(name = "password_changed_time")
    private Date passwordChangedTime;  
   
    public boolean isPasswordExpired() {
        if (this.passwordChangedTime == null) return false;
     
        long currentTime = System.currentTimeMillis();
        long lastChangedTime = this.passwordChangedTime.getTime();
     
        return currentTime > lastChangedTime + PASSWORD_EXPIRATION_TIME;
    }
 
    // other fields, getters and setters are not shown 
}
```

Here, we declare a constant of type long to represent the number of milliseconds in 30 days (password expiration time). The field passwordChangedTime maps to the corresponding column in the database table, and the isPasswordExpired() method is used to check whether a user’s password expires or not.


## 2. Update User Service Class

Next, update the user business class (CustomerServices in my case) to implement a method for updating password of a customer, as follows:

```
@Service
@Transactional
public class CustomerServices {
 
    @Autowired CustomerRepository customerRepo;
    @Autowired PasswordEncoder passwordEncoder;
   
    public void changePassword(Customer customer, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        customer.setPassword(encodedPassword);
     
        customer.setPasswordChangedTime(new Date());
     
        customerRepo.save(customer);
    }
}
```

The changePassword() method will be used by a controller when a user changes his password. And as you notice, the password changed time value is set to the current datetime so the user will have next 30 days until the newly changed password expires.


## 3. Code Password Expiration Filter

Next, we need to code a filter class to intercept all requests coming the application, in order to check if the currently logged in user has expired password or not, as follows:

```
@Component
public class PasswordExpirationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (isUrlExcluded(httpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        System.out.println("PasswordExpirationFilter");
        User user = getLoggedInUser();

        if (user != null && user.isPasswordExpired()) {
            System.out.println(user);
            showChangePasswordPage(servletResponse, httpServletRequest, user);
        } else {
          filterChain.doFilter(httpServletRequest,servletResponse);
        }
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = null;
        if (authentication != null) {
            principal = authentication.getPrincipal();
        }

        if (principal != null && principal instanceof MyUserDetails) {
            MyUserDetails userDetails = (MyUserDetails) principal;
            return userDetails.getUser();
        }
        return null;
    }

    private boolean isUrlExcluded(HttpServletRequest httpRequest)
            throws IOException, ServletException {
        String url = httpRequest.getRequestURL().toString();

        if (url.endsWith(".css") || url.endsWith(".png") || url.endsWith(".js")
                || url.endsWith("/change_password")) {
            return true;
        }

        return false;
    }

    private void showChangePasswordPage(ServletResponse response,
                                        HttpServletRequest httpRequest, User user) throws IOException {
        System.out.println("User: " + user.getName() + " - Password Expired:");
        System.out.println("Last time password changed: " + user.getPasswordChangedTime());
        System.out.println("Current time: " + new Date());

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String redirectURL = httpRequest.getContextPath() + "/change_password";
        httpResponse.sendRedirect(redirectURL);
    }
}

```


## 4. Code Password Controller Class

Next, create a new Spring MVC controller class to show the change password page as well as processing password change, with some initial code as follows:

```
@Controller
public class PasswordController {
 
    @Autowired
    private CustomerServices customerService;
   
    @Autowired
    private PasswordEncoder passwordEncoder;
   
    @GetMapping("/change_password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("pageTitle", "Change Expired Password");  
        return "change_password";
    }
   
    @PostMapping("/change_password")
    public String processChangePassword() {
        // implement later...
     
    }  
   
}
```

As you can see, the first handler method simply returns the logical view name change_password which will be resolved to a corresponding HTML page which is described next.


## 5. Code Change Password Page

Next, create the change_password.html page with the following code in the body of the document:

```
<div>
    <h2>Change Your Expired Password</h2>
</div>
     
<form th:action="@{/change_password}" method="post" style="max-width: 350px; margin: 0 auto;">
<div class="border border-secondary rounded p-3">
    <div th:if="${message != null}" class="m-3">
        <p class="text-danger">[[${message}]]</p>
    </div>   
    <div>
        <p>
            <input type="password" name="oldPassword" class="form-control"
                    placeholder="Old Password" required autofocus />
        </p>   
        <p>
            <input type="password" name="newPassword" id="newPassword" class="form-control"
                    placeholder="New password" required />
        </p>     
        <p>
            <input type="password" class="form-control" placeholder="Confirm new password"
                    required oninput="checkPasswordMatch(this);" />
        </p>     
        <p class="text-center">
            <input type="submit" value="Change Password" class="btn btn-primary" />
        </p>
    </div>
</div>
</form>
```

You can also notice that I use HTML 5, Thymeleaf, Bootstrap and jQuery. And implement the second handler method for updating user’s password upon submission of the change password page, as follows:

```
@PostMapping("/change_password")
public String processChangePassword(HttpServletRequest request, HttpServletResponse response,
        Model model, RedirectAttributes ra,
        @AuthenticationPrincipal Authentication authentication) throws ServletException {
    CustomerUserDetails userDetails = (CustomerUserDetails) authentication.getPrincipal();
    Customer customer = userDetails.getCustomer();
   
    String oldPassword = request.getParameter("oldPassword");
    String newPassword = request.getParameter("newPassword");
   
    model.addAttribute("pageTitle", "Change Expired Password");
   
    if (oldPassword.equals(newPassword)) {
        model.addAttribute("message", "Your new password must be different than the old one.");
     
        return "change_password";
    }
   
    if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
        model.addAttribute("message", "Your old password is incorrect.");      
        return "change_password";
     
    } else {
        customerService.changePassword(customer, newPassword);
        request.logout();
        ra.addFlashAttribute("message", "You have changed your password successfully. "
                + "Please login again.");
     
        return "redirect:/login";      
    }
   
}
```


Here, it gets a UserDetails object that represent the authenticated user. Then it checks to make sure that the new password is different than the old one, and the old password is correct. If both conditions are met, it updates the user’s password with new one and logs the user out – then showing the login page.

Next, we’re ready to test the password expiration feature.


## 6. Test Password Expiration Function

Use a database tool like MySQL Workbench to update changed password time of a user to a value which is older than 30 days from the current date. Then try to login with that user’s email, you should see the application asks for changing password
