package com.densoft.springbootbasicauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //    an instance of data source is created and injected by the Spring boot framework
    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select username, password, enabled from users where username =?")
                .authoritiesByUsernameQuery("select username, role from users where username =?");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/new").hasAnyRole("USER", "ADMIN")
                .antMatchers("/edit/*", "/delete/*").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/login") // custom login url
                .usernameParameter("u") // custom login form username name
                .passwordParameter("p") //custom login form password name
                .permitAll()
//                .failureUrl("/loginerror") //custom error login redirection page
//                .defaultSuccessUrl("/loginsuccess") //custom success login redirection page
                .and().logout().permitAll();
//                .logoutSuccessUrl("/logoutsuccess"); //custom logout redirection page
    }
}
