package com.densoft.springbootbasicauth.auth;

import com.densoft.springbootbasicauth.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

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
