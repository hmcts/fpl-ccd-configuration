package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.List;

@Component
public class RespondentValidator implements EventValidator {

    @Autowired
    private ValidateGroupService validateGroupService;

    @Override
    public List<String> validate(CaseData caseData) {
        return validateGroupService.validateGroup(caseData.getAllRespondents(), "You need to add details to respondents");
    }
}
