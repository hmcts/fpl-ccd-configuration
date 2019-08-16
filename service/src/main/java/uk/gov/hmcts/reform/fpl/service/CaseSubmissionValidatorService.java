package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.SectionType;
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

        Stream.of(SectionType.APPLICANT, SectionType.CHILDREN, SectionType.ORDERS, SectionType.GROUNDS,
            SectionType.HEARING, SectionType.DOCUMENTS, SectionType.CASENAME)
            .flatMap(section -> Stream.of(groupErrorsBySection(violations, section)))
            .flatMap(Collection::stream)
            .forEach(caseErrors::add);

        return caseErrors.build();
    }

    private List<String> groupErrorsBySection(Set<ConstraintViolation<CaseData>> caseData, SectionType section) {
        List<String> errorList;

        errorList = caseData.stream()
            .filter(error -> error.getPropertyPath().toString().contains(section.getPredicate()))
            .map(error -> String.format("- %s", error.getMessage()))
            .collect(Collectors.toList());

        if (!errorList.isEmpty()) {
            errorList.add(0, String.format("In the %s section:", section.getSectionHeaderName()));
        }

        return errorList;
    }
}
