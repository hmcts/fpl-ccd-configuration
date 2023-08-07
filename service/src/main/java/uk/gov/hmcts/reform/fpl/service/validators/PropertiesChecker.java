package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseDataParent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Path;


public abstract class PropertiesChecker implements EventChecker {

    private static final List<String> AVAILABLE_PROPERTIES = Stream.of(CaseData.class.getDeclaredFields(),
            CaseDataParent.class.getDeclaredFields())
        .flatMap(Arrays::stream)
        .map(Field::getName)
        .toList();

    @Autowired
    private javax.validation.Validator validator;

    public List<String> validate(CaseData caseData, List<String> propertiesToBeValidated, Class<?>... groups) {
        checkProperties(propertiesToBeValidated);
        return validator.validate(caseData, groups).stream()
                .filter(violation -> propertiesToBeValidated.contains(getViolatedProperty(violation)))
                .map(ConstraintViolation::getMessage)
                .distinct()
                .toList();
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
                .toList();
        if (!invalidProperties.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Properties %s not found in %s", invalidProperties, CaseData.class.getSimpleName()));
        }
    }
}
