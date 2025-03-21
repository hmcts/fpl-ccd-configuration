package uk.gov.hmcts.reform.fpl.interceptors;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class AboutToSubmitResponseBodyAdvice implements ResponseBodyAdvice<AboutToStartOrSubmitCallbackResponse> {
    private static final List<String> WHITE_LIST = List.of(
        "/callback/.+/about-to-submit"
    );
    private static final List<String> EXCLUDED_LIST = List.of(
        "/callback/migrate-case/about-to-submit"
    );

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse beforeBodyWrite(AboutToStartOrSubmitCallbackResponse body,
                                                                MethodParameter returnType,
                                                                MediaType selectedContentType,
                                                                Class<? extends HttpMessageConverter<?>>
                                                                        selectedConverterType,
                                                                ServerHttpRequest request,
                                                                ServerHttpResponse response) {
        final String path = request.getURI().getPath();

        if (WHITE_LIST.stream().anyMatch(path::matches) && EXCLUDED_LIST.stream().noneMatch(path::matches)) {
            body.getData().put("lastGenuineUpdateTimed", LocalDateTime.now());
        }

        return body;
    }
}
