package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidateGroupService {
    private final Validator validator;

    @Autowired
    public ValidateGroupService(Validator validator) {
        this.validator = validator;
    }

    public <T> List<String> validateGroup(T data, Class<?>... groups) {
        return validator.validate(data, groups).stream()
            .map(ConstraintViolation::getMessage).collect(Collectors.toList());
    }

    public <T> List<String> validateGroup(List<Element<T>> data, String  defaultMessage) {
        if(ObjectUtils.isEmpty(data)){
            return List.of(defaultMessage);
        }
        return data.stream().map(Element::getValue).flatMap(v -> validator.validate(v).stream())
            .map(ConstraintViolation::getMessage).collect(Collectors.toList());
    }
}
