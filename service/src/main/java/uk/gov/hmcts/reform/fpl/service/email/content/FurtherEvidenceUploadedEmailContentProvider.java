package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEmailContentProvider extends AbstractEmailContentProvider {
    public FurtherEvidenceDocumentUploadedData buildParameters(CaseData caseData) {
        return FurtherEvidenceDocumentUploadedData.builder().caseUrl(getCaseUrl(caseData.getId())).build();
    }
}
