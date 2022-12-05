package com.densoft.springbootbasicauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//      view resolvers used instead of writing controller methods to map requests
        registry.addViewController("/403").setViewName("403");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/admin-home").setViewName("admin_home");
        registry.addViewController("/creator-home").setViewName("creator_home");
        registry.addViewController("/editor-home").setViewName("editor_home");
    }
}
