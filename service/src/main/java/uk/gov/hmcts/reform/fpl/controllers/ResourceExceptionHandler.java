package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;

@ControllerAdvice
public class ResourceExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(value = AboutToStartOrSubmitCallbackException.class)
    public ResponseEntity<Object> handleAboutToStartOrSubmitCallbackException(AboutToStartOrSubmitCallbackException
                                                                                  exception) {
        logger.error(exception.getMessage(), exception);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add(exception.getUserMessage())
                .build())
            .build();

        return ResponseEntity.ok(response);
    }
}
