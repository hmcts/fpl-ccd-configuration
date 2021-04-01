package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class FurtherEvidenceUploadedEmailContentProvider extends AbstractEmailContentProvider {
    public FurtherEvidenceDocumentUploadedData buildParameters(CaseData caseData, String sender) {
        return FurtherEvidenceDocumentUploadedData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), DOCUMENTS))
            .callout(buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
                caseData.getRespondents1(),
                caseData.getFirstHearing().orElse(null)))
            .userName(sender)
            .respondentLastName(getFirstRespondentLastName(caseData))
            .build();
    }
}
