package uk.gov.hmcts.reform.fpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.logging.HeaderInformationExtractor;

import java.lang.reflect.Type;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallbackRequestLogger extends RequestBodyAdviceAdapter {

    private final HeaderInformationExtractor extractor;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return targetType.equals(CallbackRequest.class);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        logCase(body, inputMessage, parameter);

        return body;
    }

    private void logCase(Object body, HttpInputMessage inputMessage, MethodParameter parameter) {
        try {
            CallbackRequest callbackRequest = (CallbackRequest) body;
            log.info(String.format("Callback(%s) User(%s) Case(%s)",
                extractor.getCallback(callbackRequest, parameter),
                extractor.getUser(inputMessage.getHeaders()),
                extractor.getCase(callbackRequest)));
        } catch (Exception e) {
            log.warn("Can not log case details", e);
        }
    }


}
