package uk.gov.hmcts.reform.fpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

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
        logCase(body, inputMessage, parameter);

        return body;
    }

    private void logCase(Object body, HttpInputMessage inputMessage, MethodParameter parameter) {
        try {
            CallbackRequest callbackRequest = (CallbackRequest) body;
            log.info(String.format("Callback(%s) User(%s) Case(%s)",
                getCallback(callbackRequest, parameter),
                getUser(inputMessage.getHeaders()),
                getCase(callbackRequest)));
        } catch (Exception e) {
            log.warn("Can not log case details", e);
        }
    }

    private String getCallback(CallbackRequest callbackRequest, MethodParameter parameter) {
        String eventName = callbackRequest.getEventId();
        String callbackType = Optional.ofNullable(parameter.getMethod().getAnnotation(PostMapping.class))
            .map(PostMapping::value)
            .map(path -> String.join("", path))
            .orElse("");

        return String.format("event='%s',type='%s'", eventName, callbackType);
    }

    private String getUser(HttpHeaders httpHeaders) {
        String userIds = String.join(",", httpHeaders.getOrEmpty("user-id"));
        String userRoles = httpHeaders.getOrEmpty("user-roles").stream()
            .flatMap(roles -> Stream.of(roles.split(",")))
            .map(String::trim)
            .filter(role -> !role.equals("caseworker") && !role.equals("caseworker-publiclaw"))
            .collect(joining(","));

        return String.format("id='%s',roles='%s'", userIds, userRoles);
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
