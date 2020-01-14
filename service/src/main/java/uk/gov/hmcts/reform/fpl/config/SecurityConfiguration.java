package uk.gov.hmcts.reform.fpl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

import static org.springframework.http.HttpMethod.POST;

@Configuration
public class SecurityConfiguration {
    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    static class SecurityConfigurationWithUserTokenValidator extends WebSecurityConfigurerAdapter {

        private AuthCheckerUserOnlyFilter<User> authCheckerServiceAndUserFilter;


        public SecurityConfigurationWithUserTokenValidator(RequestAuthorizer<User> userRequestAuthorizer,
                                                           AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authorizeRequests()
                .antMatchers("/callback/**")
                .authenticated()
                .and()
                .requestMatchers()
                .antMatchers(POST, "/sendRPAEmailByID/*")
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
                                            AuthenticationManager authenticationManager) {
            authCheckerServiceAndUserFilter = new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers()
                .antMatchers(POST, "/sendRPAEmailByID/*")
                .and()
                .addFilter(authCheckerServiceAndUserFilter)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                .anyRequest().authenticated();
        }
    }
}
