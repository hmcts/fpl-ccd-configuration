package uk.gov.hmcts.reform.fpl.controllers.guards;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.util.List;

@Service
public class GeneratedOrderGuard implements EventGuard {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ValidateGroupService validateGroupService;

    @Override
    public List<String> validate(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        return validateGroupService.validateGroup(caseData, ValidateFamilyManCaseNumberGroup.class);
    }
}
