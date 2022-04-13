package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.courtbundle.CourtBundleUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DOCUMENTS;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtBundleUploadedEmailContentProvider extends AbstractEmailContentProvider {

    public CourtBundleUploadedData buildParameters(CaseData caseData, String hearingDetails) {
        return CourtBundleUploadedData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), DOCUMENTS))
            .hearingDetails(hearingDetails)
            .build();
    }
}
