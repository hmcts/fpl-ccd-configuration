package uk.gov.hmcts.reform.fpl.config.cafcass;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.fpl.interceptors.CafcassApiInterceptor;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiConfiguration implements WebMvcConfigurer {
    private final CafcassApiInterceptor cafcassApiInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cafcassApiInterceptor).addPathPatterns(
            "/cases",
            "/cases/documents/*/binary",
            "/cases/*/document",
            "/cases/*/guardians");
    }
}
