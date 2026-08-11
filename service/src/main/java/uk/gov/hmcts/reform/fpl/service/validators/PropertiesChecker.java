package uk.gov.hmcts.reform.fpl.service.validators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseDataParent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public abstract class PropertiesChecker implements EventChecker {

    private static final List<String> AVAILABLE_PROPERTIES = Stream.of(CaseData.class.getDeclaredFields(),
            CaseDataParent.class.getDeclaredFields())
        .flatMap(Arrays::stream)
        .map(Field::getName)
        .collect(toList());

    @Autowired
    private jakarta.validation.Validator validator;

    public List<String> validate(CaseData caseData, List<String> propertiesToBeValidated, Class<?>... groups) {
        checkProperties(propertiesToBeValidated);
        return validator.validate(caseData, groups).stream()
                .filter(violation -> propertiesToBeValidated.contains(getViolatedProperty(violation)))
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(toList());
    }

    public List<String> validate(Object object, Class<?>... groups) {
        return validator.validate(object, groups).stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(toList());
    }

    private String getViolatedProperty(ConstraintViolation<CaseData> violation) {
        final Iterator<Path.Node> paths = violation.getPropertyPath().iterator();
        if (paths.hasNext()) {
            return paths.next().getName();
        }
        return null;
    }

    private static void checkProperties(List<String> properties) {
        List<String> invalidProperties = properties.stream()
                .filter(prop -> !AVAILABLE_PROPERTIES.contains(prop))
                .collect(Collectors.toList());
        if (!invalidProperties.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Properties %s not found in %s", invalidProperties, CaseData.class.getSimpleName()));
        }
    }
}
