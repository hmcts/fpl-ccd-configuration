package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Section;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.util.stream.Collectors.toList;

@Service
public class CaseValidatorService {

    public static final String VALIDATION_SEPARATOR = "\n";
    private final Validator validator;

    @Autowired
    public CaseValidatorService(Validator validator) {
        this.validator = validator;
    }

    public List<String> validateCaseDetails(CaseData caseData, Class<?>...groups) {
        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData, groups);

        return Stream.of(Section.values())
            .flatMap(section -> Stream.of(groupViolationsBySection(violations, section)))
            .flatMap(Collection::stream)
            .collect(toList());
    }

    private List<String> groupViolationsBySection(Set<ConstraintViolation<CaseData>> constraintViolations,
                                                  Section section) {

        List<String> errorList = constraintViolations.stream()
            .filter(error -> isAssignableError(error.getPropertyPath().toString(), section))
            .flatMap(error -> Arrays.stream(error.getMessage().split(VALIDATION_SEPARATOR, -1)))
            .map(message -> String.format("• %s", message))
            .distinct()
            .collect(Collectors.toList());

        if (!errorList.isEmpty()) {
            errorList.add(0, String.format("In the %s section:", section.getSectionHeaderName()));
        }

        return errorList;
    }

    private boolean isAssignableError(String errorPropertyPath, Section section) {
        return Arrays.stream(section.getErrorKeys())
            .anyMatch(errorKey -> errorPropertyPath.toLowerCase().contains(errorKey));
    }
}
