package uk.gov.hmcts.reform.fpl.controllers.support;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final UUID UUID_3 = UUID.randomUUID();
    private static final UUID UUID_4 = UUID.randomUUID();
    private static final UUID UUID_5 = UUID.randomUUID();
    private static final UUID UUID_6 = UUID.randomUUID();
    private static final UUID UUID_7 = UUID.randomUUID();
    private static final UUID UUID_8 = UUID.randomUUID();
    private static final UUID UUID_9 = UUID.randomUUID();
    private static final UUID UUID_10 = UUID.randomUUID();

    @MockBean
    private IdentityService identityService;

    @Nested
    class Fpla2379 {

        private static final String FILE_URL = "https://docuURL";
        private static final String FILE_NAME = "mockChecklist.pdf";
        private static final String FILE_BINARY_URL = "http://dm-store:8080/documents/fakeUrl/binary";
        private static final String USER = "kurt@swansea.gov.uk";
        private static final String HEARING_INFOS = "Hearing infos";

        private String migrationId = "FPLA-2379";

        @Test
        void shouldNotMigrateOtherMigrationId() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "migrationId", "anotherId"
                )).build();

            AboutToStartOrSubmitCallbackResponse actual = postAboutToSubmitEvent(caseDetails);

            assertThat(actual.getData()).isEmpty();
        }

        @Test
        void shouldMigrateEmptyFields() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "migrationId", migrationId
                )).build();

            HashMap<Object, Object> expectedData = Maps.newHashMap();
            expectedData.put("applicationDocuments", Lists.emptyList());
            expectedData.put("applicationDocumentsToFollowReason", "");
            expectedData.put("courtBundleList", Lists.emptyList());

            AboutToStartOrSubmitCallbackResponse actual = postAboutToSubmitEvent(caseDetails);

            assertThat(actual.getData()).isEqualTo(expectedData);
        }

        @Test
        void shouldMigrateWithFields() {

            when(identityService.generateId())
                .thenReturn(UUID_1)
                .thenReturn(UUID_2)
                .thenReturn(UUID_3)
                .thenReturn(UUID_4)
                .thenReturn(UUID_5)
                .thenReturn(UUID_6)
                .thenReturn(UUID_7)
                .thenReturn(UUID_8);

            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "documents_socialWorkChronology_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_socialWorkStatement_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_socialWorkCarePlan_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_socialWorkEvidenceTemplate_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_socialWorkAssessment_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_threshold_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_checklist_document", oldStructureDoc(ATTACHED.getLabel(), USER),
                    "documents_socialWorkOther", List.of(
                        element(UUID_9, oldStructureDocOther("Other Document 1", USER)),
                        element(UUID_10, oldStructureDocOther("Other Document 2", USER))
                    ),
                    "courtBundle", courtBundle(),
                    "migrationId", migrationId
                )).build();

            HashMap<Object, Object> expectedData = Maps.newHashMap();
            expectedData.put("documents_socialWorkChronology_document", oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_socialWorkStatement_document", oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_socialWorkCarePlan_document", oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_socialWorkEvidenceTemplate_document",
                oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_socialWorkAssessment_document", oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_threshold_document", oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_checklist_document", oldStructureDoc(ATTACHED.getLabel(), USER));
            expectedData.put("documents_socialWorkOther", List.of(
                element(UUID_9, oldStructureDocOther("Other Document 1", USER)),
                element(UUID_10, oldStructureDocOther("Other Document 2", USER))
            ));
            expectedData.put("courtBundle", courtBundle());
            expectedData.put("applicationDocuments", List.of(
                element(UUID_1, newApplicationDocument("SOCIAL_WORK_CHRONOLOGY")),
                element(UUID_2, newApplicationDocument("SOCIAL_WORK_STATEMENT")),
                element(UUID_3, newApplicationDocument("CARE_PLAN")),
                element(UUID_4, newApplicationDocument("SWET")),
                element(UUID_5, newApplicationDocument("SOCIAL_WORK_STATEMENT")),
                element(UUID_6, newApplicationDocument("THRESHOLD")),
                element(UUID_7, newApplicationDocument("CHECKLIST_DOCUMENT")),
                element(UUID_9, newApplicationDocumentOther("Other Document 1")),
                element(UUID_10, newApplicationDocumentOther("Other Document 2"))
            ));
            expectedData.put("courtBundleList", List.of(element(UUID_8, courtBundle())));
            expectedData.put("applicationDocumentsToFollowReason", "");

            AboutToStartOrSubmitCallbackResponse actual = postAboutToSubmitEvent(caseDetails);

            assertThat(actual.getData()).isEqualTo(expectedData);
        }

        @Test
        void shouldMigrateWithFieldsToFollow() {

            when(identityService.generateId()).thenReturn(UUID_1);

            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "documents_socialWorkChronology_document", oldStructureDocToFollow(),
                    "documents_socialWorkStatement_document", oldStructureDocToFollow(),
                    "documents_socialWorkCarePlan_document", oldStructureDocToFollow(),
                    "documents_socialWorkEvidenceTemplate_document", oldStructureDocToFollow(),
                    "documents_socialWorkAssessment_document", oldStructureDocToFollow(),
                    "documents_threshold_document", oldStructureDocToFollow(),
                    "documents_checklist_document", oldStructureDocToFollow(),
                    "courtBundle", courtBundle(),
                    "migrationId", migrationId
                )).build();

            HashMap<Object, Object> expectedData = Maps.newHashMap();
            expectedData.put("documents_socialWorkChronology_document", oldStructureDocToFollow());
            expectedData.put("documents_socialWorkStatement_document", oldStructureDocToFollow());
            expectedData.put("documents_socialWorkCarePlan_document", oldStructureDocToFollow());
            expectedData.put("documents_socialWorkEvidenceTemplate_document", oldStructureDocToFollow());
            expectedData.put("documents_socialWorkAssessment_document", oldStructureDocToFollow());
            expectedData.put("documents_threshold_document", oldStructureDocToFollow());
            expectedData.put("documents_checklist_document", oldStructureDocToFollow());
            expectedData.put("courtBundle", courtBundle());
            expectedData.put("applicationDocuments", List.of());
            expectedData.put("courtBundleList", List.of(element(UUID_1, courtBundle())));
            expectedData.put("applicationDocumentsToFollowReason",
                "Social work chronology to follow, "
                    + "Social work statement to follow, "
                    + "Care plan to follow, "
                    + "SWET to follow, "
                    + "Social work statement to follow, "
                    + "Threshold to follow, "
                    + "Checklist document to follow"
            );

            AboutToStartOrSubmitCallbackResponse actual = postAboutToSubmitEvent(caseDetails);

            assertThat(actual.getData()).isEqualTo(expectedData);
        }

        private Map<String, Object> element(UUID uuid, Map<String, Object> appDocument) {
            return Map.of(
                "id", uuid.toString(),
                "value", appDocument
            );
        }

        private Map<String, Object> newApplicationDocument(String documentType) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("dateTimeUploaded", "2020-12-03T02:03:04.00001");
            doc.put("uploadedBy", USER);
            doc.put("documentType", documentType);
            doc.put("includedInSWET", null);
            doc.put("documentName", null);
            doc.put("document", Map.of(
                "document_url", FILE_URL,
                "document_filename", FILE_NAME,
                "document_binary_url", FILE_BINARY_URL
            ));
            return doc;
        }

        private Map<String, Object> newApplicationDocumentOther(String documentTitle) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("dateTimeUploaded", "2020-12-03T02:03:04.00001");
            doc.put("uploadedBy", USER);
            doc.put("documentType", "OTHER");
            doc.put("includedInSWET", null);
            doc.put("documentName", documentTitle);
            doc.put("document", Map.of(
                "document_url", FILE_URL,
                "document_filename", FILE_NAME,
                "document_binary_url", FILE_BINARY_URL
            ));
            return doc;
        }

        private Map<String, Object> courtBundle() {
            return Map.of(
                "hearing", HEARING_INFOS,
                "dateTimeUploaded", "2020-12-03T02:03:04.00001",
                "uploadedBy", USER,
                "document", Map.of(
                    "document_url", FILE_URL,
                    "document_filename", FILE_NAME,
                    "document_binary_url", FILE_BINARY_URL
                )
            );
        }

        private Map<String, String> oldStructureDocToFollow() {
            return Map.of(
                "documentStatus", TO_FOLLOW.getLabel(),
                "uploadedBy", USER
            );
        }

        private Map<String, Object> oldStructureDoc(String attachedLabel, String user) {
            return Map.of(
                "documentStatus", attachedLabel,
                "uploadedBy", user,
                "dateTimeUploaded", "2020-12-03T02:03:04.00001",
                "typeOfDocument", Map.of(
                    "document_url", FILE_URL,
                    "document_filename", FILE_NAME,
                    "document_binary_url", FILE_BINARY_URL
                )
            );
        }

        private Map<String, Object> oldStructureDocOther(String title, String user) {
            return Map.of(
                "documentTitle", title,
                "uploadedBy", user,
                "dateTimeUploaded", "2020-12-03T02:03:04.00001",
                "typeOfDocument", Map.of(
                    "document_url", FILE_URL,
                    "document_filename", FILE_NAME,
                    "document_binary_url", FILE_BINARY_URL
                )
            );
        }
    }

    @Nested
    class Fpla2589 {
        String errorMessage = "Expected 2 draft case management orders but found %s";
        String familyManNumber = "PO20C50030";
        String migrationId = "FPLA-2589";
        UUID orderOneId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID orderThreeId = UUID.randomUUID();
        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();
        UUID hearingThreeId = UUID.randomUUID();

        CaseManagementOrder cmo = CaseManagementOrder.builder().build();

        @Test
        void shouldRemoveFirstTwoDraftCaseManagementOrdersAndUnlinkHearings() {
            Element<CaseManagementOrder> draftCmoOne = element(orderOneId, cmo);
            Element<CaseManagementOrder> draftCmoTwo = element(orderTwoId, cmo);
            Element<CaseManagementOrder> draftCmoThree = element(orderThreeId, cmo);

            Element<HearingBooking> hearingOne = element(hearingOneId, hearing(orderOneId));
            Element<HearingBooking> hearingTwo = element(hearingTwoId, hearing(orderTwoId));
            Element<HearingBooking> hearingThree = element(hearingThreeId, hearing(orderThreeId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                draftCmoOne,
                draftCmoTwo,
                draftCmoThree);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo, hearingThree);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(draftCmoThree));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(hearingOneId, hearing(null)),
                element(hearingTwoId, hearing(null)),
                hearingThree));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> draftCmoOne = element(orderOneId, cmo);
            Element<CaseManagementOrder> draftCmoTwo = element(orderTwoId, cmo);
            Element<CaseManagementOrder> draftCmoThree = element(orderThreeId, cmo);

            Element<HearingBooking> hearingOne = element(hearingOneId, hearing(orderOneId));
            Element<HearingBooking> hearingTwo = element(hearingTwoId, hearing(orderTwoId));
            Element<HearingBooking> hearingThree = element(hearingThreeId, hearing(orderThreeId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                draftCmoOne,
                draftCmoTwo,
                draftCmoThree);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo, hearingThree);

            CaseDetails caseDetails = caseDetails(
                incorrectMigrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> draftCmoOne = element(orderOneId, cmo);
            Element<CaseManagementOrder> draftCmoTwo = element(orderTwoId, cmo);
            Element<CaseManagementOrder> draftCmoThree = element(orderThreeId, cmo);

            Element<HearingBooking> hearingOne = element(hearingOneId, hearing(orderOneId));
            Element<HearingBooking> hearingTwo = element(hearingTwoId, hearing(orderTwoId));
            Element<HearingBooking> hearingThree = element(hearingThreeId, hearing(orderThreeId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                draftCmoOne,
                draftCmoTwo,
                draftCmoThree);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo, hearingThree);

            CaseDetails caseDetails = caseDetails(
                migrationId, invalidFamilyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsOnlyOneDraftCaseManagementOrder() {
            Element<CaseManagementOrder> draftCmoOne = element(orderOneId, cmo);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(draftCmoOne);

            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 1));
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 0));
        }
    }

    @Nested
    class Fpla2593 {
        String errorMessage = "Expected 2 draft case management orders but found %s";
        String familyManNumber = "CF20C50030";
        String migrationId = "FPLA-2593";
        UUID orderOneId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();
        CaseManagementOrder cmo = CaseManagementOrder.builder().build();

        @Test
        void shouldRemoveSecondDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<CaseManagementOrder> firstOrder = element(orderOneId, cmo);
            Element<CaseManagementOrder> secondOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingOne = element(hearingOneId, hearing(orderOneId));
            Element<HearingBooking> hearingTwo = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                firstOrder,
                secondOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(firstOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                hearingOne,
                element(hearingTwoId, hearing(null))));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> firstOrder = element(orderOneId, cmo);
            Element<CaseManagementOrder> secondOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingOne = element(hearingOneId, hearing(orderOneId));
            Element<HearingBooking> hearingTwo = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                firstOrder,
                secondOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(
                incorrectMigrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> firstOrder = element(orderOneId, cmo);
            Element<CaseManagementOrder> secondOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingOne = element(hearingOneId, hearing(orderOneId));
            Element<HearingBooking> hearingTwo = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                firstOrder,
                secondOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(
                migrationId, invalidFamilyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsOnlyOneDraftCaseManagementOrder() {
            Element<CaseManagementOrder> draftCmoOne = element(orderOneId, cmo);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(draftCmoOne);

            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 1));
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 0));
        }
    }

    @Nested
    class Fpla2599 {
        String familyManNumber = "SA20C50016";
        String migrationId = "FPLA-2599";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();
        CaseManagementOrder cmo = CaseManagementOrder.builder().build();

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(hearingOneId, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, invalidFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }
    }

    private CaseDetails caseDetails(String migrationId,
                                    String familyManCaseNumber,
                                    List<Element<CaseManagementOrder>> draftCaseManagementOrders,
                                    List<Element<HearingBooking>> hearingBookings) {
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .familyManCaseNumber(familyManCaseNumber)
            .draftUploadedCMOs(draftCaseManagementOrders)
            .hearingDetails(hearingBookings)
            .build());

        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    private HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .caseManagementOrderId(cmoId)
            .build();
    }
}
