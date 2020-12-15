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
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.Month.FEBRUARY;
import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
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
    class Fpla2491 {
        String familyManCaseNumber = "SN20C50018";
        String migrationId = "FPLA-2491";

        Element<HearingBooking> hearing1 = buildHearing(2020, NOVEMBER, 19, CASE_MANAGEMENT);
        Element<HearingBooking> hearing2 = buildHearing(2021, FEBRUARY, 5, FURTHER_CASE_MANAGEMENT);
        Element<HearingBooking> hearing3 = buildHearing(2021, MARCH, 4, ISSUE_RESOLUTION);
        Element<HearingBooking> hearing4 = buildHearing(2021, FEBRUARY, 5, FURTHER_CASE_MANAGEMENT);
        Element<HearingBooking> hearing5 = buildHearing(2021, MARCH, 4, ISSUE_RESOLUTION);

        @Test
        void removeCorrectHearingsFromTheCase() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, hearing5);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).containsOnly(hearing1, hearing2, hearing3);
        }

        @Test
        void shouldNotRemoveHearingsIfNotExpectedFamilyManNumber() {
            familyManCaseNumber = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, hearing5);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails())
                .containsOnly(hearing1, hearing2, hearing3, hearing4, hearing5);
        }

        @Test
        void shouldNotRemoveHearingsIfNotExpectedMigrationId() {
            migrationId = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, hearing5);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails())
                .containsOnly(hearing1, hearing2, hearing3, hearing4, hearing5);
        }

        @Test
        void shouldThrowExceptionIfUnexpectedNumberOfHearings() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Case has 3 hearing(s), expected at least 5");
        }

        @Test
        void shouldThrowExceptionIfHearing4HasUnexpectedDate() {
            LocalDate invalidDate = LocalDate.of(1990, 1, 1);

            Element<HearingBooking> unexpectedHearing4 = buildHearing(invalidDate, FURTHER_CASE_MANAGEMENT);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                unexpectedHearing4, hearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing date %s", invalidDate));
        }

        @Test
        void shouldThrowExceptionIfHearing5HasUnexpectedDate() {
            LocalDate invalidDate = LocalDate.of(1999, 1, 1);

            Element<HearingBooking> unexpectedHearing5 = buildHearing(invalidDate, FURTHER_CASE_MANAGEMENT);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, unexpectedHearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing date %s", invalidDate));
        }

        @Test
        void shouldThrowExceptionIfHearing4HasUnexpectedType() {
            HearingType invalidType = ISSUE_RESOLUTION;

            Element<HearingBooking> unexpectedHearing4 = buildHearing(2021, FEBRUARY, 5, invalidType);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                unexpectedHearing4, hearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing type %s", invalidType));
        }

        @Test
        void shouldThrowExceptionIfHearing5HasUnexpectedType() {
            HearingType invalidType = FURTHER_CASE_MANAGEMENT;

            Element<HearingBooking> unexpectedHearing5 = buildHearing(2021, MARCH, 4, invalidType);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, unexpectedHearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing type %s", invalidType));
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

        private Element<HearingBooking> buildHearing(int year, Month month, int day, HearingType type) {
            return buildHearing(LocalDate.of(year, month, day), type);
        }

        private Element<HearingBooking> buildHearing(LocalDate date, HearingType type) {
            return ElementUtils.element(HearingBooking.builder()
                .startDate(LocalDateTime.of(date, LocalTime.now()))
                .type(type)
                .build());
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
    class Fpla2501 {

        final String migrationId = "FPLA-2501";
        final String caseName = "test name";

        @Test
        void shouldRemoveLegacyFields() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "caseName", caseName,
                    "respondents", List.of(element(Respondent.builder()
                        .party(RespondentParty.builder().lastName("Wilson").build())
                        .build())),
                    "children", List.of(element(Child.builder()
                        .party(ChildParty.builder().lastName("Smith").build())
                        .build())),
                    "applicant", List.of(element(Applicant.builder()
                        .party(ApplicantParty.builder().lastName("White").build())
                        .build())),
                    "migrationId", migrationId))
                .build();

            Map<String, Object> extractedCaseData = postAboutToSubmitEvent(caseDetails).getData();

            assertThat(extractedCaseData).isEqualTo(Map.of("caseName", caseName));
        }

        @Test
        void shouldRemoveMigrationIdOnlyIfRespondentsAndChildrenFiledNotPresent() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "caseName", caseName,
                    "migrationId", migrationId))
                .build();

            Map<String, Object> extractedCaseData = postAboutToSubmitEvent(caseDetails).getData();

            assertThat(extractedCaseData).isEqualTo(Map.of("caseName", caseName));
        }
    }
}
