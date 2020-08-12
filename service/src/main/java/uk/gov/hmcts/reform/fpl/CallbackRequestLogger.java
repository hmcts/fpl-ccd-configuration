package uk.gov.hmcts.reform.fpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.lang.reflect.Type;

@Slf4j
@ControllerAdvice
public class CallbackRequestLogger extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return targetType.equals(CallbackRequest.class);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        logCase(body, inputMessage.getHeaders());

        return body;
    }

    private void logCase(Object body, HttpHeaders headers) {
        try {
            CallbackRequest callbackRequest = (CallbackRequest) body;
            log.info(String.format("Event('%s') User(%s) Case(%s)",
                callbackRequest.getEventId(), getUser(headers), getCase(callbackRequest)));
        } catch (Exception e) {
            log.warn("Can not log case details", e);
        }
    }

    private String getUser(HttpHeaders httpHeaders) {
        String userIds = String.join(",", httpHeaders.getOrEmpty("user-id"));
        String roles = String.join(",", httpHeaders.getOrEmpty("user-roles"));

        return String.format("id='%s',roles='%s'", userIds, roles);
    }

    private String getCase(CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return String.format("id='%s' state='%s' la='%s' case number='%s'",
            caseDetails.getId(),
            caseDetails.getState(),
            caseDetails.getData().getOrDefault("caseLocalAuthority", ""),
            caseDetails.getData().getOrDefault("familyManCaseNumber", "")
        );
    }
}
