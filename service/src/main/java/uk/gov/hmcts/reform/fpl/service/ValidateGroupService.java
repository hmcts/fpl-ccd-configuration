package uk.gov.hmcts.reform.fpl.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidateGroupService {
    private final Validator validator;

    @Autowired
    public ValidateGroupService(Validator validator) {
        this.validator = validator;
    }

    public <T> List<String> validateGroup(T data, Class<?>...groups) {
        return validator.validate(data, groups).stream()
            .map(ConstraintViolation::getMessage).collect(Collectors.toList());
    }
}
