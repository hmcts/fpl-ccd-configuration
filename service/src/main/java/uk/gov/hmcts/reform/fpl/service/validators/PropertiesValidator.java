package uk.gov.hmcts.reform.fpl.service.validators;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

import static java.util.stream.Collectors.toList;

public abstract class PropertiesValidator implements Validator {

    private static List<String> AVAILABLE_PROPERTIES = Stream.of(CaseData.class.getDeclaredFields())
        .map(Field::getName)
        .collect(toList());

    private Class[] groups;
    private final List<String> properties;

    @Autowired
    private javax.validation.Validator validator;

    public PropertiesValidator(String... properties) {
        this(new Class[0], properties);
    }

    public PropertiesValidator(Class[] groups, String... properties) {
        this.groups = groups;
        this.properties = Arrays.asList(properties);
        List<String> invalidProperties = this.properties.stream()
            .filter(prop -> !AVAILABLE_PROPERTIES.contains(prop))
            .collect(Collectors.toList());
        if (ObjectUtils.isNotEmpty(invalidProperties)) {
            throw new IllegalArgumentException(
                String.format("Properties %s not found in %s", invalidProperties, CaseData.class.getSimpleName()));
        }
    }

    @Override
    public List<String> validate(CaseData caseData) {
        return validateProperty(caseData, this.properties, this.groups);
    }

    private List<String> validateProperty(CaseData caseData, List<String> propertiesToBeValidated, Class... groups) {
        return validator.validate(caseData, groups).stream()
            .filter(violation -> propertiesToBeValidated.contains(getViolatedProperty(violation)))
            .map(ConstraintViolation::getMessage)
            .distinct()
            .collect(toList());
    }

    private String getViolatedProperty(ConstraintViolation violation) {
        final Iterator<Path.Node> paths = violation.getPropertyPath().iterator();
        if (paths.hasNext()) {
            return paths.next().getName();
        }
        return null;
    }
}
