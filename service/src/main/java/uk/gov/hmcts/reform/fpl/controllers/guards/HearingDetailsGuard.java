package uk.gov.hmcts.reform.fpl.controllers.guards;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingValidatorService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.util.List;

@Service
public class HearingDetailsGuard implements EventGuard {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private HearingBookingValidatorService validationService;

    @Override
    public List<String> validate(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        return validationService.validateHasAllocatedJudge(caseData);
    }
}
