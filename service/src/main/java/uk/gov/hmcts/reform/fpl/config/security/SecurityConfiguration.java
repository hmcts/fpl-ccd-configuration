package uk.gov.hmcts.reform.fpl.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

@Configuration
@SuppressWarnings("java:S1118")
public class SecurityConfiguration {

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    static class SecurityConfigurationWithUserTokenValidator {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authorizeRequests()
                    .antMatchers("/callback/**")
                    .authenticated();
            return http.build();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "false", matchIfMissing = true)
    static class DefaultSecurityConfiguration {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                    .anyRequest()
                    .permitAll();
            return http.build();
        }
    }

    @Order(2)
    @Configuration
    static class RoboticsSecurityConfiguration {
        private AuthCheckerUserOnlyFilter<User> authCheckerUserOnlyFilter;

        public RoboticsSecurityConfiguration(RequestAuthorizer<User> userRequestAuthorizer,
                                             AuthenticationManager authenticationManager) {
            authCheckerUserOnlyFilter = new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
            authCheckerUserOnlyFilter.setAuthenticationManager(authenticationManager);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.requestMatchers()
                .antMatchers(HttpMethod.POST, "/sendRPAEmailByID/*", "/support/**/*")
                .and()
                .addFilter(authCheckerUserOnlyFilter)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                .anyRequest()
                .authenticated();
            return http.build();
        }

    }
}
