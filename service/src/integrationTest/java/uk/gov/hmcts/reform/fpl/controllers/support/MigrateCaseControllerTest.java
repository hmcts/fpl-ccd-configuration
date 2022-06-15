package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @MockBean
    private TaskListService taskListService;

    @MockBean
    private TaskListRenderer taskListRenderer;

    @MockBean
    private CaseSubmissionChecker caseSubmissionChecker;

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl82 {
        private final String migrationId = "DFPL-82";

        private CourtBundle createCourtBundle(String hearing, String fileName, String fileUrl, String binaryUrl) {
            return CourtBundle.builder()
                .hearing(hearing)
                .document(DocumentReference.builder()
                    .filename(fileName)
                    .url(fileUrl)
                    .binaryUrl(binaryUrl)
                    .build())
                .confidential(List.of())
                .build();
        }

        @Test
        void shouldPerformMigration() {
            UUID hearingUUID = UUID.randomUUID();
            UUID hearing2UUID = UUID.randomUUID();

            Element<CourtBundle> courtBundle1 = element(hearingUUID, createCourtBundle("hearing 1",
                "doc1", "url", "binaryUrl"));
            Element<CourtBundle> courtBundle2 = element(hearingUUID, createCourtBundle("hearing 1",
                "doc3", "url3", "binaryUrl3"));
            Element<CourtBundle> courtBundle3 = element(hearing2UUID, createCourtBundle("hearing 2",
                "doc2", "url2", "binaryUrl2"));

            List<Element<CourtBundle>> courtBundles = List.of(courtBundle1, courtBundle2, courtBundle3);

            CaseData caseData = CaseData.builder()
                .id(1L)
                .state(State.SUBMITTED)
                .courtBundleList(courtBundles).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );


            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getCourtBundleList()).isNull();
            assertThat(responseData.getCourtBundleListV2())
                .extracting(Element::getValue)
                .containsExactlyInAnyOrder(
                    HearingCourtBundle.builder()
                        .hearing("hearing 1")
                        .courtBundle(List.of(courtBundle1, courtBundle2))
                        .courtBundleNC(List.of(courtBundle1, courtBundle2))
                        .build(),
                    HearingCourtBundle.builder()
                        .hearing("hearing 2")
                        .courtBundle(List.of(courtBundle3))
                        .courtBundleNC(List.of(courtBundle3))
                        .build()
                );

            // now roll back the migration
            String rollBackMigrationId = "DFPL-82-rollback";
            AboutToStartOrSubmitCallbackResponse rollBackResponse = postAboutToSubmitEvent(
                buildCaseDetails(responseData, rollBackMigrationId)
            );

            CaseData rollbackResponseData = extractCaseData(rollBackResponse);
            assertThat(rollbackResponseData.getCourtBundleListV2()).isEmpty();
            assertThat(rollbackResponseData.getCourtBundleList())
                .containsExactlyInAnyOrder(courtBundle1, courtBundle2, courtBundle3);
        }

        @Test
        void shouldSkipMigration() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .state(State.SUBMITTED)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );


            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getCourtBundleList()).isNull();
            assertThat(responseData.getCourtBundleListV2()).isEmpty();
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl451 {

        private final String migrationId = "DFPL-451";
        private final long validCaseId = 1603370139459131L;
        private final long invalidCaseId = 1626258358022000L;

        @Test
        void shouldThrowAssertionErrorWhenCaseIdIsInvalid() {

            CaseDetails caseDetails = CaseDetails.builder()
                .id(invalidCaseId)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "hearingOption", HearingOptions.NEW_HEARING,
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-451, case reference = 1626258358022000},"
                    + " Unexpected case reference");
        }

        @ParameterizedTest
        @ValueSource(
            longs = {1603370139459131L, 1618403849028418L, 1592492643062277L, 1615809514849016L, 1605537316992153L})
        void shouldRemoveHearingOptionIfPresent(Long caseId) {

            CaseDetails caseDetails = CaseDetails.builder()
                .id(caseId)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "hearingOption", HearingOptions.EDIT_PAST_HEARING,
                    "migrationId", migrationId))
                .build();

            Map<String, Object> expected = new HashMap<>(caseDetails.getData());
            expected.remove("hearingOption");
            expected.remove("migrationId");

            Map<String, Object> response = postAboutToSubmitEvent(caseDetails).getData();

            assertThat(response).isEqualTo(expected);
        }

        @Test
        void shouldRemoveMigrationIdWhenHearingOptionNotPresent() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(validCaseId)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails).getData());
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl500 {
        private final String migrationId = "DFPL-500";
        private final long validCaseId = 1643728359576136L;
        private final long invalidCaseId = 1626258358022000L;

        @Test
        void shouldPerformMigrationWhenNameMatches() {
            List<UUID> uuidsToBeRetained = List.of(UUID.randomUUID(), UUID.randomUUID());
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .documentsSentToParties(
                    wrapElements(
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        uuidsToBeRetained.get(0),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRetained.doc")
                                                    .build()
                                            ).build()
                                    ),
                                    element(
                                        UUID.fromString("ad5c738e-d7aa-4ccf-b53b-0b1e40a19182"),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRemoved.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build(),
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        uuidsToBeRetained.get(1),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRetained.doc")
                                                    .build()
                                            ).build()
                                    ),
                                    element(
                                        UUID.fromString("61f97374-360b-4759-9329-af10fae1317e"),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRemoved.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build()
                    )
                ).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            List<Element<SentDocuments>> documentsSentToParties = responseData.getDocumentsSentToParties();

            List<UUID> retainedUUIDs = documentsSentToParties.stream()
                .map(Element::getValue)
                .flatMap(value -> value.getDocumentsSentToParty().stream())
                .map(Element::getId)
                .collect(toList());

            assertThat(retainedUUIDs).isEqualTo(uuidsToBeRetained);
        }

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                .legalRepresentatives(
                    wrapElements(
                        LegalRepresentative.builder()
                            .fullName("First User")
                            .email("first@gamil.com")
                            .build(),
                        LegalRepresentative.builder()
                            .fullName("Second User")
                            .email("second@gamil.com")
                            .build()
                    )
                )
                .build();
            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-500, case reference = 1626258358022000},"
                    + " expected case id 1643728359576136");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl482 {
        private final String migrationId = "DFPL-482";
        private final long validCaseId = 1636970654155393L;
        private final long invalidCaseId = 1643728359576136L;

        @Test
        void shouldPerformMigrationWhenNameMatches() {
            List<UUID> uuidsToBeRetained = List.of(UUID.randomUUID(), UUID.randomUUID());
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .documentsSentToParties(
                    wrapElements(
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        uuidsToBeRetained.get(0),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRetained1.doc")
                                                    .build()
                                            ).build()
                                    ),
                                    element(
                                        UUID.fromString("75dcdc34-7f13-4c56-aad6-8dcf7b2261b6"),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRemoved1.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build(),
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        uuidsToBeRetained.get(1),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRetained2.doc")
                                                    .build()
                                            ).build()
                                    ),
                                    element(
                                        UUID.fromString("401d9cd0-50ae-469d-b355-d467742d7ef3"),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRemoved2.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build()
                    )
                ).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            List<Element<SentDocuments>> documentsSentToParties = responseData.getDocumentsSentToParties();

            List<UUID> retainedUUIDs = documentsSentToParties.stream()
                .map(Element::getValue)
                .flatMap(value -> value.getDocumentsSentToParty().stream())
                .map(Element::getId)
                .collect(toList());

            assertThat(retainedUUIDs).isEqualTo(uuidsToBeRetained);
        }

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                .documentsSentToParties(
                    wrapElements(
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        UUID.randomUUID(),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("DocSent.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build()
                    )
                ).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-482, case reference = 1643728359576136},"
                    + " expected case id 1636970654155393");
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class DfplRemoveC110a {
        private final long invalidCaseId = 1643728359576136L;
        private final UUID invalidDocId = UUID.randomUUID();

        private Stream<Arguments> provideMigrationTestData() {
            return Stream.of(
                Arguments.of("DFPL-694", 1643970994251861L, UUID.fromString("e32175d7-28ea-4041-8f1c-1087326ee331")),
                Arguments.of("DFPL-695", 1654079894022178L, UUID.fromString("d78acec6-f57c-45ed-a343-04f5261b738b")),
                Arguments.of("DFPL-697", 1643970994251861L, UUID.fromString("e32175d7-28ea-4041-8f1c-1087326ee331"))
            );
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldPerformMigrationWhenDocIdMatches(String migrationId, Long validCaseId, UUID validDocId) {

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .c110A(C110A.builder()
                    .submittedForm(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", validDocId))
                        .build())
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getC110A().getSubmittedForm()).isNull();
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid(String migrationId, Long validCaseId, UUID validDocId) {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                .c110A(C110A.builder()
                    .submittedForm(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", validDocId))
                        .build())
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = 1643728359576136}, expected case id %d",
                    migrationId, validCaseId));
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowAssersionErrorWhenDocumentIdIsInvalid(String migrationId, Long validCaseId, UUID validDocId) {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .c110A(C110A.builder()
                    .submittedForm(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", invalidDocId))
                        .build())
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %d}, expected c110a document id %s",
                    migrationId, validCaseId, validDocId));
        }


    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl373 {
        private final String migrationId = "DFPL-373";
        private final long validCaseId = 1634821154680053L;
        private final long invalidCaseId = 1643728359576136L;

        private final UUID validDocId = UUID.fromString("27454b43-eb9d-4510-a1c5-6f3df24f1f9f");
        private final UUID invalidDocId = UUID.randomUUID();

        @Test
        void shouldPerformMigrationWhenDocIdMatches() {

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .urgentHearingOrder(UrgentHearingOrder.builder()
                    .order(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", validDocId))
                        .build())
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getUrgentHearingOrder()).isNull();
        }

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                .urgentHearingOrder(UrgentHearingOrder.builder()
                    .order(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", validDocId))
                        .build())
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-373, case reference = 1643728359576136},"
                    + " expected case id 1634821154680053");
        }

        @Test
        void shouldThrowAssersionErrorWhenDocumentIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .urgentHearingOrder(UrgentHearingOrder.builder()
                    .order(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", invalidDocId))
                        .build())
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-373, case reference = 1634821154680053},"
                    + " expected urgent hearing order document id 27454b43-eb9d-4510-a1c5-6f3df24f1f9f");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl622 {
        private final String migrationId = "DFPL-622";
        private final long validCaseId = 1639491786898849L;
        private final long invalidCaseId = 1643728359576136L;

        private final UUID validElementId = UUID.fromString("a35d4775-f3ae-4eaa-9682-df88b00634ac");
        private final UUID invalidElementId = UUID.fromString("814581ff-3bec-4c13-b355-d0b9e11337d5");

        @Test
        void shouldPerformMigration() {

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .hearingDetails(List.of(element(HearingBooking.builder()
                    .type(CASE_MANAGEMENT)
                    .startDate(now().minusDays(3))
                    .endDate(now().minusDays(2))
                    .build())))
                .draftUploadedCMOs(List.of(element(validElementId,
                    HearingOrder.builder()
                        .type(HearingOrderType.DRAFT_CMO)
                        .order(DocumentReference.builder()
                                .filename("ToBeRemoved.doc")
                                .build())
                        .build())))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        }

        @Test
        void shouldPerformMigrationWhenDocIdMatches() {

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .hearingDetails(List.of(element(HearingBooking.builder()
                    .type(CASE_MANAGEMENT)
                    .startDate(now().minusDays(3))
                    .endDate(now().minusDays(2))
                    .build())))
                .draftUploadedCMOs(List.of(
                    element(validElementId,
                        HearingOrder.builder()
                            .type(HearingOrderType.DRAFT_CMO)
                            .order(DocumentReference.builder()
                                .filename("ToBeRemoved.doc")
                                .build())
                        .build()),
                    element(invalidElementId,
                        HearingOrder.builder()
                            .type(HearingOrderType.DRAFT_CMO)
                            .order(DocumentReference.builder()
                                .filename("DoNotRemove.doc")
                                .build())
                        .build())))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getDraftUploadedCMOs().size()).isEqualTo(1);
        }

        @Test
        void shouldThrowAssersionErrorWhenElementIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .hearingDetails(List.of(element(HearingBooking.builder()
                    .type(CASE_MANAGEMENT)
                    .startDate(now().minusDays(3))
                    .endDate(now().minusDays(2))
                    .build())))
                .draftUploadedCMOs(List.of(element(invalidElementId,
                    HearingOrder.builder()
                        .type(HearingOrderType.DRAFT_CMO)
                        .order(DocumentReference.builder()
                            .filename("DoNotRemove.doc")
                            .build())
                        .build())))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getDraftUploadedCMOs().size()).isEqualTo(1);
        }

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .hearingDetails(List.of(element(HearingBooking.builder()
                    .type(CASE_MANAGEMENT)
                    .startDate(now().minusDays(3))
                    .endDate(now().minusDays(2))
                    .build())))
                .draftUploadedCMOs(List.of(element(validElementId,
                    HearingOrder.builder()
                        .type(HearingOrderType.DRAFT_CMO)
                        .order(DocumentReference.builder()
                            .filename("Remove.doc")
                            .build())
                        .build())))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-622, case reference = 1643728359576136},"
                    + " expected case id 1639491786898849");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl666 {
        final String migrationId = "DFPL-666";
        final Long caseId = 1642779142991513L;
        final UUID hearingId = UUID.fromString("68cb7808-c12f-4936-8737-b55c424bdeb6");

        final Element<HearingBooking> hearingToBeRemoved = element(hearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now().minusDays(3))
            .endDate(now().minusDays(2))
            .build());
        final Element<HearingBooking> hearingBooking1 = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now().minusDays(3))
            .endDate(now().minusDays(2))
            .build());
        final Element<HearingBooking> hearingBooking2 = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now().minusDays(3))
            .endDate(now().minusDays(2))
            .build());

        @Test
        void shouldPerformMigrationWhenHearingIdMatches() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .hearingDetails(List.of(hearingToBeRemoved, hearingBooking1, hearingBooking2))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            List<Element<HearingBooking>> expectedHearingDetails = List.of(hearingBooking1, hearingBooking2);

            assertThat(responseData.getHearingDetails()).isEqualTo(expectedHearingDetails);
            assertThat(responseData.getSelectedHearingId()).isIn(hearingBooking1.getId(), hearingBooking2.getId());
        }

        @Test
        void shouldThrowAssersionErrorWhenHearingDetailsIsNull() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-666, case reference = 1642779142991513},"
                            + " hearing details not found");
        }

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(1111111111111111L)
                .hearingDetails(List.of(hearingToBeRemoved, hearingBooking1, hearingBooking2))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-666, case reference = 1111111111111111},"
                            + " expected case id 1642779142991513");
        }

        @Test
        void shouldThrowAssersionErrorWhenHearingBookingIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .hearingDetails(List.of(hearingBooking1, hearingBooking2))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-666, case reference = 1642779142991513},"
                            + " hearing booking 68cb7808-c12f-4936-8737-b55c424bdeb6 not found");
        }

        @Test
        void shouldThrowAssersionErrorWhenMoreThanOneHearingBookingWithSameIdFound() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .hearingDetails(List.of(hearingToBeRemoved, hearingToBeRemoved))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-666, case reference = 1642779142991513},"
                            + " more than one hearing booking 68cb7808-c12f-4936-8737-b55c424bdeb6 found");
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
