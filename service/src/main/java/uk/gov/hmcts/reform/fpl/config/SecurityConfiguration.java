package uk.gov.hmcts.reform.fpl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
public class SecurityConfiguration {
    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    static class SecurityConfigurationWithUserTokenValidator extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authorizeRequests()
                    .antMatchers("/callback/**")
                    .authenticated();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "false", matchIfMissing = true)
    static class DefaultSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                    .anyRequest()
                    .permitAll();
        }
    }

    @Order(3)
    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    static class RoboticsSecurityConfiguration extends WebSecurityConfigurerAdapter {
        private AuthCheckerUserOnlyFilter<User> authCheckerServiceAndUserFilter;

        public RoboticsSecurityConfiguration(RequestAuthorizer<User> userRequestAuthorizer,
                                             AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/sendRPAEmailByID/**")
                .permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(STATELESS)
                .and()
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(authCheckerServiceAndUserFilter);
        }

    }
}
