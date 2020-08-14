package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;
    private final Time time;
    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    public C2UploadedTemplate buildC2UploadNotificationTemplate(final CaseDetails caseDetails,
                                                                final DocumentReference latestC2) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        C2UploadedTemplate adminTemplateForC2 = new C2UploadedTemplate();
        adminTemplateForC2.setCallout(buildCallout(caseData));
        adminTemplateForC2.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        adminTemplateForC2.setCaseUrl(getCaseUrl(caseDetails.getId()));
        adminTemplateForC2.setDocumentUrl(concatUrlAndMostRecentUploadedDocumentPath(xuiBaseUrl,
            latestC2.getBinaryUrl()));

        return adminTemplateForC2;
    }

    public AllocatedJudgeTemplateForC2 buildC2UploadNotificationForAllocatedJudge(final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocatedJudgeTemplateForC2 allocatedJudgeTemplateForC2 = new AllocatedJudgeTemplateForC2();
        allocatedJudgeTemplateForC2.setCaseUrl(getCaseUrl(caseDetails.getId()));
        allocatedJudgeTemplateForC2.setCallout(buildCallout(caseData));
        allocatedJudgeTemplateForC2.setJudgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle());
        allocatedJudgeTemplateForC2.setJudgeName(caseData.getAllocatedJudge().getJudgeName());
        allocatedJudgeTemplateForC2.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));

        return  allocatedJudgeTemplateForC2;
    }

    private String buildCallout(final CaseData caseData) {
        HearingBooking hearing = null;
        if (caseData.hasFutureHearing(caseData.getHearingDetails())) {
            hearing = caseData.getMostUrgentHearingBookingAfter(time.now());
        }
        return buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            hearing);
    }

    public Map<String, Object> buildC2UploadPbaPaymentNotTakenNotification(final CaseDetails caseDetails) {
        return buildCommonNotificationParameters(caseDetails);
    }

    private Map<String, Object> buildCommonNotificationParameters(final CaseDetails caseDetails) {
        return Map.of("caseUrl", getCaseUrl(caseDetails.getId()));
    }
}
