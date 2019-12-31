package uk.gov.hmcts.reform.fpl.request;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class AuthorizationConfiguration {

    @Bean
    @RequestScope
    public RequestData authorisation(HttpServletRequest request) {
        String authorization = request.getHeader("authorization");

        return new RequestData(authorization);
    }
}
