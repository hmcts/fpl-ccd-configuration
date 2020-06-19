package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingBookingValidatorService {

    private final ValidateGroupService validateGroupService;

    public List<String> validateHearingBookings(List<HearingBooking> hearingDetails) {
        final List<String> errors = new ArrayList<>();

        boolean multipleHearings = hearingDetails.size() > 1;

        for (int i = 0; i < hearingDetails.size(); i++) {
            HearingBooking hearingDetail = hearingDetails.get(i);
            for (String message : validateGroupService.validateGroup(hearingDetail, HearingBookingDetailsGroup.class)) {
                errors.add(multipleHearings ? String.format("%s for hearing %d", message, i + 1) : message);
            }
        }
        return errors;
    }

    public List<String> validateHasAllocatedJudge(CaseData caseData) {
        return validateGroupService.validateGroup(caseData, HearingBookingDetailsGroup.class);
    }
}
