package uk.gov.hmcts.reform.fpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@ControllerAdvice
public class CallbackResponseLogger implements ResponseBodyAdvice<AboutToStartOrSubmitCallbackResponse> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse beforeBodyWrite(
        AboutToStartOrSubmitCallbackResponse body,
        MethodParameter returnType, MediaType contentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request,
        ServerHttpResponse response) {

        logCase(body, request);
        return body;
    }

    private void logCase(AboutToStartOrSubmitCallbackResponse body, ServerHttpRequest request) {
        try {
            if (isNotEmpty(body.getErrors()) || isNotEmpty(body.getWarnings())) {
                log.info(String.format(
                    "Callback(%s) ended with errors=%s warnings=%s",
                    request.getURI().getPath(),
                    defaultIfNull(body.getErrors(), emptyList()),
                    defaultIfNull(body.getWarnings(), emptyList())));
            }
        } catch (Exception e) {
            log.warn("Can not log case details", e);
        }
    }

}
