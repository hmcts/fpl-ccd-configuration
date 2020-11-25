package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final Time time;
    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;
    private static final String C2 = "C2Tab";

    public C2UploadedTemplate getNotifyData(final CaseData caseData, final DocumentReference latestC2) {
        C2UploadedTemplate adminTemplateForC2 = new C2UploadedTemplate();
        adminTemplateForC2.setCallout(buildCallout(caseData));
        adminTemplateForC2.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        adminTemplateForC2.setCaseUrl(getCaseUrl(caseData.getId(), C2));
        adminTemplateForC2.setDocumentUrl(concatUrlAndMostRecentUploadedDocumentPath(xuiBaseUrl,
            latestC2.getBinaryUrl()));

        return adminTemplateForC2;
    }

    public AllocatedJudgeTemplateForC2 getNotifyDataForAllocatedJudge(final CaseData caseData) {

        return AllocatedJudgeTemplateForC2.builder()
            .caseUrl(getCaseUrl(caseData.getId(), C2))
            .callout(buildCallout(caseData))
            .judgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle())
            .judgeName(caseData.getAllocatedJudge().getJudgeName())
            .respondentLastName(getFirstRespondentLastName(caseData))
            .build();

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

    public BaseCaseNotifyData getPbaPaymentNotTakenNotifyData(final CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), C2))
            .build();
    }
}
