package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Deprecated
@Getter
@RequiredArgsConstructor
public class FurtherEvidenceUploadedEvent {
    private final CaseData caseData;
    private final CaseData caseDataBefore;
    private final DocumentUploaderType userType;
    private final UserDetails initiatedBy;
}
