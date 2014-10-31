package net.steveperkins.fitnessjiffy.config;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.service.ReportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.sql.Timestamp;
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
        @Nonnull
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        @Nonnull
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        @Nonnull
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        @Nonnull
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
            final User user = userRepository.findOne(userDTO.getId());

            // Update the last-login time
            final long currentTime = System.currentTimeMillis();
            user.setLastUpdatedTime(new Timestamp(currentTime));
            userRepository.save(user);

            // Schedule a ReportData update
            final Date today = new Date(currentTime);  // TODO:  need to account for time zone?
            reportDataService.updateUserFromDate(userDTO.getId(), today);
        }

    }

    @Autowired
    ReportDataService reportDataService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private Converter<User, UserDTO> userDTOConverter;

    /**
     * This override is necessary to work around a Spring Security issue introduced with Spring Boot 1.1.6.RELEASE, and
     * is supposed to become unnecessary when version 1.1.7 comes out.
     *
     * See https://github.com/spring-projects/spring-boot/issues/1532
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

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
        daoAuthenticationProvider.setUserDetailsService(new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
                final User user = userRepository.findByEmailEquals(username);
                final UserDTO userDTO = userDTOConverter.convert(user);
                return (user == null) ? null : new SpringUserDetails(userDTO, user.getPasswordHash());
            }
        });
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return daoAuthenticationProvider;
    }

}
