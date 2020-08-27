package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.SOMETHING_ELSE;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderIssuedEmailContentProvider extends StandardDirectionOrderContent {
    private final ObjectMapper mapper;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    public AllocatedJudgeTemplateForSDO buildNotificationParametersForAllocatedJudge(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        Map<String, Object> commonSDOParameters = super.getSDOPersonalisationBuilder(
            caseDetails.getId(), caseData).build();

        AllocatedJudgeTemplateForSDO allocatedJudgeTemplate = new AllocatedJudgeTemplateForSDO();
        allocatedJudgeTemplate.setFamilyManCaseNumber(commonSDOParameters.get("familyManCaseNumber").toString());
        allocatedJudgeTemplate.setLeadRespondentsName(commonSDOParameters.get("leadRespondentsName").toString());
        allocatedJudgeTemplate.setHearingDate(commonSDOParameters.get("hearingDate").toString());
        allocatedJudgeTemplate.setCaseUrl(commonSDOParameters.get("caseUrl").toString());
        allocatedJudgeTemplate.setJudgeTitle(caseData.getStandardDirectionOrder()
            .getJudgeAndLegalAdvisor()
            .getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getStandardDirectionOrder()
            .getJudgeAndLegalAdvisor()
            .getJudgeName());

        return allocatedJudgeTemplate;
    }

    public CTSCTemplateForSDO buildNotificationParametersForCTSC(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        HearingBooking hearing = caseData.getFirstHearing().orElseThrow(NoHearingBookingException::new);

        CTSCTemplateForSDO ctscTemplateForSDO = new CTSCTemplateForSDO();
        ctscTemplateForSDO.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        ctscTemplateForSDO.setCallout(buildCallout(caseData));
        ctscTemplateForSDO.setCourtName(getCourtName(caseData.getCaseLocalAuthority()));
        ctscTemplateForSDO.setHearingNeedsPresent(getHearingNeedsPresent(hearing));
        ctscTemplateForSDO.setHearingNeeds(buildHearingNeedsList(hearing.getHearingNeedsBooked()));
        ctscTemplateForSDO.setHearingNeedsDetails(hearing.getHearingNeedsDetails());
        ctscTemplateForSDO.setCaseUrl(getCaseUrl(caseDetails.getId()));
        ctscTemplateForSDO.setDocumentLink(concatUrlAndMostRecentUploadedDocumentPath(xuiBaseUrl,
            caseData.getStandardDirectionOrder().getOrderDoc().getBinaryUrl()));

        return ctscTemplateForSDO;
    }

    private String buildCallout(final CaseData caseData) {
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            caseData.getFirstHearing().orElse(null));
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private List<String> buildHearingNeedsList(List<HearingNeedsBooked> hearingNeedsBooked) {
        List<String> list = new ArrayList<>();

        if (hearingNeedsBooked == null || hearingNeedsBooked.isEmpty()) {
            return list;
        }

        for (HearingNeedsBooked hearingNeed : hearingNeedsBooked) {
            if (hearingNeed == NONE) {
                return emptyList();
            }
            if (hearingNeed != SOMETHING_ELSE) {
                list.add(hearingNeed.getLabel());
            }
        }
        return list;
    }

    private String getHearingNeedsPresent(HearingBooking hearingBooking) {
        return hearingBooking.getHearingNeedsBooked() == null
            || hearingBooking.getHearingNeedsBooked().contains(NONE) ? "No" : "Yes";
    }
}
