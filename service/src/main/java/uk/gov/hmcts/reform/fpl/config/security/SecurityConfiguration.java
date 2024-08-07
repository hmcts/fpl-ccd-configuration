package uk.gov.hmcts.reform.fpl.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

@Configuration
@SuppressWarnings({"java:S1118", "java:S4502"})
@EnableWebSecurity
public class SecurityConfiguration {

    private final RequestAuthorizer<User> userRequestAuthorizer;

    private final AuthenticationManager authenticationManager;

    public SecurityConfiguration(
        RequestAuthorizer<User> userRequestAuthorizer,
        AuthenticationManager authenticationManager
    ) {
        this.userRequestAuthorizer = userRequestAuthorizer;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true")
    public SecurityFilterChain securityFilterChainWithJwt(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder)))
            .authorizeRequests()
            .requestMatchers("/callback/**")
            .authenticated();
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeRequests(auth -> auth
                .anyRequest().permitAll());
        return http.build();
    }

    @Order(2)
    @Bean
    public SecurityFilterChain roboticsSecurityFilterChain(HttpSecurity http) throws Exception {
        AuthCheckerUserOnlyFilter<User> authCheckerUserOnlyFilter =
            new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
        authCheckerUserOnlyFilter.setAuthenticationManager(authenticationManager);

        http
            .authorizeRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/sendRPAEmailByID/*", "/support/**")
                .authenticated())
            .csrf(csrf -> csrf.disable())
            .addFilter(authCheckerUserOnlyFilter);

        return http.build();
    }
}
