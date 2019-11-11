package uk.gov.hmcts.reform.fpl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(value="spring.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .antMatchers("/callback/**")
                    .authenticated()
            );
    }
}
