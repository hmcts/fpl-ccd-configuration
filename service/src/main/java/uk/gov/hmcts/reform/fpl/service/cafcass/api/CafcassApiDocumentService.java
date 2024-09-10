package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiDocumentService {
    private final SecureDocStoreService secureDocStoreService;
    private final UploadDocumentService uploadDocumentService;
    private final CoreCaseDataService coreCaseDataService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MIME_TYPE_PDF = "application/pdf";
    private static final String MIME_TYPE_PDF_X = "application/x-pdf";

    public byte[] downloadDocumentByDocumentId(String documentId) throws IllegalArgumentException, EmptyFileException {
        return secureDocStoreService.downloadDocument(documentId);
    }

    public DocumentReference uploadDocumentToDocStore(MultipartFile file, String typeOfDocument) throws IOException {
        return DocumentReference.buildFromDocument(uploadDocumentService
            .uploadDocument(file.getBytes(), generateFileName(typeOfDocument), file.getContentType()));
    }

    public String generateFileName(String typeOfDocument) {
        return format("%s-%s",
           typeOfDocument, ZonedDateTime.now(ZoneId.of("Europe/London")).format(DATE_TIME_FORMATTER));
    }

    public CaseDetails uploadGuardianReport(DocumentReference documentReference, CaseData caseData) {
        ManagedDocument guardianReport = ManagedDocument.builder()
            .uploaderType(DocumentUploaderType.CAFCASS)
            .document(documentReference)
            .build();

        List<Element<ManagedDocument>> updatedGuardianReports = caseData.getGuardianReportsList();
        updatedGuardianReports.add(element(guardianReport));

        return coreCaseDataService.performPostSubmitCallback(caseData.getId(), "internal-upload-document",
            caseDetails -> Map.of("guardianReportsList", updatedGuardianReports));
    }

    public boolean isValidFile(MultipartFile file) throws IOException {
        return !file.isEmpty() && List.of(MIME_TYPE_PDF, MIME_TYPE_PDF_X).contains(file.getContentType());
    }
}
