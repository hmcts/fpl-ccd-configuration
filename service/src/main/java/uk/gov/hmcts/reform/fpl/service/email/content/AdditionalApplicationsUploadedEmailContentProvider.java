package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsUploadedEmailContentProvider extends AbstractEmailContentProvider {

    private final Time time;

    public AdditionalApplicationsUploadedTemplate getNotifyData(final CaseData caseData,
                                                                final DocumentReference latestC2) {
        return AdditionalApplicationsUploadedTemplate.builder()
            .callout(buildCallout(caseData))
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .caseUrl(getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .documentUrl(getDocumentUrl(latestC2))
            .applicationTypes(caseData.getMostRecentC2DocumentBundle().getApplicationTypes())
            .build();
    }

    public BaseCaseNotifyData getPbaPaymentNotTakenNotifyData(final CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), OTHER_APPLICATIONS))
            .build();
    }
}
