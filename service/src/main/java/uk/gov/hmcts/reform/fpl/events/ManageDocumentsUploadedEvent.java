package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ManageDocumentsUploadedEvent {
    private final CaseData caseData;
    private final UserDetails initiatedBy;
    private final DocumentUploaderType uploadedUserType;

    private final Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocuments;
    private final Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocumentsLA;
    private final Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocumentsCTSC;
}
