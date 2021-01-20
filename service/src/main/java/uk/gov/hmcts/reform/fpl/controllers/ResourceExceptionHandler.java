package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;
import uk.gov.hmcts.reform.fpl.exceptions.LogAsWarningException;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(value = AboutToStartOrSubmitCallbackException.class)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> handleAboutToStartOrSubmitCallbackException(
        AboutToStartOrSubmitCallbackException exception, HttpServletRequest request) {
        log.error("Exception for caller {}. {}", getCaller(request), exception.getMessage(), exception);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add(exception.getUserMessage())
                .build())
            .build());
    }

    @ExceptionHandler(value = Exception.class)
    public void handleAboutToStartOrSubmitCallbackException(RuntimeException exception, HttpServletRequest request) {
        log.error("Caller {}", getCaller(request));
        throw exception;
    }

    @ExceptionHandler(value = LogAsWarningException.class)
    public void handleLogAsWarningException(LogAsWarningException exception) {
        log.warn(exception.getMessage());
        throw exception;
    }

    private String getCaller(HttpServletRequest request) {
        return String.format("(id='%s', roles='%s')", request.getHeader("user-id"), request.getHeader("user-roles"));
    }
}
