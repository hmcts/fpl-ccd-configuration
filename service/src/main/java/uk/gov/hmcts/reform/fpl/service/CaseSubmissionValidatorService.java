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

import static uk.gov.hmcts.reform.fpl.enums.SectionType.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.SectionType.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.SectionType.ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.SectionType.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.SectionType.HEARING;
import static uk.gov.hmcts.reform.fpl.enums.SectionType.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.SectionType.CASENAME;

@Service
public class CaseSubmissionValidatorService {

    public List<String> validateCaseDetails(CaseData caseData) {
        ImmutableList.Builder<String> caseErrors = ImmutableList.builder();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData);

        Stream.of(APPLICANT, CHILDREN, ORDERS, GROUNDS, HEARING, DOCUMENTS, CASENAME)
            .flatMap(section -> Stream.of(groupErrorsBySection(violations, section)))
            .flatMap(Collection::stream)
            .forEach(caseErrors::add);

        return caseErrors.build();
    }

    private List<String> groupErrorsBySection(Set<ConstraintViolation<CaseData>> caseData, SectionType section) {
        List<String> errorList;

        errorList = caseData.stream()
            .filter(error -> error.getPropertyPath().toString().contains(section.getPredicate()))
            .map(error -> String.format("\u2022 %s", error.getMessage()))
            .collect(Collectors.toList());

        if (!errorList.isEmpty()) {
            errorList.add(0, String.format("In the %s section:", section.getSectionHeaderName()));
        }

        return errorList;
    }
}
