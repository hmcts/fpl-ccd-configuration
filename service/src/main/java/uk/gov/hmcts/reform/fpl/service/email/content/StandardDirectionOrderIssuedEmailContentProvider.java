package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.SOMETHING_ELSE;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderIssuedEmailContentProvider extends StandardDirectionOrderContent {
    private final CaseDataExtractionService caseDataExtractionService;
    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    public AllocatedJudgeTemplateForSDO buildNotificationParametersForAllocatedJudge(CaseData caseData) {
        Map<String, Object> commonSDOParameters = super.getSDOPersonalisationBuilder(caseData).build();

        AllocatedJudgeTemplateForSDO allocatedJudgeTemplate = new AllocatedJudgeTemplateForSDO();
        allocatedJudgeTemplate.setFamilyManCaseNumber(commonSDOParameters.get("familyManCaseNumber").toString());
        allocatedJudgeTemplate.setLeadRespondentsName(commonSDOParameters.get("leadRespondentsName").toString());
        allocatedJudgeTemplate.setHearingDate(commonSDOParameters.get("hearingDate").toString());
        allocatedJudgeTemplate.setCaseUrl(commonSDOParameters.get("caseUrl").toString());
        allocatedJudgeTemplate.setCallout(buildCallout(caseData));
        allocatedJudgeTemplate.setJudgeTitle(caseData.getStandardDirectionOrder()
            .getJudgeAndLegalAdvisor()
            .getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getStandardDirectionOrder()
            .getJudgeAndLegalAdvisor()
            .getJudgeName());

        return allocatedJudgeTemplate;
    }

    //Clean up private method logic when hearing needs list is improved (remove 'none' option, make others optional)
    //FPLA-2144
    public CTSCTemplateForSDO buildNotificationParametersForCTSC(CaseData caseData) {

        HearingBooking hearing = caseData.getFirstHearing().orElseThrow(NoHearingBookingException::new);

        CTSCTemplateForSDO ctscTemplateForSDO = new CTSCTemplateForSDO();
        ctscTemplateForSDO.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        ctscTemplateForSDO.setCallout(buildCallout(caseData));
        ctscTemplateForSDO.setCourtName(caseDataExtractionService.getCourtName(caseData.getCaseLocalAuthority()));
        ctscTemplateForSDO.setHearingNeedsPresent(getHearingNeedsPresent(hearing));
        ctscTemplateForSDO.setShowDetailsLabel(getShowDetailsLabel(hearing));
        ctscTemplateForSDO.setHearingNeeds(hearing.buildHearingNeedsList());
        ctscTemplateForSDO.setHearingNeedsDetails(getHearingNeedsDetails(hearing));
        ctscTemplateForSDO.setCaseUrl(getCaseUrl(caseData.getId()));
        ctscTemplateForSDO.setDocumentLink(concatUrlAndMostRecentUploadedDocumentPath(xuiBaseUrl,
            caseData.getStandardDirectionOrder().getOrderDoc().getBinaryUrl()));

        return ctscTemplateForSDO;
    }

    private String buildCallout(final CaseData caseData) {
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            caseData.getFirstHearing().orElse(null));
    }

    private String getHearingNeedsPresent(HearingBooking hearingBooking) {
        return hearingBooking.getHearingNeedsBooked() == null
            || hearingBooking.getHearingNeedsBooked().contains(NONE) ? "No" : "Yes";
    }

    private String getHearingNeedsDetails(HearingBooking hearingBooking) {
        if (hearingBooking.getHearingNeedsBooked() != null && hearingBooking.getHearingNeedsBooked().contains(NONE)) {
            return "";
        } else {
            return hearingBooking.getHearingNeedsDetails();
        }
    }

    //Do not show label if NONE is selected or if SOMETHING_ELSE is only option selected
    private String getShowDetailsLabel(HearingBooking hearingBooking) {
        if (hearingBooking.getHearingNeedsBooked() != null) {
            List<HearingNeedsBooked> hearingNeedsBooked = hearingBooking.getHearingNeedsBooked();

            if (hearingNeedsBooked.contains(NONE)
                || hearingNeedsBooked.contains(SOMETHING_ELSE) && hearingNeedsBooked.size() == 1) {
                return "No";
            } else {
                return "Yes";
            }
        }
        return "No";
    }
}
