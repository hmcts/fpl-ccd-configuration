package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("LineLength")
@ControllerAdvice
public class ResourceExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(value = AboutToStartOrSubmitCallbackException.class)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> handleAboutToStartOrSubmitCallbackException(AboutToStartOrSubmitCallbackException exception, HttpServletRequest request) {
        logger.error("Exception for caller {}. {}", getCaller(request), exception.getMessage(), exception);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add(exception.getUserMessage())
                .build())
            .build());
    }

    @ExceptionHandler(value = Exception.class)
    public void handleAboutToStartOrSubmitCallbackException(RuntimeException exception, HttpServletRequest request) {
        logger.error("Caller {}", getCaller(request));
        throw exception;
    }

    private String getCaller(HttpServletRequest request) {
        return String.format("(id='%s', roles='%s')", request.getHeader("user-id"), request.getHeader("user-roles"));
    }
}
