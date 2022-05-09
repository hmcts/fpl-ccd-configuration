package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    public static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();


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
    class Dfpl635 {
        private final String migrationId = "DFPL-635";
        private final long validCaseId = 1642758673379744L;
        private final long invalidCaseId = 1643728359576136L;

        private final UUID validDocId = UUID.fromString("9f0d570a-2cb8-48eb-90cb-3d4f26a2350a");
        private final UUID invalidDocId = UUID.randomUUID();

        @Test
        void shouldPerformMigrationWhenDocIdMatches() {

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

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
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
                .hasMessage("Migration {id = DFPL-635, case reference = 1643728359576136},"
                    + " expected case id 1642758673379744");
        }

        @Test
        void shouldThrowAssersionErrorWhenDocumentIdIsInvalid() {
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
                .hasMessage("Migration {id = DFPL-635, case reference = 1642758673379744},"
                    + " expected c110a document id 9f0d570a-2cb8-48eb-90cb-3d4f26a2350a");
        }


    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl572 {
        private final String migrationId = "DFPL-572";
        private final long validCaseId = 1646391317671957L;
        private final long invalidCaseId = 1643728359576136L;

        private final UUID validDocId = UUID.fromString("0d30f8e4-cf44-47f6-ab1b-7fc11fdc34a8");
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
                .hasMessage("Migration {id = DFPL-572, case reference = 1643728359576136},"
                    + " expected case id 1646391317671957");
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
                .hasMessage("Migration {id = DFPL-572, case reference = 1646391317671957},"
                    + " expected urgent hearing order document id 0d30f8e4-cf44-47f6-ab1b-7fc11fdc34a8");
        }


    }


    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
