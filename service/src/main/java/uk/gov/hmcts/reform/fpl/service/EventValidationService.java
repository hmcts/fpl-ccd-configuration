package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventValidationService {
    private final Validator validator;

    @Autowired
    public EventValidationService(Validator validator) {
        this.validator = validator;
    }

    public <T> List<String> validateEvent(T data, Class<?>...groups) {
        return validator.validate(data, groups).stream()
            .map(error -> error.getMessage()).collect(Collectors.toList());
    }
}
