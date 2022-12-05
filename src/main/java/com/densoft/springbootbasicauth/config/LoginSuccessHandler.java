package com.densoft.springbootbasicauth.config;

import com.densoft.springbootbasicauth.auth.MyUserDetails;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        String redirectUrl = request.getContextPath();
        if (myUserDetails.hasRole("ADMIN")) {
            redirectUrl = "admin-home";
        } else if (myUserDetails.hasRole("CREATOR")) {
            redirectUrl = "creator-home";
        } else if (myUserDetails.hasRole("EDITOR")) {
            redirectUrl = "editor-home";
        }

        response.sendRedirect(redirectUrl);
    }
}
