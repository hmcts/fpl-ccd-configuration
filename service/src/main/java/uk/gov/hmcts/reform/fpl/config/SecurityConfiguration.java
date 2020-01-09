package uk.gov.hmcts.reform.fpl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

@Configuration
public class SecurityConfiguration {
    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    static class SecurityConfigurationWithUserTokenValidator extends WebSecurityConfigurerAdapter {

        private AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter;

        public SecurityConfigurationWithUserTokenValidator(RequestAuthorizer<User> userRequestAuthorizer,
                                                           RequestAuthorizer<Service> serviceRequestAuthorizer,
                                                           AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer,
                userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .addFilter(authCheckerServiceAndUserFilter)
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authorizeRequests()
                    .antMatchers("/callback/**")
                    .authenticated();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "false", matchIfMissing = true)
    static class DefaultSecurityConfiguration extends WebSecurityConfigurerAdapter {
        private AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter;

        public DefaultSecurityConfiguration(RequestAuthorizer<User> userRequestAuthorizer,
                                            RequestAuthorizer<Service> serviceRequestAuthorizer,
                                            AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer,
                userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .addFilter(authCheckerServiceAndUserFilter)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                    .anyRequest()
                    .permitAll();
        }
    }
}
