package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Getter
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEvent {
    private final CaseData caseData;
    // TODO: Replace with a suitable enum or logic. Something like LA_SOLICITOR, SOLICITOR, HMCTS_USER?
    private final String uploadedBy;
    private final UserDetails initiatedBy;
}
