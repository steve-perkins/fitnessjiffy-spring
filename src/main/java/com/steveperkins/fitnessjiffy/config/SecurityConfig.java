package com.steveperkins.fitnessjiffy.config;

import com.steveperkins.fitnessjiffy.domain.User;
import com.steveperkins.fitnessjiffy.dto.UserDTO;
import com.steveperkins.fitnessjiffy.dto.converter.UserToUserDTO;
import com.steveperkins.fitnessjiffy.repository.UserRepository;
import com.steveperkins.fitnessjiffy.service.ReportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final class SpringUserDetails implements UserDetails {

        private final UserDTO userDTO;
        private final String password;

        public SpringUserDetails(
            @Nonnull final UserDTO userDTO,
            @Nonnull final String password
        ) {
            this.userDTO = userDTO;
            this.password = password;
        }

        @Override
        @Nonnull
        public String getUsername() {
            return this.userDTO.getEmail();
        }

        @Override
        @Nullable
        public String getPassword() {
            return this.password;
        }

        @Nullable
        public UserDTO getUserDTO() {
            return this.userDTO;
        }

        @Override
        @Nonnull
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return new HashSet<>();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public final class LoginListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

        @Override
        public void onApplicationEvent(final InteractiveAuthenticationSuccessEvent event) {
            // Retrieve the user
            final SpringUserDetails userDetails = (SpringUserDetails) event.getAuthentication().getPrincipal();
            final UserDTO userDTO = userDetails.getUserDTO();
            final User user = userRepository.findById(userDTO.getId()).orElse(null);

            // Schedule a ReportData update
            final Date lastUpdateDate = new Date(user.getLastUpdatedTime().getTime());
            reportDataService.updateUserFromDate(user, lastUpdateDate);
        }

    }

    @Autowired
    ReportDataService reportDataService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserToUserDTO userDTOConverter;

    /** This bean must be manually declared, since Spring's autoscanning apparently won't find a @Component-annotated inner class. */
    @Bean
    public LoginListener loginListener() {
        return new LoginListener();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/static/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(buildDaoAuthenticationProvider());
    }

    @Nonnull
    private DaoAuthenticationProvider buildDaoAuthenticationProvider() {
        final DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(username -> {
            final User user = userRepository.findByEmailEquals(username);
            final UserDTO userDTO = userDTOConverter.convert(user);
            return (user == null || userDTO == null) ? null : new SpringUserDetails(userDTO, user.getPasswordHash());
        });
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return daoAuthenticationProvider;
    }

}
