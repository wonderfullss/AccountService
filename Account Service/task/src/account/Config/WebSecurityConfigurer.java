package account.Config;

import account.Exception.CustomAccessDeniedHandler;
import account.Exception.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@EnableWebSecurity
@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    RestAuthenticationEntryPoint authenticationEntryPoint;

    private final UserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfigurer(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(getEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .csrf().disable()
                .headers().frameOptions().disable()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                .authorizeRequests()
                .mvcMatchers("/api/auth/signup").permitAll()
                .mvcMatchers("/api/auth/changepass").authenticated()
                .antMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT")
                .antMatchers(HttpMethod.POST, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                .antMatchers(HttpMethod.PUT, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                .mvcMatchers(HttpMethod.GET, "/api/admin/user").hasAuthority("ROLE_ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "api/admin/user/access").hasAuthority("ROLE_ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "/api/security/events").hasAuthority("ROLE_AUDITOR")
                .antMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasAuthority("ROLE_ADMINISTRATOR")
                .antMatchers(HttpMethod.PUT, "/api/admin/user/role").hasAuthority("ROLE_ADMINISTRATOR")
                .mvcMatchers("/actuator/shutdown").permitAll();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}