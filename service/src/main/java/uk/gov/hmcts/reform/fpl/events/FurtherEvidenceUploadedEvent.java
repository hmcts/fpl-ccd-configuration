package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Getter
@RequiredArgsConstructor
public class FurtherEvidenceUploadedEvent {
    private final CaseData caseData;
    private final CaseData caseDataBefore;
    private final DocumentUploadNotificationUserType userType;
    private final UserDetails initiatedBy;
}
