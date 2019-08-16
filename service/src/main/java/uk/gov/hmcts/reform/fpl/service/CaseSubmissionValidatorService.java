package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

    public List<String> validateCaseDetails(CaseData caseData) {
        ImmutableList.Builder<String> caseErrors = ImmutableList.builder();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData);

        Stream.of(ImmutablePair.of("applicant", "applicant"),
            ImmutablePair.of("children", "children"),
            ImmutablePair.of("orders", "orders and directions needed"),
            ImmutablePair.of("groundsForTheApplication", "grounds for the application"),
            ImmutablePair.of("hearing", "hearing needed"),
            ImmutablePair.of("documents", "documents"),
            ImmutablePair.of("caseName", "case name"))
            .flatMap(section -> Stream.of(groupErrorsBySection(violations, section)))
            .flatMap(Collection::stream)
            .forEach(caseErrors::add);

        return caseErrors.build();
    }

    private List<String> groupErrorsBySection(Set<ConstraintViolation<CaseData>> caseData, ImmutablePair section) {
        List<String> errorList;

        errorList = caseData.stream()
            .filter(error -> error.getPropertyPath().toString().contains(section.left.toString()))
            .map(error -> String.format("- %s", error.getMessage()))
            .collect(Collectors.toList());

        if (!errorList.isEmpty()) {
            errorList.add(0, String.format("In the %s section:", section.right.toString()));
        }

        return errorList;
    }
}
