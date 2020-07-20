package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingValidatorService;

import java.util.List;

@Service
public class HearingDetailsGuard implements EventValidator {

    @Autowired
    private HearingBookingValidatorService validationService;

    @Override
    public List<String> validate(CaseData caseData) {
        return validationService.validateHasAllocatedJudge(caseData);
    }
}
