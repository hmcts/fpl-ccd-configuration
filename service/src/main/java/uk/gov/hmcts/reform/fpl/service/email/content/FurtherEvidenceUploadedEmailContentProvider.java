package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FurtherEvidenceUploadedEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public FurtherEvidenceDocumentUploadedData buildParameters(CaseData caseData, String sender,
                                                               List<String> newNonConfidentialDocuments) {
        return FurtherEvidenceDocumentUploadedData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), DOCUMENTS))
            .callout(buildSubjectLineWithHearingBookingDateSuffix(
                caseData.getFamilyManCaseNumber(), caseData.getRespondents1(), caseData.getFirstHearing().orElse(null)
            ))
            .userName(sender)
            .lastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .documents(newNonConfidentialDocuments)
            .build();
    }
}
