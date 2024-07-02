package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;
import uk.gov.hmcts.reform.fpl.exceptions.LogAsWarningException;
import uk.gov.hmcts.reform.fpl.logging.HeaderInformationExtractor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResourceExceptionHandler {

    private final HeaderInformationExtractor extractor;

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
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> handleLogAsWarningException(
        LogAsWarningException exception, HttpServletRequest request) {
        log.warn("Ignorable exception for caller {}. {}", getCaller(request), exception.getMessage());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(exception.getUserMessage()))
            .build());
    }

    private String getCaller(HttpServletRequest httpRequest) {
        HttpHeaders httpHeaders = Collections.list(httpRequest.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                h -> Collections.list(httpRequest.getHeaders(h)),
                (oldValue, newValue) -> newValue,
                HttpHeaders::new
            ));
        return String.format("(%s)", extractor.getUser(httpHeaders));
    }
}
