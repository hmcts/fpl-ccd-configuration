package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

@Service
public class CaseSubmissionValidatorService {

    @Autowired
    public CaseSubmissionValidatorService() {

    }

    public List<String> validateCaseDetails(CaseData caseData) {
        ImmutableList.Builder<String> caseErrors = ImmutableList.builder();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData);

        List<String> errors = Stream.of("applicant", "children", "orders", "groundsForTheApplication", "hearing",
            "caseName", "documents")
            .flatMap(section -> Stream.of(buildErrorSection(violations, section)))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        errors.forEach(error -> caseErrors.add(error));

        return caseErrors.build();
    }

    private List<String> buildErrorSection(Set<ConstraintViolation<CaseData>> caseData, String predicate) {
        List<String> errorList;

        errorList = caseData.stream()
            .filter(error -> error.getPropertyPath().toString().indexOf(predicate) != -1)
            .map(error -> String.format("- %s", error.getMessage()))
            .collect(Collectors.toList());

        if (!errorList.isEmpty()) {
            errorList.add(0, String.format("In the %s section:", splitCamelCase(predicate)));
        }

        return errorList;
    }

    private String splitCamelCase(String phrase) {
        String editedPhrase = "";

        for (int i = 0; i < phrase.length(); i++) {
            char charAtIndex = phrase.charAt(i);
            if (Character.isUpperCase(charAtIndex)) {
                editedPhrase = editedPhrase + " " + Character.toLowerCase(charAtIndex);
            } else {
                editedPhrase = editedPhrase + charAtIndex;
            }
        }
        return editedPhrase;
    }
}
