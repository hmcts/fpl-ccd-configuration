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
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
    class Fpla2525 {
        String familyManCaseNumber = "SN20C50028";
        String migrationId = "FPLA-2525";

        Element<HearingBooking> hearing1 = element(HearingBooking.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeLastName("Smith")
                .judgeEmailAddress("judge@test.com")
                .build())
            .build());

        Element<HearingBooking> hearing2 = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(1))
            .venue("7")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeLastName("Smith")
                .judgeEmailAddress("judge@test.com")
                .build())
            .build());

        @Test
        void removeCorrectHearingsFromTheCase() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).containsOnly(hearing2);
        }

        @Test
        void shouldNotRemoveHearingsIfNotExpectedFamilyManNumber() {
            familyManCaseNumber = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).containsOnly(hearing1, hearing2);
        }

        @Test
        void shouldNotRemoveHearingsIfNotExpectedMigrationId() {
            migrationId = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).containsOnly(hearing1, hearing2);
        }

        @Test
        void shouldThrowExceptionIfUnexpectedNumberOfHearings() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No hearings in a case");
        }

        @Test
        void shouldThrowExceptionIfHearingHasUnexpectedStartDate() {
            LocalDate invalidDate = LocalDate.of(1990, 1, 1);

            Element<HearingBooking> unexpectedHearing = element(hearing1.getValue().toBuilder()
                .startDate(invalidDate.atStartOfDay())
                .build());

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, unexpectedHearing, hearing2);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing date %s", invalidDate));
        }

        @Test
        void shouldThrowExceptionIfHearingHasUnexpectedEndDate() {
            LocalDate invalidDate = LocalDate.of(1990, 1, 1);

            Element<HearingBooking> unexpectedHearing = element(hearing1.getValue().toBuilder()
                .endDate(invalidDate.atStartOfDay())
                .build());

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, unexpectedHearing, hearing2);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing end date %s", invalidDate));
        }

        @Test
        void shouldThrowExceptionIfHearingHasUnexpectedVenue() {
            String invalidVenue = "0";

            Element<HearingBooking> unexpectedHearing = element(hearing1.getValue().toBuilder()
                .venue(invalidVenue)
                .build());

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, unexpectedHearing, hearing2);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing venue %s", invalidVenue));
        }

        @Test
        void shouldThrowExceptionIfHearingHasUnexpectedType() {
            HearingType invalidType = CASE_MANAGEMENT;

            Element<HearingBooking> unexpectedHearing = element(hearing1.getValue().toBuilder()
                .type(invalidType)
                .build());

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, unexpectedHearing, hearing2);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing type %s", CASE_MANAGEMENT));
        }

        @SafeVarargs
        private CaseDetails caseDetails(String familyManCaseNumber, String migrationId,
                                        Element<HearingBooking>... hearings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .hearingDetails(List.of(hearings))
                .build());
            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

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
    class Fpla2481 {
        String familyManCaseNumber = "LE20C50023";
        String migrationId = "FPLA-2481";

        @Test
        void shouldRemoveOthersPropertyIfOthersContainsFirstOtherPropertyOnly() {
            Others others = Others.builder()
                .firstOther(Other.builder()
                    .name("John Smith")
                    .telephone("07741172242")
                    .build())
                .build();

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, others);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOthers()).isNull();
        }

        @Test
        void shouldRemoveOthersPropertyIfOthersContainsFirstOtherAndEmptyAdditionalOthers() {
            Others others = Others.builder()
                .additionalOthers(List.of())
                .firstOther(Other.builder()
                    .name("John Smith")
                    .telephone("07741172242")
                    .build())
                .build();

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, others);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOthers()).isNull();
        }

        @Test
        void shouldRemoveFirstOtherPropertyOnlyIfCaseHasAdditionalOthers() {
            List<Element<Other>> additionalOthers = List.of(element(Other.builder()
                .name("Additional other 1")
                .build()));

            Others others = Others.builder()
                .additionalOthers(additionalOthers)
                .firstOther(Other.builder()
                    .name("John Smith")
                    .telephone("07741172242")
                    .build())
                .build();

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, others);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOthers().getFirstOther()).isNull();
            assertThat(extractedCaseData.getOthers().getAdditionalOthers()).isEqualTo(additionalOthers);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            List<Element<Other>> additionalOthers = List.of(element(Other.builder()
                .name("Additional other 1")
                .build()));

            Others others = Others.builder()
                .additionalOthers(additionalOthers)
                .firstOther(Other.builder()
                    .name("John Smith")
                    .telephone("07741172242")
                    .build())
                .build();

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, "FPLA-1111", others);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getFamilyManCaseNumber()).isEqualTo(familyManCaseNumber);
            assertThat(extractedCaseData.getOthers()).isEqualTo(others);
        }

        private CaseDetails caseDetails(String familyManCaseNumber, String migrationId, Others others) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .others(others)
                .build());
            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
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

}
