package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithoutHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    public Map<String, Object> buildC2UploadNotification(final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        final String subjectLine = buildSubjectLine(caseData);

        return ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters(caseDetails))
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .put("reference", String.valueOf(caseDetails.getId()))
            .build();
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

    private String buildCallout(CaseData caseData) {
        if(hearingBookingService.hasFutureHearing(caseData.getHearingDetails())) {
            return buildSubjectLineWithHearingBookingDateSuffix(caseData,
                hearingBookingService.getMostUrgentHearingBooking(caseData.getHearingDetails()));
        } else {
            return buildSubjectLineWithoutHearingBookingDateSuffix(caseData);
        }
    }

    public Map<String, Object> buildC2UploadPbaPaymentNotTakenNotification(final CaseDetails caseDetails) {
        return buildCommonNotificationParameters(caseDetails);
    }

    private Map<String, Object> buildCommonNotificationParameters(final CaseDetails caseDetails) {
        return Map.of("caseUrl", getCaseUrl(caseDetails.getId()));
    }
}
