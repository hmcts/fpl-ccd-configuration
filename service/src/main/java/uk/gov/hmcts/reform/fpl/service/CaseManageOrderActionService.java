package uk.gov.hmcts.reform.fpl.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
@Slf4j
@Service
public class CaseManageOrderActionService {
    public CaseManagementOrder addDocumentToCaseManagementOrder(final CaseManagementOrder caseManagementOrder,
                                                                final Document documentToAdd) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildCMODocumentReference(documentToAdd))
            .build();
    }
    private DocumentReference buildCMODocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }
}
