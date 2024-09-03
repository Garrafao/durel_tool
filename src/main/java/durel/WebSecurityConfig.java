package durel;

import durel.utils.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtRequestFilter jwtRequestFilter;

    private final LoginSuccessHandler loginSuccessHandler;

    @Autowired
    public WebSecurityConfig(JwtRequestFilter jwtRequestFilter, LoginSuccessHandler loginSuccessHandler) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    /**
     * Basic Configuration of Spring.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();

        http.
                csrf().disable()
                .exceptionHandling().accessDeniedHandler(new AdminAccessDeniedHandler())
                .and()
                .authorizeRequests()
                .antMatchers("/annotatorInstances/**").hasAnyRole("ADMIN", "CANNOTATOR")
                //.antMatchers("/authenticate").hasAnyRole("ADMIN", "CANNOTATOR")
                .antMatchers("/upload/tutorial").hasRole("ADMIN")
                .antMatchers("/tutorial/delete").hasRole("ADMIN")
                .antMatchers("/uploadTutorial").hasRole("ADMIN")
                .antMatchers("/deleteTutorial").hasRole("ADMIN")
                .antMatchers("/css/**").permitAll()
                .antMatchers("/img/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/pdf/**").permitAll()
                .antMatchers("/",
                        "/login",
                        "/register",
                        "/about",
                        "/privacyPolicy",
                        "/getLocale",
                        "/annotationGuidelines",
                        "/docs/**")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login").successHandler(loginSuccessHandler)
                .and().logout().logoutSuccessUrl("/");

                http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Password encoder that is used by the Authentication Manager.
     * @return password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
