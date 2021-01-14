package uk.gov.hmcts.reform.fpl.controllers.support;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
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
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

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
    class Fpla2544 {
        String familyManCaseNumber = "PO20C50030";
        String migrationId = "FPLA-2544";

        @Test
        void shouldChangeCaseStatusAndPrePopulateSDODirections() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, SUBMITTED);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getState()).isEqualTo(State.GATEKEEPING);
            assertThat(extractedCaseData.getAllParties()).hasSize(5);
            assertThat(extractedCaseData.getAllPartiesCustom()).isNull();
            assertThat(extractedCaseData.getLocalAuthorityDirections()).hasSize(7);
            assertThat(extractedCaseData.getLocalAuthorityDirectionsCustom()).isNull();
            assertThat(extractedCaseData.getCourtDirections()).hasSize(1);
            assertThat(extractedCaseData.getCourtDirectionsCustom()).isNull();
            assertThat(extractedCaseData.getCafcassDirections()).hasSize(3);
            assertThat(extractedCaseData.getCafcassDirectionsCustom()).isNull();
            assertThat(extractedCaseData.getOtherPartiesDirections()).hasSize(1);
            assertThat(extractedCaseData.getOtherPartiesDirectionsCustom()).isNull();
            assertThat(extractedCaseData.getRespondentDirections()).hasSize(1);
            assertThat(extractedCaseData.getRespondentDirectionsCustom()).isNull();
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManNumber() {
            familyManCaseNumber = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, SUBMITTED);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertDirectionsUnchanged(caseDetails, extractedCaseData);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            migrationId = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, SUBMITTED);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertDirectionsUnchanged(caseDetails, extractedCaseData);
        }

        @ParameterizedTest
        @EnumSource(value = State.class, names = {"SUBMITTED"}, mode = EnumSource.Mode.EXCLUDE)
        void shouldThrowExceptionIfUnexpectedCaseState(State state) {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, state);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Case is in %s state, expected SUBMITTED", state));
        }

        private CaseDetails caseDetails(String familyManCaseNumber, String migrationId, State state) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .hearingDetails(wrapElements(HearingBooking.builder()
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .judgeLastName("Smith")
                        .judgeEmailAddress("judge@test.com")
                        .build())
                    .build()))
                .state(state)
                .build());
            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private void assertDirectionsUnchanged(CaseDetails caseDetails, CaseData updatedCaseData) {
            CaseData caseData = mapper.convertValue(caseDetails, CaseData.class);

            assertThat(updatedCaseData.getAllParties())
                .isEqualTo(caseData.getAllParties());
            assertThat(updatedCaseData.getAllPartiesCustom())
                .isEqualTo(caseData.getAllPartiesCustom());
            assertThat(updatedCaseData.getLocalAuthorityDirections())
                .isEqualTo(caseData.getLocalAuthorityDirections());
            assertThat(updatedCaseData.getLocalAuthorityDirectionsCustom())
                .isEqualTo(caseData.getLocalAuthorityDirectionsCustom());
            assertThat(updatedCaseData.getCourtDirections())
                .isEqualTo(caseData.getCourtDirections());
            assertThat(updatedCaseData.getCourtDirectionsCustom())
                .isEqualTo(caseData.getCourtDirectionsCustom());
            assertThat(updatedCaseData.getCafcassDirections())
                .isEqualTo(caseData.getCafcassDirections());
            assertThat(updatedCaseData.getCafcassDirectionsCustom())
                .isEqualTo(caseData.getCafcassDirectionsCustom());
            assertThat(updatedCaseData.getOtherPartiesDirections())
                .isEqualTo(caseData.getOtherPartiesDirections());
            assertThat(updatedCaseData.getOtherPartiesDirectionsCustom())
                .isEqualTo(caseData.getOtherPartiesDirectionsCustom());
            assertThat(updatedCaseData.getRespondentDirections())
                .isEqualTo(caseData.getRespondentDirections());
            assertThat(updatedCaseData.getRespondentDirectionsCustom())
                .isEqualTo(caseData.getRespondentDirectionsCustom());
        }
    }

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
    class Fpla2521 {
        Long caseNumber = 1599470847274974L;
        String migrationId = "FPLA-2521";
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

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, draftCaseManagementOrders, hearingBookings);
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

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, caseNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            Long incorrectCaseNumber = 1599470847274973L;

            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectCaseNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        Long caseNumber,
                                        List<Element<CaseManagementOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .id(caseNumber)
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

    @Nested
    class Fpla2573 {
        Long caseNumber = 1603717767912577L;
        String migrationId = "FPLA-2573";
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

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, draftCaseManagementOrders, hearingBookings);
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

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, caseNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            Long incorrectCaseNumber = 1599470847274973L;

            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectCaseNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        Long caseNumber,
                                        List<Element<CaseManagementOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .id(caseNumber)
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

    @Nested
    class Fpla2535 {
        Long caseNumber = 1607361111762499L;
        String migrationId = "FPLA-2535";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID childrenId = UUID.randomUUID();

        @Test
        void shouldRemoveFirstGeneratedOrderAndNotModifyChildren() {
            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssuedType("Yes")
                .finalOrderIssued("Yes")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderToBeRemoved = element(orderToBeRemovedId, generateOrder(CARE_ORDER, children));
            Element<GeneratedOrder> additionalOrder = element(orderTwoId, generateOrder(BLANK_ORDER));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, orderCollection, children);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
            assertThat(extractedCaseData.getHiddenOrders()).isEqualTo(List.of());
        }

        @Test
        void shouldUnsetFinalChildrenPropertiesWhenRemovingFinalOrder() {
            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssuedType("Yes")
                .finalOrderIssued("Yes")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderToBeRemoved = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));
            Element<GeneratedOrder> additionalOrder = element(orderTwoId, generateOrder(BLANK_ORDER));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<GeneratedOrder>> hiddenOrders = newArrayList(
                element(GeneratedOrder.builder().build()));

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, orderCollection, children);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getChildren1()).isEqualTo(List.of(
                element(childrenId, Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Tom")
                        .lastName("Wilson")
                        .build())
                    .finalOrderIssuedType(null)
                    .finalOrderIssued(null)
                    .build())));
            assertThat(extractedCaseData.getHiddenOrders()).isEqualTo(hiddenOrders);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<GeneratedOrder> orderToBeRemoved = element(orderToBeRemovedId, generateOrder(UPLOAD));
            Element<GeneratedOrder> additionalOrder = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<Child> childElement = element(childrenId, Child.builder()
                .finalOrderIssuedType("Test")
                .finalOrderIssued("Test")
                .build());

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<Child>> children = newArrayList(childElement);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, caseNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orderCollection);
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            Long incorrectCaseNumber = 1599470847274973L;

            Element<GeneratedOrder> orderToBeRemoved = element(orderToBeRemovedId, generateOrder(UPLOAD));
            Element<GeneratedOrder> additionalOrder = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<Child> childElement = element(childrenId, Child.builder()
                .finalOrderIssuedType("Test")
                .finalOrderIssued("Test")
                .build());

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<Child>> children = newArrayList(childElement);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectCaseNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orderCollection);
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainGeneratedOrders() {
            List<Element<Child>> children = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, null, children);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No generated orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        Long caseNumber,
                                        List<Element<GeneratedOrder>> orders,
                                        List<Element<Child>> children) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .id(caseNumber)
                .orderCollection(orders)
                .children1(children)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private GeneratedOrder generateOrder(GeneratedOrderType type, List<Element<Child>> linkedChildren) {
            return generateOrder(type).toBuilder()
                .children(linkedChildren)
                .build();
        }

        private GeneratedOrder generateOrder(GeneratedOrderType type) {
            return GeneratedOrder.builder()
                .type(getFullOrderType(type))
                .build();
        }
    }
}
