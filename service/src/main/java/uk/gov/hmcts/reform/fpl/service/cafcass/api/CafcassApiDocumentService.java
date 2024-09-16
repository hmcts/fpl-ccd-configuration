package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiDocumentService {
    private final SecureDocStoreService secureDocStoreService;
    private final UploadDocumentService uploadDocumentService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseConverter caseConverter;
    private final UserService userService;
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
        return format("%s-%s%s",
           typeOfDocument, ZonedDateTime.now(ZoneId.of("Europe/London")).format(DATE_TIME_FORMATTER), ".pdf");
    }

    public CaseDetails uploadGuardianReport(DocumentReference documentReference, long caseId) {
        return coreCaseDataService.performPostSubmitCallback(caseId, "internal-upload-document",
            caseDetails -> {
                CaseData caseData = getCaseData(caseDetails);
                ManagedDocument guardianReport = ManagedDocument.builder()
                    .uploaderType(CAFCASS)
                    .document(documentReference)
                    .build();

                List<Element<ManagedDocument>> updatedGuardianReports = caseData.getGuardianReportsList();
                updatedGuardianReports.add(element(guardianReport));

                return Map.of("guardianReportsList", updatedGuardianReports);
            });
    }

    public ManageDocumentsUploadedEvent generateDocumentUploadedEvent(DocumentReference documentReference,
                                                                      DocumentType documentType,
                                                                      CaseData caseData) {
        ManagedDocument documentUploaded = ManagedDocument.builder()
            .uploaderType(CAFCASS)
            .document(documentReference)
            .build();

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocument =
            Map.of(documentType, List.of(element(documentUploaded)));

        return ManageDocumentsUploadedEvent.builder()
            .caseData(caseData)
            .newDocuments(newDocument)
            .newDocumentsLA(new HashMap<>())
            .newDocumentsCTSC(new HashMap<>())
            .uploadedUserType(CAFCASS)
            .initiatedBy(userService.getUserDetails())
            .build();
    }

    public boolean isValidFile(MultipartFile file) throws IOException {
        return !file.isEmpty() && List.of(MIME_TYPE_PDF, MIME_TYPE_PDF_X).contains(file.getContentType());
    }

    private CaseData getCaseData(CaseDetails caseDetails) {
        return caseConverter.convert(caseDetails);
    }
}
