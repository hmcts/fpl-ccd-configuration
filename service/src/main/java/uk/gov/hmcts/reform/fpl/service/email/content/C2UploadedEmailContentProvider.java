package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.C2;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEmailContentProvider extends AbstractEmailContentProvider {
    private final FeatureToggleService featureToggleService;
    private final Time time;

    public C2UploadedTemplate getNotifyData(final CaseData caseData, final DocumentReference latestC2) {
        List<String> applicationTypes;

        if (featureToggleService.isUploadAdditionalApplicationsEnabled()) {
            applicationTypes = caseData.getLastC2DocumentBundle().getApplicationTypes();
        } else {
            applicationTypes = null;
        }

        return C2UploadedTemplate.builder()
            .callout(buildCallout(caseData))
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .caseUrl(getCaseUrl(caseData.getId(), C2))
            .documentUrl(getDocumentUrl(latestC2))
            .applicationTypes(applicationTypes)
            .build();
    }

    public AllocatedJudgeTemplateForC2 getNotifyDataForAllocatedJudge(final CaseData caseData) {

        return AllocatedJudgeTemplateForC2.builder()
            .caseUrl(getCaseUrl(caseData.getId(), C2))
            .callout(buildCalloutWithNextHearing(caseData, time.now()))
            .judgeTitle(caseData.getAllocatedJudge().getJudgeOrMagistrateTitle())
            .judgeName(caseData.getAllocatedJudge().getJudgeName())
            .respondentLastName(getFirstRespondentLastName(caseData))
            .build();

    }

    public BaseCaseNotifyData getPbaPaymentNotTakenNotifyData(final CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), C2))
            .build();
    }
}
