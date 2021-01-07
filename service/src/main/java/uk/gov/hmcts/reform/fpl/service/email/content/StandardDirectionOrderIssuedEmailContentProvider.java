package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderIssuedEmailContentProvider extends StandardDirectionOrderContent {
    private final CaseDataExtractionService caseDataExtractionService;

    public CTSCTemplateForSDO buildNotificationParametersForCTSC(CaseData caseData) {

        HearingBooking hearing = caseData.getFirstHearing().orElseThrow(NoHearingBookingException::new);

        return CTSCTemplateForSDO.builder()
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .callout(buildCallout(caseData))
            .courtName(caseDataExtractionService.getCourtName(caseData.getCaseLocalAuthority()))
            .hearingNeedsPresent(getHearingNeedsPresent(hearing))
            .hearingNeeds(hearing.buildHearingNeedsList())
            .caseUrl(getCaseUrl(caseData.getId(), "OrdersTab"))
            .documentLink(getDocumentUrl(caseData.getStandardDirectionOrder().getOrderDoc()))
            .build();
    }

    private String getHearingNeedsPresent(HearingBooking hearingBooking) {
        return hearingBooking.getHearingNeedsBooked() == null
            || hearingBooking.getHearingNeedsBooked().contains(NONE) ? "No" : "Yes";
    }
}
