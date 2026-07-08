package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.GENOGRAM;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    ApplicationDocumentsService.class,
    ManageDocumentService.class,
    FixedTimeConfiguration.class,
    UserService.class
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

    @MockBean
    private ManageDocumentService manageDocumentService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private DocumentUploadHelper documentUploadHelper;

    @BeforeEach
    void setup() {
        when(documentUploadHelper.getUploadedDocumentUserDetails()).thenReturn(HMCTS_USER);
        when(userService.getCaseRoles(any())).thenReturn(Set.of());
        when(manageDocumentService.getUploaderType(any())).thenReturn(HMCTS);
    }

    @Test
    void shouldUpdateUploadedByAndDateTimeOnOldApplicationDocumentWhenModified() {
        ApplicationDocument document = buildApplicationDocument(PAST_DATE);
        List<Element<ApplicationDocument>> previousDocuments = List.of(element(document));

        UUID previousDocumentId = previousDocuments.get(0).getId();

        ApplicationDocument updatedDocument = ApplicationDocument.builder()
            .documentType(CHECKLIST_DOCUMENT)
            .document(buildDocumentReference(NEW_FILENAME)).build();

        List<Element<ApplicationDocument>> currentDocuments = List.of(buildApplicationDocumentElement(
            previousDocumentId,
            updatedDocument));

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(caseData(),
            currentDocuments, previousDocuments);
        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        ApplicationDocument actualDocument = actualCaseData.getTemporaryApplicationDocuments().get(0).getValue();
        ApplicationDocument expectedDocument = buildExpectedDocument(currentDocuments, HMCTS_USER, time.now(), HMCTS,
            List.of());

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
            .uploaderCaseRoles(List.of())
            .build();

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(caseData(),
            List.of(buildApplicationDocumentElement(previousDocumentId,
                pastDocument.toBuilder()
                    .document(buildDocumentReference(NEW_FILENAME))
                    .build())
        ), List.of(
            element(previousDocumentId, pastDocument)
        ));

        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        assertThat(actualCaseData.getTemporaryApplicationDocuments()).isEqualTo(List.of(
            element(previousDocumentId, pastDocument.toBuilder()
                .uploadedBy(HMCTS_USER)
                .dateTimeUploaded(time.now())
                .document(buildDocumentReference(NEW_FILENAME))
                .uploaderType(HMCTS)
                .uploaderCaseRoles(List.of())
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

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(caseData(),
            currentDocuments, previousDocuments);
        CaseData actualCaseData = mapper.convertValue(map, CaseData.class);

        ApplicationDocument actualDocument = actualCaseData.getTemporaryApplicationDocuments().get(0).getValue();
        ApplicationDocument expectedDocument = buildExpectedDocument(currentDocuments, LA_USER, PAST_DATE, HMCTS,
            List.of());

        assertThat(actualDocument).isEqualTo(expectedDocument);
    }

    @Test
    void shouldNotUpdateUploadedByAndDateTimeOnOldCaseDocumentsWhenNotModifiedAndNewDocumentAdded() {
        ApplicationDocument firstDocument = buildApplicationDocument(PAST_DATE);

        ApplicationDocument secondDocument = ApplicationDocument.builder()
            .documentType(CHECKLIST_DOCUMENT)
            .document(buildDocumentReference(NEW_FILENAME)).build();

        List<Element<ApplicationDocument>> currentDocuments = List.of(buildApplicationDocumentElement(DOCUMENT_ID,
            firstDocument), buildApplicationDocumentElement(UUID.randomUUID(), secondDocument));

        List<Element<ApplicationDocument>> previousDocuments = List.of(buildApplicationDocumentElement(DOCUMENT_ID,
            firstDocument));

        Map<String, Object> map = applicationDocumentsService.updateApplicationDocuments(caseData(),
            currentDocuments, previousDocuments);
        CaseData caseData = mapper.convertValue(map, CaseData.class);

        assertThat(caseData.getTemporaryApplicationDocuments().get(0).getValue()).isEqualTo(firstDocument);
    }

    private ApplicationDocument buildApplicationDocument(LocalDateTime time) {
        return ApplicationDocument.builder()
            .documentType(BIRTH_CERTIFICATE)
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
                                                      LocalDateTime dateTimeUploaded,
                                                      DocumentUploaderType uploaderType,
                                                      List<CaseRole> uploaderCaseRoles) {
        ApplicationDocument expectedDocument = documents.get(0).getValue();
        expectedDocument.setUploadedBy(uploadedBy);
        expectedDocument.setDateTimeUploaded(dateTimeUploaded);
        expectedDocument.setUploaderType(uploaderType);
        expectedDocument.setUploaderCaseRoles(uploaderCaseRoles);

        return expectedDocument;
    }

    @Nested
    class SynchroniseToNewFields {
        private final Map<ApplicationDocumentType, String> applicationDocumentTypeFieldNameMap = Map.of(
            THRESHOLD, "thresholdList",
            SWET, "documentsFiledOnIssueList",
            CARE_PLAN, "carePlanList",
            SOCIAL_WORK_CHRONOLOGY, "documentsFiledOnIssueList",
            SOCIAL_WORK_STATEMENT, "documentsFiledOnIssueList",
            GENOGRAM, "documentsFiledOnIssueList",
            CHECKLIST_DOCUMENT, "documentsFiledOnIssueList",
            BIRTH_CERTIFICATE, "documentsFiledOnIssueList",
            OTHER, "documentsFiledOnIssueList"
        );

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = ApplicationDocumentType.class, names = {
            "THRESHOLD", "SWET", "CARE_PLAN",
            "SOCIAL_WORK_CHRONOLOGY", "SOCIAL_WORK_STATEMENT",
            "GENOGRAM", "CHECKLIST_DOCUMENT", "BIRTH_CERTIFICATE", "OTHER"})
        void shouldMigrateApplicationDocumentUploaded(ApplicationDocumentType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().filename("abc").build();
            ApplicationDocument ad1 = ApplicationDocument.builder()
                .documentType(type)
                .document(document1)
                .build();

            Map<String, Object> updatedFields = applicationDocumentsService
                .synchroniseToNewFields(List.of(element(doc1Id, ad1)));

            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type) + "LA").asList()
                .isEmpty();
            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type)).asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @EnumSource(value = ApplicationDocumentType.class, names = {
            "THRESHOLD", "SWET", "CARE_PLAN",
            "SOCIAL_WORK_CHRONOLOGY", "SOCIAL_WORK_STATEMENT",
            "GENOGRAM", "CHECKLIST_DOCUMENT", "BIRTH_CERTIFICATE", "OTHER"})
        void shouldMigrateConfidentialApplicationDocumentUploaded(ApplicationDocumentType type) throws Exception {
            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().filename("abc").build();
            ApplicationDocument ad1 = ApplicationDocument.builder()
                .documentType(type)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            Map<String, Object> updatedFields = applicationDocumentsService.synchroniseToNewFields(
                List.of(element(doc1Id, ad1)));

            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type)).asList()
                .isEmpty();
            assertThat(updatedFields).extracting(applicationDocumentTypeFieldNameMap.get(type) + "LA").asList()
                .contains(element(doc1Id, ManagedDocument.builder().document(document1).build()));
        }

        @Test
        void shouldMigrateMixedApplicationDocuments() {
            UUID doc1Id = UUID.randomUUID();
            UUID doc2Id = UUID.randomUUID();
            UUID doc3Id = UUID.randomUUID();
            UUID doc4Id = UUID.randomUUID();
            UUID doc5Id = UUID.randomUUID();
            UUID doc6Id = UUID.randomUUID();
            UUID doc7Id = UUID.randomUUID();
            UUID doc8Id = UUID.randomUUID();
            UUID doc9Id = UUID.randomUUID();
            UUID doc10Id = UUID.randomUUID();
            UUID doc11Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().filename("abc").build();
            DocumentReference document2 = DocumentReference.builder().filename("def").build();
            DocumentReference document3 = DocumentReference.builder().filename("ghi").build();
            DocumentReference document4 = DocumentReference.builder().filename("jkl").build();
            DocumentReference document5 = DocumentReference.builder().filename("mno").build();
            DocumentReference document6 = DocumentReference.builder().filename("pqr").build();
            DocumentReference document7 = DocumentReference.builder().filename("stu").build();
            DocumentReference document8 = DocumentReference.builder().filename("vwx").build();
            DocumentReference document9 = DocumentReference.builder().filename("yza").build();
            DocumentReference document10 = DocumentReference.builder().filename("bcd").build();
            DocumentReference document11 = DocumentReference.builder().filename("fgh").build();

            ApplicationDocument ad1 = ApplicationDocument.builder()
                .documentType(THRESHOLD)
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            ApplicationDocument ad2 = ApplicationDocument.builder()
                .documentType(THRESHOLD)
                .document(document2)
                .build();
            ApplicationDocument ad3 = ApplicationDocument.builder()
                .documentType(SWET)
                .document(document3)
                .build();
            ApplicationDocument ad4 = ApplicationDocument.builder()
                .documentType(CARE_PLAN)
                .document(document4)
                .build();
            ApplicationDocument ad5 = ApplicationDocument.builder()
                .documentType(SOCIAL_WORK_CHRONOLOGY)
                .document(document5)
                .build();
            ApplicationDocument ad6 = ApplicationDocument.builder()
                .documentType(SOCIAL_WORK_STATEMENT)
                .document(document6)
                .build();
            ApplicationDocument ad7 = ApplicationDocument.builder()
                .documentType(GENOGRAM)
                .document(document7)
                .build();
            ApplicationDocument ad8 = ApplicationDocument.builder()
                .documentType(CHECKLIST_DOCUMENT)
                .document(document8)
                .build();
            ApplicationDocument ad9 = ApplicationDocument.builder()
                .documentType(BIRTH_CERTIFICATE)
                .document(document9)
                .confidential(List.of("CONFIDENTIAL"))
                .build();
            ApplicationDocument ad10 = ApplicationDocument.builder()
                .documentType(OTHER)
                .document(document10)
                .build();
            ApplicationDocument ad11 = ApplicationDocument.builder()
                .documentType(CARE_PLAN)
                .document(document11)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            Map<String, Object> updatedFields = applicationDocumentsService.synchroniseToNewFields(
                List.of(
                    element(doc1Id, ad1), element(doc2Id, ad2), element(doc3Id, ad3), element(doc4Id, ad4),
                    element(doc5Id, ad5), element(doc6Id, ad6), element(doc7Id, ad7), element(doc8Id, ad8),
                    element(doc9Id, ad9), element(doc10Id, ad10), element(doc11Id, ad11)
                )
            );

            assertThat(updatedFields).extracting("thresholdListLA").asList().contains(
                element(doc1Id, ManagedDocument.builder().document(document1).build()));
            assertThat(updatedFields).extracting("thresholdList").asList().contains(
                element(doc2Id, ManagedDocument.builder().document(document2).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc3Id, ManagedDocument.builder().document(document3).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc5Id, ManagedDocument.builder().document(document5).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc6Id, ManagedDocument.builder().document(document6).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc7Id, ManagedDocument.builder().document(document7).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc8Id, ManagedDocument.builder().document(document8).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueList").asList().contains(
                element(doc10Id, ManagedDocument.builder().document(document10).build()));
            assertThat(updatedFields).extracting("documentsFiledOnIssueListLA").asList().contains(
                element(doc9Id, ManagedDocument.builder().document(document9).build()));

            assertThat(updatedFields).extracting("carePlanList").asList()
                .contains(element(doc4Id, ManagedDocument.builder().document(document4).build()));
            assertThat(updatedFields).extracting("carePlanListLA").asList()
                .contains(element(doc11Id, ManagedDocument.builder().document(document11).build()));
        }
    }

    @Nested
    class RebuildTemporaryApplicationDocuments {

        @Test
        void shouldRebuildTemporaryDocumentsFromCarePlanDocs() {
            UUID carePlanID = UUID.randomUUID();
            ManagedDocument carePlan = ManagedDocument.builder()
                .document(DocumentReference.builder().filename("carePlan").build())
                .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
                .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                .build();

            CaseData caseData = CaseData.builder()
                .carePlanList(List.of(element(carePlanID, carePlan)))
                .build();

            List<Element<ApplicationDocument>> expectedApplicationDocuments = List.of(
                element(carePlanID,
                    ApplicationDocument.builder()
                        .document(carePlan.getDocument())
                        .documentType(CARE_PLAN)
                        .documentName("carePlan")
                        .uploaderCaseRoles(carePlan.getUploaderCaseRoles())
                        .uploaderType(carePlan.getUploaderType())
                        .build()
                ));

            List<Element<ApplicationDocument>> applicationDocuments = applicationDocumentsService
                .rebuildTemporaryApplicationDocuments(caseData);

            assertThat(applicationDocuments).hasSize(1);
            assertThat(applicationDocuments).isEqualTo(expectedApplicationDocuments);
        }

        @Test
        void shouldRebuildTemporaryDocumentsFromThresholdDocs() {
            UUID thresholdID = UUID.randomUUID();
            ManagedDocument threshold = ManagedDocument.builder()
                .document(DocumentReference.builder().filename("threshold").build())
                .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
                .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                .build();

            CaseData caseData = CaseData.builder()
                .thresholdList(List.of(element(thresholdID, threshold)))
                .build();

            List<Element<ApplicationDocument>> expectedApplicationDocuments = List.of(
                element(thresholdID,
                    ApplicationDocument.builder()
                        .document(threshold.getDocument())
                        .documentType(THRESHOLD)
                        .documentName("threshold")
                        .uploaderCaseRoles(threshold.getUploaderCaseRoles())
                        .uploaderType(threshold.getUploaderType())
                        .build()
                ));

            List<Element<ApplicationDocument>> applicationDocuments = applicationDocumentsService
                .rebuildTemporaryApplicationDocuments(caseData);

            assertThat(applicationDocuments).hasSize(1);
            assertThat(applicationDocuments).isEqualTo(expectedApplicationDocuments);
        }

        @Test
        void shouldRebuildTemporaryDocumentsFromDocumentsFiledOnIssue() {
            UUID docFiledOnIssueID = UUID.randomUUID();
            ManagedDocument docFiledOnIssue = ManagedDocument.builder()
                .document(DocumentReference.builder().filename("SWET").build())
                .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
                .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                .build();

            CaseData caseData = CaseData.builder()
                .documentsFiledOnIssueList(List.of(element(docFiledOnIssueID, docFiledOnIssue)))
                .build();

            List<Element<ApplicationDocument>> expectedApplicationDocuments = List.of(
                element(docFiledOnIssueID,
                    ApplicationDocument.builder()
                        .document(docFiledOnIssue.getDocument())
                        .documentType(OTHER)
                        .documentName("SWET")
                        .uploaderCaseRoles(docFiledOnIssue.getUploaderCaseRoles())
                        .uploaderType(docFiledOnIssue.getUploaderType())
                        .build()
                ));

            List<Element<ApplicationDocument>> applicationDocuments = applicationDocumentsService
                .rebuildTemporaryApplicationDocuments(caseData);

            assertThat(applicationDocuments).hasSize(1);
            assertThat(applicationDocuments).isEqualTo(expectedApplicationDocuments);
        }

        @Test
        void shouldRebuildTemporaryDocumentsFromConfidentialDocumentsFiledOnIssue() {
            UUID docFiledOnIssueID = UUID.randomUUID();
            ManagedDocument docFiledOnIssue = ManagedDocument.builder()
                .document(DocumentReference.builder().filename("SWET").build())
                .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
                .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                .build();

            CaseData caseData = CaseData.builder()
                .documentsFiledOnIssueListLA(List.of(element(docFiledOnIssueID, docFiledOnIssue)))
                .build();

            List<Element<ApplicationDocument>> expectedApplicationDocuments = List.of(
                element(docFiledOnIssueID,
                    ApplicationDocument.builder()
                        .document(docFiledOnIssue.getDocument())
                        .documentType(OTHER)
                        .documentName("SWET")
                        .confidential(List.of("CONFIDENTIAL"))
                        .uploaderCaseRoles(docFiledOnIssue.getUploaderCaseRoles())
                        .uploaderType(docFiledOnIssue.getUploaderType())
                        .build()
                ));

            List<Element<ApplicationDocument>> applicationDocuments = applicationDocumentsService
                .rebuildTemporaryApplicationDocuments(caseData);

            assertThat(applicationDocuments).hasSize(1);
            assertThat(applicationDocuments).isEqualTo(expectedApplicationDocuments);
        }

    }

}
