package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassUploadDocumentService {
    private final CoreCaseDataService coreCaseDataService;
    private final UploadDocumentService uploadDocumentService;
    private static final String MIME_TYPE_PDF = "application/pdf";
    private static final String MIME_TYPE_PDF_X = "application/x-pdf";

    public DocumentReference uploadDocumentToDocStore(MultipartFile file) throws IOException {
        return DocumentReference.buildFromDocument(uploadDocumentService
                .uploadDocument(file.getBytes(), file.getName(), file.getContentType()));
    }

    public ManagedDocument convertToManagedDocument(DocumentReference documentReference) {
        return ManagedDocument.builder()
            .uploaderType(DocumentUploaderType.CAFCASS)
            .document(documentReference)
            .build();
    }

    public boolean isValidFile(MultipartFile file) throws IOException {
        return !file.isEmpty() && List.of(MIME_TYPE_PDF, MIME_TYPE_PDF_X).contains(file.getContentType());
    }
}
