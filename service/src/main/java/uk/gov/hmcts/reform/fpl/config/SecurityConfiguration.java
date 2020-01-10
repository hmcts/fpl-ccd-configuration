package uk.gov.hmcts.reform.fpl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

@Configuration
public class SecurityConfiguration {
    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    static class SecurityConfigurationWithUserTokenValidator extends WebSecurityConfigurerAdapter {

        private AuthCheckerUserOnlyFilter<User> authCheckerServiceAndUserFilter;


        public SecurityConfigurationWithUserTokenValidator(RequestAuthorizer<User> userRequestAuthorizer,
                                                           RequestAuthorizer<Service> serviceRequestAuthorizer,
                                                           AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerUserOnlyFilter<User>(userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.requestMatchers()
                .antMatchers(HttpMethod.POST, "/sendRPAEmailByID/*")
                .antMatchers(HttpMethod.POST, "/sendRPAEmailByID/**")
                .and()
                .addFilter(authCheckerServiceAndUserFilter)
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "false", matchIfMissing = true)
    static class DefaultSecurityConfiguration extends WebSecurityConfigurerAdapter {
        private AuthCheckerUserOnlyFilter<User> authCheckerServiceAndUserFilter;

        public DefaultSecurityConfiguration(RequestAuthorizer<User> userRequestAuthorizer,
                                            RequestAuthorizer<Service> serviceRequestAuthorizer,
                                            AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerUserOnlyFilter<User>(userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers()
                .antMatchers(HttpMethod.POST, "/sendRPAEmailByID/*")
                .antMatchers(HttpMethod.POST, "/sendRPAEmailByID/**")
                .and()
                .addFilter(authCheckerServiceAndUserFilter)
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated();
        }
    }
}
