package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderIssuedEmailContentProvider extends StandardDirectionOrderContent {
    private final CaseDataExtractionService caseDataExtractionService;
    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    public CTSCTemplateForSDO buildNotificationParametersForCTSC(CaseData caseData) {

        HearingBooking hearing = caseData.getFirstHearing().orElseThrow(NoHearingBookingException::new);

        CTSCTemplateForSDO ctscTemplateForSDO = new CTSCTemplateForSDO();
        ctscTemplateForSDO.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        ctscTemplateForSDO.setCallout(buildCallout(caseData));
        ctscTemplateForSDO.setCourtName(caseDataExtractionService.getCourtName(caseData.getCaseLocalAuthority()));
        ctscTemplateForSDO.setHearingNeedsPresent(getHearingNeedsPresent(hearing));
        ctscTemplateForSDO.setHearingNeeds(hearing.buildHearingNeedsList());
        ctscTemplateForSDO.setCaseUrl(getCaseUrl(caseData.getId(), "OrdersTab"));
        ctscTemplateForSDO.setDocumentLink(concatUrlAndMostRecentUploadedDocumentPath(xuiBaseUrl,
            caseData.getStandardDirectionOrder().getOrderDoc().getBinaryUrl()));

        return ctscTemplateForSDO;
    }

    private String getHearingNeedsPresent(HearingBooking hearingBooking) {
        return hearingBooking.getHearingNeedsBooked() == null
            || hearingBooking.getHearingNeedsBooked().contains(NONE) ? "No" : "Yes";
    }
}
