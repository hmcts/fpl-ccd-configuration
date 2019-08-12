package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Service
public class CaseSubmissionValidatorService {

    @Autowired
    public CaseSubmissionValidatorService() {

    }

    public List<String> validateCaseDetails(CaseData caseData) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData);
        violations.forEach(error -> errors.add(error.getMessage()));

        return errors.build();
    }
}
