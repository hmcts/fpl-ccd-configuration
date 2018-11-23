package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;

@ControllerAdvice
public class ResourceExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(value = UnknownLocalAuthorityDomainException.class)
    public ResponseEntity<Object> handleUnknownLocalAuthorityDomain(Exception exception) {
        logger.error(exception.getMessage(), exception);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add("The email address was not linked to a known Local Authority")
                .build())
            .build();

        return ResponseEntity.ok(response);
    }
}
