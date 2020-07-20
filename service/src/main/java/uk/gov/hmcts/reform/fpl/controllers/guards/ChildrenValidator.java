package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ChildrenValidator implements EventValidator {

    @Autowired
    private  Validator validator;

    @Autowired
    private ValidateGroupService validateGroupService;

    @Override
    public List<String> validate(CaseData caseData) {

       List<ConstraintViolation<CaseData>> v = validator.validate(caseData).stream().filter(c-> c.getPropertyPath().iterator().next().getName().equals("children1")).collect(Collectors.toList());

        return validateGroupService.validateGroup(caseData.getAllChildren(), "You need to add details to children");
    }
}
