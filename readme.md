# Spring Security Forgot Password Tutorial

## 1. Update Database table and Entity class

First, you need to update the database table that stores the user information (customers in my case) by adding a new column to store a random token key which is used to protect the reset password page:

So add a new column named reset_password_token with data type is varchar(30) because we’ll use a random token string of 30 characters. Then update the corresponding entity class, by declaring a new field named resetPasswordToken:

```
@Entity
@Table(name = "customers")
public class User {
 
    // other fields...
   
    @Column(name = "reset_password_token")
    private String resetPasswordToken;
   
    // getters and setters...
}
```

When a customer submits a request to reset password, the application will generate a random reset password token, which will be used in the forgot password email and reset password form to make sure that only the user who got the email can change password.

## 2. Update Repository Interface and Service Class

Next, in the repository layer, declare two new methods in the repository interface as below:

```
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> getUserByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
}

```

The findByEmail() method will be used to check a user’s email when he starts to use the forgot password function. And the findByResetPasswordToken() method will be used to validate the token when the user clicks the change password link in email.

And update the service class as follows:

```
@Transactional
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void updateResetPasswordToken(String token, String email) throws UsernameNotFoundException {
        User user = userRepository.getUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("no user found"));
        user.setResetPasswordToken(token);
        userRepository.save(user);
    }

    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token).orElseThrow(() -> new UsernameNotFoundException("no user found"));
    }

    public void updatePassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }
}
```

Here we implement 3 methods:

* updateResetPasswordToken(): sets value for the field resetPasswordToken of a user found by the given email – and persist change to the database. Else throw UserNotFoundException (you may need to create this exception class).
* getByResetPasswordToken(): finds a user by the given reset password token. Suppose that the random token is unique.
* updatePassword(): sets new password for the user (using BCrypt password encoding) and nullifies the reset password token.

These methods will be used by a Spring MVC controller class, which you’ll see in the sections below.


## 3. Update Login Page

Next, update the login page by adding a hyperlink that allows the user to use the forgot password function:

```
<a th:href="/@{/forgot_password}">Forgot your password?</a>
```


## 4. Add Spring Mail dependency and Configure JavaMail properties

```
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```

I will be using Gmail’s SMTP server for sending emails, so specify the following properties in the Spring application configuration file (below is in yml format):

```
spring.mail.host=smtp.gmail.com
spring.mail.username=<..email..>
spring.mail.password=<apppassword>
spring.mail.port=587
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```


## 5. Create Forgot Password Controller class

```
@Controller
public class ForgotPasswordController {
    @Autowired
    private JavaMailSender mailSender;
   
    @Autowired
    private CustomerServices customerService;
   
    @GetMapping("/forgot_password")
    public String showForgotPasswordForm() {
 
    }
 
    @PostMapping("/forgot_password")
    public String processForgotPassword() {
    }
   
    public void sendEmail(){
 
    }  
   
   
    @GetMapping("/reset_password")
    public String showResetPasswordForm() {
 
    }
   
    @PostMapping("/reset_password")
    public String processResetPassword() {
 
    }
}
```

As you can see, we tell Spring to autowire an instance of JavaMailSender into this controller in order to send email containing the reset password link to the user. You’ll see the detailed code for each handler method in the next sections.


## 6. Code Forgot Password Page

When a user clicks the Forgot your password link in the home page, the application will show the forgot password page that requires the user enters email in order to receive reset password link. So update code of the handler method as follows:

```
@GetMapping("/forgot_password")
public String showForgotPasswordForm() {
    return "forgot_password_form";
}
```

This handler method simply returns the view name forgot_password_form, so create a new HTML file with the same name, under src/main/resources/templates. Make sure that it contains the following code:

```
<div>
    <h2>Forgot Password</h2>
</div>
   
<div th:if="${error != null}">
    <p class="text-danger">[[${error}]]</p>
</div>
<div th:if="${message != null}">
    <p class="text-warning">[[${message}]]</p>
</div>
     
<form th:action="@{/forgot_password}" method="post" style="max-width: 420px; margin: 0 auto;">
<div class="border border-secondary rounded p-3">
    <div>
        <p>We will be sending a reset password link to your email.</p>
    </div>
    <div>
        <p>
            <input type="email" name="email" class="form-control" placeholder="Enter your e-mail" required autofocus/>
        </p>     
        <p class="text-center">
            <input type="submit" value="Send" class="btn btn-primary" />
        </p>
    </div>
</div>
</form>
```


## 7. Code to Send Reset Password Email

Next, code another handler method in the controller class to process the forgot password form as below:

```
@PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String token = RandomString.make(30);

        try {

            userService.updateResetPasswordToken(token, email);
            String resetPasswordLink = Utility.getStaticSiteUrl(request) + "/reset_password?token=" + token;
            sendEmail(email, resetPasswordLink);
            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");

        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", e.getMessage());
        } catch (UnsupportedEncodingException | MessagingException e) {
            model.addAttribute("error", "Error while sending email");
        }
        return "forgot_password_form";
    }
```


As you can see, a random String is generated as reset password token using the RandomString class from the net.bytebuddy.utility package. ByteBuddy is a library comes with Spring Boot so you don’t have to declare any new dependency.

The it updates the reset password token field of the user found with the given email, otherwise throws an exception whose message will be shown up in the forgot password form.

Then it generates a reset password link containing the random token as a URL parameter in the following form:

```
http://contextpath/reset_password?token=random_token
```

And below is code of the sendEmail() method:

```
 public void sendEmail(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("contact@test.com", "support");
        helper.setTo(recipientEmail);
        String subject = "Here's the link to reset your password";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + link + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }
```


## 8. Code to Show Reset Password page

Next, implement the following handler method in the controller class to display the reset password page when the user clicks the Change password link in the email:

```
@GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("token", token);
        if(user == null){
            model.addAttribute("message", "Invalid Token");
            return "message";
        }
        return "reset_password_form";
    }
```


Here, the application checks for the validity of the token in the URL, to make sure that only the user who got the real email can change password. In case the token not found in the database, it will display the message “Invalid Token”;

And create the reset_password_form.html file containing the following code:

```
<div>
    <h2>Reset Your Password</h2>
</div>
     
<form th:action="@{/reset_password}" method="post" style="max-width: 350px; margin: 0 auto;">
    <input type="hidden" name="token" th:value="${token}" />
<div class="border border-secondary rounded p-3">
    <div>
        <p>
            <input type="password" name="password" id="password" class="form-control"
                placeholder="Enter your new password" required autofocus />
        </p>     
        <p>
            <input type="password" class="form-control" placeholder="Confirm your new password"
                required oninput="checkPasswordMatch(this);" />
        </p>     
        <p class="text-center">
            <input type="submit" value="Change Password" class="btn btn-primary" />
        </p>
    </div>
</div>
</form>
```


## 9. Code to Handle Reset Password form

Next, implement the handler method to process password reset as follows:

```
@PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");

        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "message";
        } else {
            userService.updatePassword(user, password);

            model.addAttribute("message", "You have successfully changed your password.");
        }

        return "message";
    }
```
