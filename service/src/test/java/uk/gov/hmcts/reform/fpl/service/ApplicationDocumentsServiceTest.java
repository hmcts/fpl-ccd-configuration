package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    ApplicationDocumentsService.class,
    FixedTimeConfiguration.class
})
class ApplicationDocumentsServiceTest {

    private static final String HMCTS_USER = "HMCTS";
    private static final String LA_USER = "kurt@swansea.gov.uk";
    private static final String OLD_FILENAME = "Old file";
    private static final String NEW_FILENAME = "New file";
    private static final LocalDateTime PAST_DATE = LocalDateTime.now().minusDays(2);
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String DOCUMENT_NAME = "documentName";
    private static final String INCLUDED_IN_SWET = "includedInSwet";
    private static final ApplicationDocumentType DOCUMENT_TYPE = ApplicationDocumentType.CHECKLIST_DOCUMENT;

    @Autowired
    private Time time;

    @Autowired
    private ApplicationDocumentsService applicationDocumentsService;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private DocumentUploadHelper documentUploadHelper;

    @BeforeEach
    void setup() {
        when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn(HMCTS_USER);
    }

    @Test
    void shouldSetUploadedByAndDateTimeOnNewApplicationDocument() {
        CaseData caseData = caseData();

        List<Element<ApplicationDocument>> previousDocuments = emptyCaseData().getApplicationDocuments();
        List<Element<ApplicationDocument>> documents = caseData.getApplicationDocuments();

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(documents, previousDocuments);
        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        ApplicationDocument actualDocument = actualCaseData.getApplicationDocuments().get(0).getValue();
        ApplicationDocument expectedDocument = buildExpectedDocument(caseData.getApplicationDocuments(), HMCTS_USER,
            time.now());

        assertThat(actualDocument).isEqualTo(expectedDocument);
    }

    @Test
    void shouldUpdateUploadedByAndDateTimeOnOldApplicationDocumentWhenModified() {
        ApplicationDocument document = buildApplicationDocument(PAST_DATE);
        List<Element<ApplicationDocument>> previousDocuments = List.of(element(document));

        UUID previousDocumentId = previousDocuments.get(0).getId();

        ApplicationDocument updatedDocument = ApplicationDocument.builder()
            .document(buildDocumentReference(NEW_FILENAME)).build();

        List<Element<ApplicationDocument>> currentDocuments = List.of(buildApplicationDocumentElement(
            previousDocumentId,
            updatedDocument));

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(currentDocuments,
            previousDocuments);
        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        ApplicationDocument actualDocument = actualCaseData.getApplicationDocuments().get(0).getValue();
        ApplicationDocument expectedDocument = buildExpectedDocument(currentDocuments, HMCTS_USER, time.now());

        assertThat(actualDocument).isEqualTo(expectedDocument);
    }

    @Test
    void shouldUpdateUploadedByAndDateTimeOnOldApplicationDocumentWhenFileNotPresent() {

        UUID previousDocumentId = UUID.randomUUID();

        ApplicationDocument pastDocument = ApplicationDocument.builder()
            .uploadedBy(LA_USER)
            .dateTimeUploaded(PAST_DATE)
            .documentType(DOCUMENT_TYPE)
            .documentName(DOCUMENT_NAME)
            .includedInSWET(INCLUDED_IN_SWET)
            .build();

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(List.of(
            buildApplicationDocumentElement(previousDocumentId,
                pastDocument.toBuilder()
                    .document(buildDocumentReference(NEW_FILENAME))
                    .build())
        ), List.of(
            element(previousDocumentId, pastDocument)
        ));

        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        assertThat(actualCaseData.getApplicationDocuments()).isEqualTo(List.of(
            element(previousDocumentId, pastDocument.toBuilder()
                .uploadedBy(HMCTS_USER)
                .dateTimeUploaded(time.now())
                .document(buildDocumentReference(NEW_FILENAME))
                .build()
            )
        ));
    }

    @Test
    void shouldNotUpdateUploadedByAndDateTimeOnOldApplicationDocumentWhenNotModified() {
        ApplicationDocument document = buildApplicationDocument(PAST_DATE);

        List<Element<ApplicationDocument>> currentDocuments = List.of(buildApplicationDocumentElement(DOCUMENT_ID,
            document));

        List<Element<ApplicationDocument>> previousDocuments = List.of(buildApplicationDocumentElement(DOCUMENT_ID,
            document));

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(currentDocuments,
            previousDocuments);
        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        ApplicationDocument actualDocument = actualCaseData.getApplicationDocuments().get(0).getValue();
        ApplicationDocument expectedDocument = buildExpectedDocument(currentDocuments, LA_USER, PAST_DATE);

        assertThat(actualDocument).isEqualTo(expectedDocument);
    }

    @Test
    void shouldNotUpdateUploadedByAndDateTimeOnOldCaseDocumentsWhenNotModifiedAndNewDocumentAdded() {
        ApplicationDocument firstDocument = buildApplicationDocument(PAST_DATE);

        ApplicationDocument secondDocument = ApplicationDocument.builder()
            .document(buildDocumentReference(NEW_FILENAME)).build();

        List<Element<ApplicationDocument>> currentDocuments = List.of(buildApplicationDocumentElement(DOCUMENT_ID,
            firstDocument), buildApplicationDocumentElement(UUID.randomUUID(), secondDocument));

        List<Element<ApplicationDocument>> previousDocuments = List.of(buildApplicationDocumentElement(DOCUMENT_ID,
            firstDocument));

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(currentDocuments,
            previousDocuments);
        CaseData caseData = mapper.convertValue(map, CaseData.class);

        assertThat(caseData.getApplicationDocuments().get(0).getValue()).isEqualTo(firstDocument);
    }

    private ApplicationDocument buildApplicationDocument(LocalDateTime time) {
        return ApplicationDocument.builder()
            .document(buildDocumentReference(OLD_FILENAME))
            .uploadedBy(LA_USER)
            .dateTimeUploaded(time).build();
    }

    private Element<ApplicationDocument> buildApplicationDocumentElement(UUID id, ApplicationDocument document) {
        return Element.<ApplicationDocument>builder()
            .id(id)
            .value(document).build();
    }

    private DocumentReference buildDocumentReference(String filename) {
        return DocumentReference.builder()
            .filename(filename)
            .build();
    }

    private ApplicationDocument buildExpectedDocument(List<Element<ApplicationDocument>> documents, String uploadedBy,
                                                      LocalDateTime dateTimeUploaded) {
        ApplicationDocument expectedDocument = documents.get(0).getValue();
        expectedDocument.setUploadedBy(uploadedBy);
        expectedDocument.setDateTimeUploaded(dateTimeUploaded);

        return expectedDocument;
    }
}
