package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MigrateCaseServiceTest {

    private static final String MIGRATION_ID = "test-migration";

    @Mock
    private CaseNoteService caseNoteService;
    @Mock
    private DocumentListService documentListService;

    @Mock
    private CourtService courtService;

    @InjectMocks
    private MigrateCaseService underTest;

    @Test
    void shouldDoHearingOptionCheck() {
        assertDoesNotThrow(() -> underTest.doHearingOptionCheck(1L, "EDIT_HEARING", "EDIT_HEARING", MIGRATION_ID));
    }

    @Test
    void shouldThrowExceptionIfHearingOptionCheckFails() {
        assertThrows(AssertionError.class, () -> underTest.doHearingOptionCheck(1L, "EDIT_PAST_HEARING",
            "EDIT_HEARING", MIGRATION_ID));
    }

    @Test
    void shouldDoCaseIdCheck() {
        assertDoesNotThrow(() -> underTest.doCaseIdCheck(1L, 1L, MIGRATION_ID));
    }

    @Test
    void shouldThrowExceptionIfCaseIdCheckFails() {
        assertThrows(AssertionError.class, () -> underTest.doCaseIdCheck(1L, 2L, MIGRATION_ID));
    }

    @Test
    void shouldThrowExceptionIfCaseIdListCheckFails() {
        assertThrows(AssertionError.class, () -> underTest.doCaseIdCheckList(1L, List.of(2L, 3L), MIGRATION_ID));
    }

    @Nested
    class RemoveHearingOrderBundleDraft {

        private final UUID bundleIdToRemove = UUID.randomUUID();
        private final UUID bundleIdToKeep = UUID.randomUUID();
        private final UUID orderIdToRemove = UUID.randomUUID();
        private final UUID orderIdToKeep = UUID.randomUUID();

        private final Element<HearingOrder> orderToRemove = element(orderIdToRemove, HearingOrder.builder()
            .type(HearingOrderType.C21)
            .title("Draft order")
            .build());

        private final Element<HearingOrder> orderToKeep = element(orderIdToKeep, HearingOrder.builder()
            .type(HearingOrderType.C21)
            .title("Order to keep")
            .build());

        @Test
        void shouldClearBundlesWithNoOrdersPostMigration() {
            List<Element<HearingOrder>> orders = new ArrayList<>();
            orders.add(orderToRemove);
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(
                    element(bundleIdToRemove, HearingOrdersBundle.builder()
                        .orders(orders)
                        .build())
                ))
                .build();

            Map<String, Object> fields = underTest.removeHearingOrderBundleDraft(caseData, MIGRATION_ID,
                bundleIdToRemove, orderIdToRemove);

            assertThat(fields.get("hearingOrdersBundlesDrafts")).isEqualTo(List.of());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherOrdersIntact() {
            List<Element<HearingOrder>> orders = new ArrayList<>();
            orders.add(orderToKeep);
            orders.add(orderToRemove);

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(
                    element(bundleIdToRemove, HearingOrdersBundle.builder()
                        .orders(orders)
                        .build())
                ))
                .build();

            Map<String, Object> fields = underTest.removeHearingOrderBundleDraft(caseData, MIGRATION_ID,
                bundleIdToRemove, orderIdToRemove);

            List<Element<HearingOrdersBundle>> resultBundles = (List<Element<HearingOrdersBundle>>)
                fields.get("hearingOrdersBundlesDrafts");

            assertThat(resultBundles).hasSize(1);
            assertThat(resultBundles.get(0).getValue().getOrders()).containsExactly(orderToKeep);
        }

        @Test
        void shouldThrowExceptionIfNoBundleFound() {
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(
                    element(bundleIdToKeep, HearingOrdersBundle.builder()
                        .build())
                ))
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removeHearingOrderBundleDraft(caseData, MIGRATION_ID, bundleIdToRemove, orderIdToRemove));
        }

    }

    @Nested
    class RemoveDocumentsSentToParties {

        private final UUID partyId = UUID.randomUUID();
        private final UUID docIdToRemove = UUID.randomUUID();
        private final UUID docIdToKeep = UUID.randomUUID();

        private final Element<SentDocument> docToRemove = element(docIdToRemove, SentDocument.builder()
            .partyName("REMOVE")
            .build());

        private final Element<SentDocument> docToKeep = element(docIdToKeep, SentDocument.builder()
            .partyName("KEEP")
            .build());

        @Test
        void shouldClearDocumentsSentToPartiesWithNoDocumentsPostMigration() {
            List<Element<SentDocument>> orders = new ArrayList<>();
            orders.add(docToRemove);
            CaseData caseData = CaseData.builder()
                .documentsSentToParties(List.of(
                    element(partyId, SentDocuments.builder()
                        .documentsSentToParty(List.of(docToRemove))
                        .build())
                ))
                .build();

            Map<String, Object> fields = underTest.removeDocumentsSentToParties(caseData, MIGRATION_ID,
                partyId, List.of(docIdToRemove));

            assertThat(fields.get("documentsSentToParties")).isEqualTo(
                List.of(element(partyId, SentDocuments.builder().documentsSentToParty(List.of()).build())));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntact() {
            List<Element<SentDocument>> documents = new ArrayList<>();
            documents.add(docToKeep);
            documents.add(docToRemove);

            CaseData caseData = CaseData.builder()
                .documentsSentToParties(List.of(
                    element(partyId, SentDocuments.builder()
                        .documentsSentToParty(documents)
                        .build())
                ))
                .build();

            Map<String, Object> fields = underTest.removeDocumentsSentToParties(caseData, MIGRATION_ID,
                partyId, List.of(docIdToRemove));

            List<Element<SentDocuments>> resultDocumentsSentToParties = (List<Element<SentDocuments>>)
                fields.get("documentsSentToParties");

            assertThat(resultDocumentsSentToParties).hasSize(1);
            assertThat(resultDocumentsSentToParties.get(0).getValue().getDocumentsSentToParty())
                .containsExactly(docToKeep);
        }

        @Test
        void shouldThrowExceptionIfNoDocumentFound() {
            CaseData caseData = CaseData.builder()
                .documentsSentToParties(List.of(element(partyId,
                    SentDocuments.builder()
                        .documentsSentToParty(List.of(element(UUID.randomUUID(),
                            SentDocument.builder().build()
                        )))
                        .build()
                )))
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removeDocumentsSentToParties(caseData, MIGRATION_ID, partyId,
                    List.of(docIdToRemove)));
        }

        @Test
        void shouldThrowExceptionIfNoPartyFound() {
            CaseData caseData = CaseData.builder()
                .documentsSentToParties(List.of(element(UUID.randomUUID(),
                    SentDocuments.builder()
                        .documentsSentToParty(List.of(element(
                            SentDocument.builder().build()
                        )))
                        .build()
                )))
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removeDocumentsSentToParties(caseData, MIGRATION_ID, partyId,
                    List.of(docIdToRemove)));
        }
    }

    @Nested
    class RemovePositionStatementChild {

        private final UUID docIdToRemove = UUID.randomUUID();
        private final UUID docIdToKeep = UUID.randomUUID();

        private final Element<PositionStatementChild> docToRemove = element(docIdToRemove,
            PositionStatementChild.builder()
                .build());

        private final Element<PositionStatementChild> docToKeep = element(docIdToKeep,
            PositionStatementChild.builder()
                .build());

        @Test
        void shouldClearPositionStatementChildWithNoDocumentsPostMigration() {
            List<Element<PositionStatementChild>> positionStatementChilds = new ArrayList<>();
            positionStatementChilds.add(docToRemove);
            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(docToRemove))
                    .build())
                .build();

            Map<String, Object> fields = underTest.removePositionStatementChild(caseData, MIGRATION_ID,
                docIdToRemove);

            assertThat(fields.get("positionStatementChildListV2")).isEqualTo(List.of());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntact() {
            List<Element<PositionStatementChild>> positionStatements = new ArrayList<>();
            positionStatements.add(docToKeep);
            positionStatements.add(docToRemove);

            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(positionStatements)
                    .build())
                .build();

            Map<String, Object> fields = underTest.removePositionStatementChild(caseData, MIGRATION_ID,
                docIdToRemove);

            List<Element<PositionStatementChild>> resultsPositionStatements =
                (List<Element<PositionStatementChild>>) fields.get("positionStatementChildListV2");

            assertThat(resultsPositionStatements).hasSize(1);
            assertThat(resultsPositionStatements).containsExactly(docToKeep);
        }

        @Test
        void shouldThrowExceptionIfNoDocumentFound() {
            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(element(PositionStatementChild.builder().build())))
                    .build())
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removePositionStatementChild(caseData, MIGRATION_ID,
                    docIdToRemove));
        }
    }

    @Nested
    class RemovePositionStatementRespondent {

        private final UUID docIdToRemove = UUID.randomUUID();
        private final UUID docIdToKeep = UUID.randomUUID();

        private final Element<PositionStatementRespondent> docToRemove = element(docIdToRemove,
            PositionStatementRespondent.builder()
                .build());

        private final Element<PositionStatementRespondent> docToKeep = element(docIdToKeep,
            PositionStatementRespondent.builder()
                .build());

        @Test
        void shouldClearPositionStatementRespondentWithNoDocumentsPostMigration() {
            List<Element<PositionStatementRespondent>> positionStatementRespondents = new ArrayList<>();
            positionStatementRespondents.add(docToRemove);
            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(docToRemove))
                    .build())
                .build();

            Map<String, Object> fields = underTest.removePositionStatementRespondent(caseData, MIGRATION_ID,
                docIdToRemove);

            assertThat(fields.get("positionStatementRespondentListV2")).isEqualTo(List.of());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntact() {
            List<Element<PositionStatementRespondent>> positionStatements = new ArrayList<>();
            positionStatements.add(docToKeep);
            positionStatements.add(docToRemove);

            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(positionStatements)
                    .build())
                .build();

            Map<String, Object> fields = underTest.removePositionStatementRespondent(caseData, MIGRATION_ID,
                docIdToRemove);

            List<Element<PositionStatementRespondent>> resultsPositionStatements =
                (List<Element<PositionStatementRespondent>>) fields.get("positionStatementRespondentListV2");

            assertThat(resultsPositionStatements).hasSize(1);
            assertThat(resultsPositionStatements).containsExactly(docToKeep);
        }

        @Test
        void shouldThrowExceptionIfNoDocumentFound() {
            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(element(PositionStatementRespondent.builder().build())))
                    .build())
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removePositionStatementRespondent(caseData, MIGRATION_ID, docIdToRemove));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveCaseNote {

        private final UUID noteIdToRemove = UUID.randomUUID();

        @Test
        void shouldThrowExceptionWhenCaseNoteNotPresent() {
            UUID otherNoteId = UUID.randomUUID();
            UUID otherNoteId2 = UUID.randomUUID();
            CaseData caseData = CaseData.builder()
                .caseNotes(List.of(
                    element(otherNoteId, CaseNote.builder().note("Test note 1").build()),
                    element(otherNoteId2, CaseNote.builder().note("Test note 2").build())
                ))
                .build();

            assertThrows(AssertionError.class, () -> underTest.removeCaseNote(caseData, MIGRATION_ID, noteIdToRemove));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveHearingBooking {

        private final UUID hearingBookingToRemove = UUID.randomUUID();
        private final UUID otherHearingBookingId = UUID.randomUUID();

        @Test
        void shouldThrowAssertionErrorIfHearingBookingNotPresent() {
            List<Element<HearingBooking>> bookings = new ArrayList<>();
            bookings.add(element(otherHearingBookingId, HearingBooking.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingDetails(bookings)
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removeHearingBooking(caseData, MIGRATION_ID, hearingBookingToRemove));
        }

        @Test
        void shouldRemoveHearingBooking() {
            List<Element<HearingBooking>> bookings = new ArrayList<>();
            bookings.add(element(otherHearingBookingId, HearingBooking.builder().build()));
            bookings.add(element(hearingBookingToRemove, HearingBooking.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingDetails(bookings)
                .build();

            Map<String, Object> updatedFields = underTest.removeHearingBooking(caseData, MIGRATION_ID,
                hearingBookingToRemove);

            assertThat(updatedFields).extracting("hearingDetails").asList().hasSize(1);
            assertThat(updatedFields).extracting("hearingDetails").asList()
                .doesNotContainAnyElementsOf(List.of(hearingBookingToRemove));

        }

        @Test
        void shouldRemoveHearingBookingWithSingleHearing() {
            List<Element<HearingBooking>> bookings = new ArrayList<>();
            bookings.add(element(hearingBookingToRemove, HearingBooking.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingDetails(bookings)
                .build();

            Map<String, Object> updatedFields = underTest.removeHearingBooking(caseData, MIGRATION_ID,
                hearingBookingToRemove);

            assertThat(updatedFields).extracting("hearingDetails").asList().hasSize(0);
            assertThat(updatedFields).extracting("hearingDetails").asList()
                .doesNotContainAnyElementsOf(List.of(hearingBookingToRemove));
            assertThat(updatedFields).extracting("selectedHearingId").isNull();

        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveGatekeepingOrderUrgentHearingOrder {

        private final long caseId = 1L;

        @Test
        void shouldThrowAssertionIfOrderNotFound() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.verifyGatekeepingOrderUrgentHearingOrderExistWithGivenFileName(caseData, MIGRATION_ID,
                    "test.pdf"));
        }

        @Test
        void shouldThrowAssertionIfOrderFileNameNotMatch() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .urgentHearingOrder(UrgentHearingOrder.builder()
                    .order(DocumentReference.builder().filename("test").build())
                    .build())
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.verifyGatekeepingOrderUrgentHearingOrderExistWithGivenFileName(caseData, MIGRATION_ID,
                    "test.pdf"));
        }

        @Test
        void shouldNotThrowIfUrgentHearingOrderFound() {
            CaseData caseData = CaseData.builder()
                .urgentHearingOrder(UrgentHearingOrder.builder()
                    .order(DocumentReference.builder().filename("test.pdf").build())
                    .build())
                .build();

            assertDoesNotThrow(() ->
                underTest.verifyGatekeepingOrderUrgentHearingOrderExistWithGivenFileName(caseData, MIGRATION_ID,
                    "test.pdf"));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveApplicationDocument {

        private final UUID applicationDocumentIdToRemove = UUID.randomUUID();

        @Test
        void shouldThrowExceptionWhenApplicationDocumentNotPresent() {
            UUID otherApplicationDocumentId1 = UUID.randomUUID();
            UUID otherApplicationDocumentId2 = UUID.randomUUID();
            CaseData caseData = CaseData.builder()
                .applicationDocuments(List.of(
                    element(otherApplicationDocumentId1, ApplicationDocument.builder().documentName("1").build()),
                    element(otherApplicationDocumentId2, ApplicationDocument.builder().documentName("2").build())
                ))
                .build();

            assertThrows(AssertionError.class, () -> underTest.removeApplicationDocument(caseData, MIGRATION_ID,
                applicationDocumentIdToRemove));
        }

        @Test
        void shouldRemoveApplicationDocument() {
            UUID otherApplicationDocumentId1 = UUID.randomUUID();
            List<Element<ApplicationDocument>> applicationDocuments = new ArrayList<>();
            applicationDocuments.add(element(otherApplicationDocumentId1, ApplicationDocument.builder().build()));
            applicationDocuments.add(element(applicationDocumentIdToRemove, ApplicationDocument.builder().build()));

            CaseData caseData = CaseData.builder()
                .applicationDocuments(applicationDocuments)
                .build();

            Map<String, Object> updatedFields = underTest.removeApplicationDocument(caseData, MIGRATION_ID,
                applicationDocumentIdToRemove);

            assertThat(updatedFields).extracting("applicationDocuments").asList().hasSize(1);
            assertThat(updatedFields).extracting("applicationDocuments").asList()
                .doesNotContainAnyElementsOf(List.of(applicationDocumentIdToRemove));
        }

        @Test
        void shouldRemoveSingleApplicationDocument() {
            List<Element<ApplicationDocument>> applicationDocuments = new ArrayList<>();
            applicationDocuments.add(element(applicationDocumentIdToRemove, ApplicationDocument.builder().build()));

            CaseData caseData = CaseData.builder()
                .applicationDocuments(applicationDocuments)
                .build();

            Map<String, Object> updatedFields = underTest.removeApplicationDocument(caseData, MIGRATION_ID,
                applicationDocumentIdToRemove);

            assertThat(updatedFields).extracting("applicationDocuments").asList().hasSize(0);
        }
    }

    @Nested
    class UpdateIncorrectCourtCodes {

        @Test
        void shouldUpdateIncorrectCourtCodeForBHC() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("544")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("0F6AZIR").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("554")
                .name("Family Court sitting at Brighton")
                .build());
        }

        @Test
        void shouldUpdateIncorrectCourtCodeForWSX() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("544")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("HLT7S0M").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("554")
                .name("Family Court Sitting at Brighton County Court")
                .build());
        }

        @Test
        void shouldUpdateIncorrectCourtCodeForBNT() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("117")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("SPUL3VV").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("332")
                .name("Family Court Sitting at West London")
                .build());
        }

        @Test
        void shouldUpdateIncorrectCourtCodeForHRW() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("117")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("L3HSA4L").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("332")
                .name("Family Court Sitting at West London")
                .build());
        }

        @Test
        void shouldUpdateIncorrectCourtCodeForHLW() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("117")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("6I4Z3OO").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("332")
                .name("Family Court Sitting at West London")
                .build());
        }

        @Test
        void shouldUpdateIncorrectCourtCodeForRCT() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("164")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("68MNZN8").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("159")
                .name("Family Court sitting at Cardiff")
                .build());
        }

        @Test
        void shouldUpdateIncorrectCourtCodeForBAD() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("3403")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("3FG3URQ").build())
                        .build())
                .build();

            Map<String, Object> fields = underTest.updateIncorrectCourtCodes(caseData);

            assertThat(fields.get("court")).isEqualTo(Court.builder()
                .code("121")
                .name("Family Court Sitting at East London Family Court")
                .build());
        }

        @Test
        void shouldThrowExceptionWhenCourtCodeAndOrganisationNotMatch() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("544")
                    .build())
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("0F6AZIX").build())
                        .build())
                .build();

            assertThatThrownBy(() -> underTest.updateIncorrectCourtCodes(caseData))
                .isInstanceOf(AssertionError.class)
                .hasMessage("It does not match any migration conditions. (courtCode = 544, "
                    + "localAuthorityPolicy.organisation.organisationID = 0F6AZIX)");
        }

        @Test
        void shouldThrowExceptionWithoutLocalAuthorityPolicy() {
            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .name("Something")
                    .code("544")
                    .build())
                .build();

            assertThatThrownBy(() -> underTest.updateIncorrectCourtCodes(caseData))
                .isInstanceOf(AssertionError.class)
                .hasMessage("The case does not have court or local authority policy's organisation.");
        }

        @Test
        void shouldThrowExceptionWithoutCourt() {
            CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(
                    OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("0F6AZIX").build())
                        .build())
                .build();

            assertThatThrownBy(() -> underTest.updateIncorrectCourtCodes(caseData))
                .isInstanceOf(AssertionError.class)
                .hasMessage("The case does not have court or local authority policy's organisation.");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveCaseSummary {

        private final UUID hearingIdToRemove = UUID.randomUUID();

        @Test
        void shouldThrowExceptionWhenCaseSummaryNotPresent() {
            UUID otherHearingId1 = UUID.randomUUID();
            UUID otherHearingId2 = UUID.randomUUID();
            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(List.of(
                        element(otherHearingId1, CaseSummary.builder().build()),
                        element(otherHearingId2, CaseSummary.builder().build())
                    ))
                    .build())
                .build();

            assertThrows(AssertionError.class, () -> underTest.removeCaseSummaryByHearingId(caseData, MIGRATION_ID,
                hearingIdToRemove));
        }

        @Test
        void shouldRemoveCaseSummary() {
            UUID otherHearingId1 = UUID.randomUUID();
            List<Element<CaseSummary>> caseSummaries = new ArrayList<>();
            caseSummaries.add(element(otherHearingId1, CaseSummary.builder().build()));
            caseSummaries.add(element(hearingIdToRemove, CaseSummary.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(caseSummaries)
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeCaseSummaryByHearingId(caseData, MIGRATION_ID,
                hearingIdToRemove);

            assertThat(updatedFields).extracting("caseSummaryList").asList().hasSize(1);
            assertThat(updatedFields).extracting("caseSummaryList").asList()
                .doesNotContainAnyElementsOf(List.of(hearingIdToRemove));
        }

        @Test
        void shouldRemoveSingleCaseSummary() {
            List<Element<CaseSummary>> caseSummaries = new ArrayList<>();
            caseSummaries.add(element(hearingIdToRemove, CaseSummary.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(caseSummaries)
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeCaseSummaryByHearingId(caseData, MIGRATION_ID,
                hearingIdToRemove);

            assertThat(updatedFields).extracting("caseSummaryList").asList().hasSize(0);
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RevertChildExtensionDate {
        LocalDate completeDate = LocalDate.of(2023, 1, 1);
        LocalDate revertedDate = LocalDate.of(2022, 1, 1);

        CaseExtensionReasonList extensionReason = CaseExtensionReasonList.DELAY_IN_CASE_OR_IMPACT_ON_CHILD;
        CaseExtensionReasonList revertedReason = CaseExtensionReasonList.NO_EXTENSION;

        Element<Child> targetChild1 = element(UUID.randomUUID(), Child.builder()
            .party(ChildParty.builder()
                .completionDate(completeDate)
                .extensionReason(extensionReason)
                .build())
            .build());

        Element<Child> otherChild = element(UUID.randomUUID(), Child.builder()
            .party(ChildParty.builder()
                .completionDate(completeDate)
                .extensionReason(extensionReason)
                .build())
            .build());

        @Test
        void shouldRevertChildCompletionDate() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .children1(List.of(targetChild1, otherChild))
                .build();

            Map<String, Object> resultMap = underTest.revertChildExtensionDate(caseData, MIGRATION_ID,
                targetChild1.getId().toString(), revertedDate, revertedReason);

            assertThat(resultMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(targetChild1.getId(), Child.builder()
                        .party(ChildParty.builder()
                            .completionDate(revertedDate)
                            .extensionReason(revertedReason)
                            .build())
                        .build()),
                    otherChild
                )));
        }

        @Test
        void shouldRevertChildNullExtensionReason() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .children1(List.of(targetChild1, otherChild))
                .build();

            Map<String, Object> resultMap = underTest.revertChildExtensionDate(caseData, MIGRATION_ID,
                targetChild1.getId().toString(), revertedDate, null);

            assertThat(resultMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(targetChild1.getId(), Child.builder()
                        .party(ChildParty.builder()
                            .completionDate(revertedDate)
                            .extensionReason(null)
                            .build())
                        .build()),
                    otherChild
                )));
        }

        @Test
        void shouldThrowExceptionIfChildNotFound() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .children1(List.of(otherChild))
                .build();

            assertThatThrownBy(() -> underTest.revertChildExtensionDate(caseData, MIGRATION_ID,
                    targetChild1.getId().toString(), revertedDate, revertedReason))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s}, case reference = %s} child %s not found",
                    MIGRATION_ID, 1L, targetChild1.getId()));
        }

        @Test
        void shouldThrowExceptionIfNoChildren() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .build();

            assertThatThrownBy(() -> underTest.revertChildExtensionDate(caseData, MIGRATION_ID,
                targetChild1.getId().toString(), revertedDate, revertedReason))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s} doesn't have children",
                    MIGRATION_ID, 1L));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RefreshDocumentView {
        @Test
        void shouldInvokeDocumentListServiceForRefreshingDocumentViews() {
            CaseData data  = CaseData.builder().build();
            underTest.refreshDocumentViews(data);
            verify(documentListService).getDocumentView(data);
        }
    }

    @Test
    void shouldNotThrowWhenNoConfidentialDocumentInDocumentViewNC() {
        assertDoesNotThrow(() -> underTest.doDocumentViewNCCheck(1L, MIGRATION_ID,
            CaseDetails.builder().data(Map.of("documentViewNC", "\"<p><div class='width-50'>\\n"
                + "\\n<details class=\\\"govuk-details\\\"><summary class=\\\"govuk-details__summary\\\">"
                + "Applicant's statements and application documents</summary>"
                + "<div class=\\\"govuk-details__text\\\"><details class=\\\"govuk-details\\\">"
                + "<summary class=\\\"govuk-details__summary\\\">Genogram</summary>"
                + "<div class=\\\"govuk-details__text\\\"><details class=\\\"govuk-details\\\">"
                + "<dt class=\\\"govuk-summary-list__key\\\">"
                + "<img height='25px' src='https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/"
                + "master/resources/confidential.png' title='Confidential'/></dt>"
                + "<summary class=\\\"govuk-details__summary\\\">complete guide to fpla-ccd-configuration.pdf</summary>"
                + "<div class=\\\"govuk-details__text\\\"><dl class=\\\"govuk-summary-list\\\">"
                + "<div class=\\\"govuk-summary-list__row\\\">")).build()));
    }

    @Test
    void shouldThrowWhenNoConfidentialDocumentInDocumentViewNC() {
        assertThatThrownBy(() -> underTest.doDocumentViewNCCheck(1L, MIGRATION_ID,
            CaseDetails.builder().data(Map.of("documentViewNC", "\"<p><div class='width-50'>\\n"
                + "\\n<details class=\\\"govuk-details\\\"><summary class=\\\"govuk-details__summary\\\">"
                + "Applicant's statements and application documents</summary>"
                + "<div class=\\\"govuk-details__text\\\"><details class=\\\"govuk-details\\\">"
                + "<summary class=\\\"govuk-details__summary\\\">Genogram</summary>"
                + "<div class=\\\"govuk-details__text\\\"><details class=\\\"govuk-details\\\">"
                + "<summary class=\\\"govuk-details__summary\\\">complete guide to fpla-ccd-configuration.pdf</summary>"
                + "<div class=\\\"govuk-details__text\\\"><dl class=\\\"govuk-summary-list\\\">"
                + "<div class=\\\"govuk-summary-list__row\\\">")).build()))
            .isInstanceOf(AssertionError.class)
            .hasMessage(format(
                "Migration {id = %s, case reference = %s}, expected documentViewNC contains confidential doc.",
                MIGRATION_ID, 1L));
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemovePlacementApplication {
        private final UUID placementToRemove = UUID.randomUUID();
        private final UUID placementToRemain = UUID.randomUUID();

        @Test
        void shouldOnlyRemoveSelectPlacement() {
            List<Element<Placement>> placements = List.of(
                element(placementToRemove, Placement.builder()
                    .build()),
                element(placementToRemain, Placement.builder()
                    .build())
            );

            List<Element<Placement>> placementsRemaining = List.of(
                element(placementToRemain, Placement.builder()
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(placements)
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeSpecificPlacements(caseData, placementToRemove);

            assertThat(updatedFields).extracting("placements").asList().hasSize(1)
                    .isEqualTo(placementsRemaining);
            assertThat(updatedFields).extracting("placementsNonConfidential").asList()
                .hasSize(1).isEqualTo(placementsRemaining);
            assertThat(updatedFields).extracting("placementsNonConfidentialNotices").asList()
                .hasSize(1).isEqualTo(placementsRemaining);
        }

        @Test
        void shouldRemovePlacementWhenSelectedPlacementIsTheLastOne() {
            List<Element<Placement>> placements = List.of(
                element(placementToRemove, Placement.builder()
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(placements)
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeSpecificPlacements(caseData, placementToRemove);

            assertThat(updatedFields).extracting("placements").isNull();
            assertThat(updatedFields).extracting("placementsNonConfidential").isNull();
            assertThat(updatedFields).extracting("placementsNonConfidentialNotices").isNull();
        }
    }

    @Nested
    class RemoveDraftUploadedCMO {

        private final UUID orderIdToRemove = UUID.randomUUID();
        private final UUID orderIdToKeep = UUID.randomUUID();

        private final Element<HearingOrder> orderToRemove = element(orderIdToRemove, HearingOrder.builder().build());

        private final Element<HearingOrder> orderToKeep = element(orderIdToKeep, HearingOrder.builder().build());

        @Test
        void shouldClearDraftUploadedCMOsWithNoOrderPostMigration() {
            List<Element<HearingOrder>> hearingOrders = new ArrayList<>();
            hearingOrders.add(orderToRemove);
            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(orderToRemove))
                .build();

            Map<String, Object> fields = underTest.removeDraftUploadedCMOs(caseData, MIGRATION_ID,
                orderIdToRemove);

            assertThat(fields.get("draftUploadedCMOs")).isEqualTo(List.of());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherOrdersIntact() {
            List<Element<HearingOrder>> draftUploadedCMOs = new ArrayList<>();
            draftUploadedCMOs.add(orderToKeep);
            draftUploadedCMOs.add(orderToRemove);

            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(draftUploadedCMOs)
                .build();

            Map<String, Object> fields = underTest.removeDraftUploadedCMOs(caseData, MIGRATION_ID,
                orderIdToRemove);

            List<Element<HearingOrder>> result =
                (List<Element<HearingOrder>>) fields.get("draftUploadedCMOs");

            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(orderToKeep);
        }

        @Test
        void shouldThrowExceptionIfNoOrderFound() {
            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of())
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removeDraftUploadedCMOs(caseData, MIGRATION_ID,
                    orderIdToRemove));
        }
    }

    @Nested
    class RemoveHearingOrdersBundlesDraft {

        private final UUID orderIdToRemove = UUID.randomUUID();
        private final UUID orderIdToKeep = UUID.randomUUID();

        private final Element<HearingOrdersBundle> bundleToRemove = element(orderIdToRemove,
            HearingOrdersBundle.builder().build());

        private final Element<HearingOrdersBundle> bundleToKeep = element(orderIdToKeep,
            HearingOrdersBundle.builder().build());

        @Test
        void shouldClearHearingOrderBundleWithNoOrderPostMigration() {
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(bundleToRemove))
                .build();

            Map<String, Object> fields = underTest.removeHearingOrdersBundlesDrafts(caseData, MIGRATION_ID,
                orderIdToRemove);

            assertThat(fields.get("hearingOrdersBundlesDrafts")).isEqualTo(List.of());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherOrdersIntact() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = new ArrayList<>();
            hearingOrdersBundlesDrafts.add(bundleToKeep);
            hearingOrdersBundlesDrafts.add(bundleToRemove);

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
                .build();

            Map<String, Object> fields = underTest.removeHearingOrdersBundlesDrafts(caseData, MIGRATION_ID,
                orderIdToRemove);

            List<Element<HearingOrdersBundle>> result =
                (List<Element<HearingOrdersBundle>>) fields.get("hearingOrdersBundlesDrafts");

            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(bundleToKeep);
        }

        @Test
        void shouldThrowExceptionIfNoOrderFound() {
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of())
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.removeHearingOrdersBundlesDrafts(caseData, MIGRATION_ID,
                    orderIdToRemove));
        }
    }

    @Nested
    class RenameApplicationDocuments {

        @Test
        void shouldRemoveAngularBracketsFromDocumentNames() {
            UUID docId = UUID.randomUUID();
            Element<ApplicationDocument> appDoc = element(docId, ApplicationDocument.builder()
                .documentName("PA>S")
                .build());

            Element<ApplicationDocument> expectedDoc = element(docId, ApplicationDocument.builder()
                .documentName("PAS")
                .build());

            CaseData caseData = CaseData.builder()
                .applicationDocuments(List.of(appDoc))
                .build();

            Map<String, Object> updates = underTest.renameApplicationDocuments(caseData);

            assertThat(updates).extracting("applicationDocuments").asList().containsExactly(expectedDoc);
        }

        @Test
        void shouldDoNothingIfNoAngularBrackets() {
            Element<ApplicationDocument> appDoc = element(ApplicationDocument.builder()
                .documentName("PAS")
                .build());

            CaseData caseData = CaseData.builder()
                .applicationDocuments(List.of(appDoc))
                .build();

            Map<String, Object> updates = underTest.renameApplicationDocuments(caseData);

            assertThat(updates).extracting("applicationDocuments").asList().containsExactly(appDoc);
        }

        @Test
        void shouldRenameMultipleDocsIfAngularBrackets() {
            UUID docId1 = UUID.randomUUID();
            UUID docId2 = UUID.randomUUID();
            Element<ApplicationDocument> appDoc1 = element(docId1, ApplicationDocument.builder()
                .documentName("PA>S")
                .build());
            Element<ApplicationDocument> appDoc2 = element(docId2, ApplicationDocument.builder()
                .documentName("PA<S")
                .build());

            Element<ApplicationDocument> expectedDoc1 = element(docId1, ApplicationDocument.builder()
                .documentName("PAS")
                .build());

            Element<ApplicationDocument> expectedDoc2 = element(docId2, ApplicationDocument.builder()
                .documentName("PAS")
                .build());

            CaseData caseData = CaseData.builder()
                .applicationDocuments(List.of(appDoc1, appDoc2))
                .build();

            Map<String, Object> updates = underTest.renameApplicationDocuments(caseData);

            assertThat(updates).extracting("applicationDocuments").asList().containsExactly(expectedDoc1, expectedDoc2);
        }
    }

    static Stream<Arguments> createPossibleOrderType() {
        String invalidOrderType = "EDUCATION_SUPERVISION__ORDER";
        String validOrderType = "EDUCATION_SUPERVISION_ORDER";
        return Stream.of(
            Arguments.of(List.of(invalidOrderType), List.of(validOrderType)),
            Arguments.of(List.of(invalidOrderType, "DEF"), List.of(validOrderType, "DEF")),
            Arguments.of(List.of("ABC", invalidOrderType), List.of("ABC", validOrderType)),
            Arguments.of(List.of("ABC", invalidOrderType, "DEF"), List.of("ABC", validOrderType, "DEF"))
        );
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class FixOrderTypeTypo {

        @ParameterizedTest
        @SuppressWarnings("unchecked")
        @MethodSource("uk.gov.hmcts.reform.fpl.service.MigrateCaseServiceTest#createPossibleOrderType")
        void shouldChangeInvalidOrderType(List<String> orderType, List<String> expectedOrderType) {
            CaseDetails caseDetails = CaseDetails.builder().data(
                Map.of("orders", Map.of("orderType", orderType))
            ).build();

            assertThat(underTest.fixOrderTypeTypo(MIGRATION_ID, caseDetails)).containsEntry("orders",
                Map.of("orderType", expectedOrderType));
        }

        @Test
        void shouldThrowAssertionErrorIfOrdersMissing() {
            CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();

            assertThatThrownBy(() -> underTest.fixOrderTypeTypo(MIGRATION_ID, caseDetails))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = test-migration}, case does not have [orders]");
        }

        @Test
        void shouldThrowAssertionErrorIfOrderTypeMissing() {
            Map<String, Object> orders = new HashMap<>();
            orders.put("orders", Map.of());
            CaseDetails caseDetails = CaseDetails.builder().data(orders).build();

            assertThatThrownBy(() -> underTest.fixOrderTypeTypo(MIGRATION_ID, caseDetails))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = test-migration}, case does not have [orders.orderType] "
                    + "or missing target invalid order type [EDUCATION_SUPERVISION__ORDER]");
        }

        @Test
        void shouldThrowAssertionErrorIfCaseDoesNotContainInvalidOrderType() {
            Map<String, Object> orders = new HashMap<>();
            orders.put("orders", Map.of("orderType", List.of("ABC")));
            CaseDetails caseDetails = CaseDetails.builder().data(orders).build();

            assertThatThrownBy(() -> underTest.fixOrderTypeTypo(MIGRATION_ID, caseDetails))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = test-migration}, case does not have [orders.orderType] "
                    + "or missing target invalid order type [EDUCATION_SUPERVISION__ORDER]");
        }
    }
    
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveJudicialMessage {
        final Element<JudicialMessage> message1 = element(JudicialMessage.builder().build());
        final Element<JudicialMessage> message2 = element(JudicialMessage.builder().build());
        final Element<JudicialMessage> mesageToBeRemoved = element(JudicialMessage.builder().build());

        @Test
        void shouldRemoveJudicialMessage() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .judicialMessages(List.of(message1, message2, mesageToBeRemoved))
                .build();

            Map<String, Object> updates =
                underTest.removeJudicialMessage(caseData, MIGRATION_ID, mesageToBeRemoved.getId().toString());
            assertThat(updates).extracting("judicialMessages").asList().containsExactly(message1, message2);
        }

        @Test
        void shouldRemoveJudicialMessageIfOnlyOneMessageExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .judicialMessages(List.of(mesageToBeRemoved))
                .build();

            Map<String, Object> updates =
                underTest.removeJudicialMessage(caseData, MIGRATION_ID, mesageToBeRemoved.getId().toString());
            assertThat(updates).extracting("judicialMessages").asList().isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenNull() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() ->
                underTest.removeJudicialMessage(caseData, MIGRATION_ID, mesageToBeRemoved.getId().toString()))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        void shouldThrowExceptionWhenMessageNotFound() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() ->
                underTest.removeJudicialMessage(caseData, MIGRATION_ID, mesageToBeRemoved.getId().toString()))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = " + MIGRATION_ID + ", case reference = 1}, judicial message "
                            + mesageToBeRemoved.getId() + " not found");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveSkeletonArgument {
        private final Element<SkeletonArgument> skeletonArgument1 = element(SkeletonArgument.builder().build());
        private final Element<SkeletonArgument> skeletonArgument2 = element(SkeletonArgument.builder().build());
        private final Element<SkeletonArgument> skeletonArgumentToBeRemoved =
            element(SkeletonArgument.builder().build());

        @Test
        void shouldRemoveSkeletonArgument() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .skeletonArgumentList(List.of(skeletonArgument1, skeletonArgument2, skeletonArgumentToBeRemoved))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeSkeletonArgument(caseData,
                skeletonArgumentToBeRemoved.getId().toString(), MIGRATION_ID);

            assertThat(updatedFields).extracting("skeletonArgumentList").asList()
                .containsExactly(skeletonArgument1, skeletonArgument2);
        }

        @Test
        void shouldRemoveSkeletonArgumentIfOnlyOneExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .skeletonArgumentList(List.of(skeletonArgumentToBeRemoved))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeSkeletonArgument(caseData,
                skeletonArgumentToBeRemoved.getId().toString(), MIGRATION_ID);

            assertThat(updatedFields).extracting("skeletonArgumentList").asList().isEmpty();
        }

        @Test
        void shouldThrowExceptionIfSkeletonArgumentNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .skeletonArgumentList(List.of(skeletonArgument1, skeletonArgument2))
                    .build())
                .build();

            assertThatThrownBy(() -> underTest.removeSkeletonArgument(caseData,
                    skeletonArgumentToBeRemoved.getId().toString(), MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, skeleton argument %s not found",
                    MIGRATION_ID, 1, skeletonArgumentToBeRemoved.getId().toString()));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @Nested
    class AddCourt {

        @Test
        void shouldGetCourtFieldToUpdate() {
            Court court = Court.builder().code("165").name("Carlisle").build();
            when(courtService.getCourt("165")).thenReturn(Optional.of(court));

            Map<String, Object> updatedFields = underTest.addCourt("165");

            assertThat(updatedFields).extracting("court").isEqualTo(court);
        }

        @Test
        void shouldThrowExceptionIfCourtNotFound() {
            when(courtService.getCourt("NOTCOURT")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> underTest.addCourt("NOTCOURT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Court not found with ID NOTCOURT");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveHearingFurtherEvidenceDocuments {
        private final Element<SupportingEvidenceBundle> seb1 = element(SupportingEvidenceBundle.builder()
            .build());
        private final Element<SupportingEvidenceBundle> seb2 = element(SupportingEvidenceBundle.builder()
            .build());
        private final Element<SupportingEvidenceBundle> sebToBeRemoved =
            element(SupportingEvidenceBundle.builder().build());

        private UUID hearingFurtherEvidenceBundleId = UUID.randomUUID();

        @Test
        void shouldRemoveTargetSupportingEvidenceBundle() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(
                    element(hearingFurtherEvidenceBundleId, HearingFurtherEvidenceBundle.builder()
                        .supportingEvidenceBundle(List.of(seb1, seb2, sebToBeRemoved))
                        .build())
                ))
                .build();

            Map<String, Object> updatedFields = underTest.removeHearingFurtherEvidenceDocuments(caseData, MIGRATION_ID,
                hearingFurtherEvidenceBundleId, sebToBeRemoved.getId());

            assertThat(updatedFields).extracting("hearingFurtherEvidenceDocuments").asList()
                .containsExactly(
                    element(hearingFurtherEvidenceBundleId, HearingFurtherEvidenceBundle.builder()
                        .supportingEvidenceBundle(List.of(seb1, seb2))
                        .build()
                ));
        }

        @Test
        void shouldReturnNullWhenLastSupportingEvidenceBundleIsRemoved() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(
                    element(hearingFurtherEvidenceBundleId, HearingFurtherEvidenceBundle.builder()
                        .supportingEvidenceBundle(List.of(sebToBeRemoved))
                        .build())
                ))
                .build();

            Map<String, Object> updatedFields = underTest.removeHearingFurtherEvidenceDocuments(caseData, MIGRATION_ID,
                hearingFurtherEvidenceBundleId, sebToBeRemoved.getId());

            assertThat(updatedFields).extracting("hearingFurtherEvidenceDocuments").isNull();
        }

        @Test
        void shouldThrowExceptionIfTargetSupportingEvidenceBundleNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(
                    element(hearingFurtherEvidenceBundleId, HearingFurtherEvidenceBundle.builder()
                        .supportingEvidenceBundle(List.of(seb1, seb2))
                        .build())
                ))
                .build();

            assertThatThrownBy(() -> underTest.removeHearingFurtherEvidenceDocuments(caseData, MIGRATION_ID,
                    hearingFurtherEvidenceBundleId, sebToBeRemoved.getId()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, hearing further evidence documents not found",
                    MIGRATION_ID, 1, sebToBeRemoved.getId().toString()));
        }

        @Test
        void shouldThrowExceptionIfHearingIdNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingFurtherEvidenceDocuments(List.of(
                    element(hearingFurtherEvidenceBundleId, HearingFurtherEvidenceBundle.builder()
                        .supportingEvidenceBundle(List.of(seb1, seb2, sebToBeRemoved))
                        .build())
                ))
                .build();

            assertThatThrownBy(() -> underTest.removeHearingFurtherEvidenceDocuments(caseData, MIGRATION_ID,
                UUID.randomUUID(), sebToBeRemoved.getId()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, hearing not found", MIGRATION_ID, 1));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveFurtherEvidenceSolicitorDocuments {
        private final Element<SupportingEvidenceBundle> seb1 = element(SupportingEvidenceBundle.builder()
            .build());
        private final Element<SupportingEvidenceBundle> seb2 = element(SupportingEvidenceBundle.builder()
            .build());
        private final Element<SupportingEvidenceBundle> sebToBeRemoved =
            element(SupportingEvidenceBundle.builder().build());

        private UUID hearingFurtherEvidenceBundleId = UUID.randomUUID();

        @Test
        void shouldRemoveTargetSupportingEvidenceBundle() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsSolicitor(List.of(seb1, seb2, sebToBeRemoved))
                .build();

            Map<String, Object> updatedFields = underTest.removeFurtherEvidenceSolicitorDocuments(caseData,
                MIGRATION_ID, sebToBeRemoved.getId());

            assertThat(updatedFields).extracting("furtherEvidenceDocumentsSolicitor").asList()
                .containsExactly(seb1, seb2);
        }

        @Test
        void shouldReturnNullWhenLastSupportingEvidenceBundleIsRemoved() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsSolicitor(List.of(sebToBeRemoved))
                .build();

            Map<String, Object> updatedFields = underTest.removeFurtherEvidenceSolicitorDocuments(caseData,
                MIGRATION_ID, sebToBeRemoved.getId());

            assertThat(updatedFields).extracting("furtherEvidenceDocumentsSolicitor").isNull();
        }

        @Test
        void shouldThrowExceptionIfTargetSupportingEvidenceBundleNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .furtherEvidenceDocumentsSolicitor(List.of(seb1, seb2))
                .build();

            assertThatThrownBy(() -> underTest.removeFurtherEvidenceSolicitorDocuments(caseData,
                MIGRATION_ID, sebToBeRemoved.getId()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, further evidence documents solicitor not found",
                    MIGRATION_ID, 1, sebToBeRemoved.getId().toString()));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveCourtBundleByBundleId {

        private UUID hearingId = UUID.randomUUID();

        private UUID targetBundleId = UUID.randomUUID();

        private final Element<CourtBundle> cb1 = element(CourtBundle.builder()
            .document(DocumentReference.builder().build()).build());
        private final Element<CourtBundle> cb2 = element(CourtBundle.builder()
            .document(DocumentReference.builder().build()).build());

        private final Element<HearingCourtBundle> singleCbHearingCourtBundle = element(hearingId,
            HearingCourtBundle.builder().courtBundle(List.of(
                element(targetBundleId, CourtBundle.builder().document(DocumentReference.builder().build()).build())
            ))
            .build());

        private final Element<HearingCourtBundle> mixedCourtBundlesHearingCourtBundle = element(hearingId,
            HearingCourtBundle.builder().courtBundle(List.of(cb1, cb2,
                    element(targetBundleId, CourtBundle.builder().document(DocumentReference.builder().build()).build())
                ))
                .build());

        private final Element<HearingCourtBundle> expectedHearingCourtBundle = element(hearingId,
            HearingCourtBundle.builder().courtBundle(List.of(cb1, cb2)).build());

        @Test
        void shouldRemoveTargetedCourtBundleWithOtherCourtBundleInTheSameHearing() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(mixedCourtBundlesHearingCourtBundle))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeCourtBundleByBundleId(caseData, MIGRATION_ID,
                hearingId, targetBundleId);

            assertThat(updatedFields).extracting("courtBundleListV2").asList()
                .containsExactly(expectedHearingCourtBundle);
        }

        @Test
        void shouldRemoveTargetedCourtBundleIfItIsTheOnlyCourtBundle() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(singleCbHearingCourtBundle))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.removeCourtBundleByBundleId(caseData, MIGRATION_ID,
                hearingId, targetBundleId);

            assertThat(updatedFields).extracting("courtBundleListV2")
                .isNull();
        }

        @Test
        void shouldThrowExceptionIfTargetHearingNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(mixedCourtBundlesHearingCourtBundle))
                    .build())
                .build();

            assertThatThrownBy(() -> underTest.removeCourtBundleByBundleId(caseData, MIGRATION_ID,
                UUID.randomUUID(), targetBundleId))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, hearing not found",
                    MIGRATION_ID, 1, hearingId));
        }

        @Test
        void shouldThrowExceptionIfTargetCourtBundleNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(mixedCourtBundlesHearingCourtBundle))
                    .build())
                .build();

            assertThatThrownBy(() -> underTest.removeCourtBundleByBundleId(caseData, MIGRATION_ID,
                hearingId, UUID.randomUUID()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, hearing court bundle not found",
                    MIGRATION_ID, 1, targetBundleId));
        }
    }

    @Nested
    class CaseFileViewMigrations {

        private Element<CourtBundle> buildCourtBundle() {
            return element(
                    CourtBundle.builder()
                        .document(testDocumentReference())
                        .confidential(List.of(""))
                        .hasConfidentialAddress(YesNo.NO.getValue())
                        .uploadedBy("LA")
                        .build());
        }

        private Element<CourtBundle> buildCTSCCourtBundle() {
            return element(
                CourtBundle.builder()
                    .document(testDocumentReference())
                    .confidential(List.of("CONFIDENTIAL"))
                    .hasConfidentialAddress(YesNo.YES.getValue())
                    .uploadedBy("HMCTS")
                    .build());
        }

        private Element<CourtBundle> buildLACourtBundle() {
            return element(
                CourtBundle.builder()
                    .document(testDocumentReference())
                    .confidential(List.of("CONFIDENTIAL"))
                    .hasConfidentialAddress(YesNo.YES.getValue())
                    .uploadedBy("LA")
                    .build());
        }

        @Test
        void shouldMigratePositionStatementChild() {
            Element<PositionStatementChild> positionStatementOne = element(UUID.randomUUID(),
                PositionStatementChild.builder().build());
            Element<PositionStatementChild> positionStatementTwo = element(UUID.randomUUID(),
                PositionStatementChild.builder().build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(positionStatementOne, positionStatementTwo)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementChild(caseData);
            assertThat(updatedFields).extracting("positionStatementChildListV2").isNull();
            assertThat(updatedFields).extracting("posStmtChildListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("posStmtChildList").asList()
                .contains(positionStatementTwo, positionStatementOne);
        }

        @Test
        void shouldMigratePositionStatementChildWithConfidentialAddress() {
            Element<PositionStatementChild> positionStatementWithConfidentialAddress = element(UUID.randomUUID(),
                PositionStatementChild.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<PositionStatementChild> positionStatementChildElement = element(UUID.randomUUID(),
                PositionStatementChild.builder().hasConfidentialAddress(YesNo.NO.getValue()).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementChildListV2(List.of(positionStatementWithConfidentialAddress,
                        positionStatementChildElement)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementChild(caseData);
            assertThat(updatedFields).extracting("positionStatementChildListV2").isNull();
            assertThat(updatedFields).extracting("posStmtChildListLA").asList()
                .contains(positionStatementWithConfidentialAddress);
            assertThat(updatedFields).extracting("posStmtChildList").asList()
                .contains(positionStatementChildElement);
        }

        @Test
        void shouldMigratePositionStatementRespondent() {
            Element<PositionStatementRespondent> positionStatementOne = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().build());
            Element<PositionStatementRespondent> positionStatementTwo = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(positionStatementOne, positionStatementTwo)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementRespondent(caseData);
            assertThat(updatedFields).extracting("positionStatementRespondentListV2").isNull();
            assertThat(updatedFields).extracting("posStmtRespListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("posStmtRespList").asList()
                .contains(positionStatementTwo, positionStatementOne);
        }

        @Test
        void shouldMigratePositionStatementRespondentWithConfidentialAddress() {
            Element<PositionStatementRespondent> positionStatementWithConfidentialAddress = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<PositionStatementRespondent> positionStatementRespoondentElement = element(UUID.randomUUID(),
                PositionStatementRespondent.builder().hasConfidentialAddress(YesNo.NO.getValue()).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(positionStatementWithConfidentialAddress,
                        positionStatementRespoondentElement)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migratePositionStatementRespondent(caseData);
            assertThat(updatedFields).extracting("positionStatementRespondentListV2").isNull();
            assertThat(updatedFields).extracting("posStmtRespListLA").asList()
                .contains(positionStatementWithConfidentialAddress);
            assertThat(updatedFields).extracting("posStmtRespList").asList()
                .contains(positionStatementRespoondentElement);
        }

        @Test
        void shouldMigrateNonConfidentialRespondentStatement() {
            UUID respondentOneId = UUID.randomUUID();
            UUID respondentTwoId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .build()));
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateNonConfidentialMultipleRespondentStatements() {
            UUID respondentOneId = UUID.randomUUID();
            UUID respondentTwoId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();
            UUID doc2Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .build();

            DocumentReference document2 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebTwo = SupportingEvidenceBundle.builder()
                .document(document2)
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());
            Element<RespondentStatement> respondentStatementTwo = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentTwoId).respondentName("NAME 2")
                    .supportingEvidenceBundle(List.of(element(doc2Id, sebTwo)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne, respondentStatementTwo))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .build()),
                    element(doc2Id, RespondentStatementV2.builder()
                        .respondentId(respondentTwoId)
                        .respondentName("NAME 2")
                        .document(document2)
                        .build()));
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateConfidentialRespondentStatementByLA() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .confidential(List.of("CONFIDENTIAL"))
                        .build()));
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateRespondentStatementContainsConfidentialAddressByLA() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .hasConfidentialAddress("Yes")
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .hasConfidentialAddress("Yes")
                        .build()));
            assertThat(updatedFields).extracting("respStmtListCTSC").asList().isEmpty();
        }

        @Test
        void shouldMigrateConfidentialRespondentStatementByCTSC() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .confidential(List.of("CONFIDENTIAL"))
                .uploadedBy("HMCTS")
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .confidential(List.of("CONFIDENTIAL"))
                        .uploadedBy("HMCTS")
                        .build()));
        }

        @Test
        void shouldMigrateRespondentStatementContainsConfidentialAddressByCTSC() {
            UUID respondentOneId = UUID.randomUUID();

            UUID doc1Id = UUID.randomUUID();

            DocumentReference document1 = DocumentReference.builder().build();
            SupportingEvidenceBundle sebOne = SupportingEvidenceBundle.builder()
                .document(document1)
                .hasConfidentialAddress("Yes")
                .uploadedBy("HMCTS")
                .build();

            Element<RespondentStatement> respondentStatementOne = element(UUID.randomUUID(),
                RespondentStatement.builder().respondentId(respondentOneId).respondentName("NAME 1")
                    .supportingEvidenceBundle(List.of(element(doc1Id, sebOne)))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .respondentStatements(List.of(respondentStatementOne))
                .build();

            Map<String, Object> updatedFields = underTest.migrateRespondentStatement(caseData);

            assertThat(updatedFields).extracting("respondentStatements").isNull();
            assertThat(updatedFields).extracting("respStmtList").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("respStmtListCTSC").asList()
                .contains(
                    element(doc1Id, RespondentStatementV2.builder()
                        .respondentId(respondentOneId)
                        .respondentName("NAME 1")
                        .document(document1)
                        .hasConfidentialAddress("Yes")
                        .uploadedBy("HMCTS")
                        .build()));
        }

        void shouldMoveSingleCaseSummaryWithConfidentialAddressToCaseSummaryListLA() {
            Element<CaseSummary> caseSummaryListElement = element(UUID.randomUUID(), CaseSummary.builder()
                .hasConfidentialAddress(YesNo.YES.getValue())
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(List.of(caseSummaryListElement))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(
                caseData);
            assertThat(updatedFields).extracting("caseSummaryList").asList().isEmpty();
            assertThat(updatedFields).extracting("caseSummaryListLA").asList()
                .containsExactly(caseSummaryListElement);
        }

        @Test
        void shouldMoveOneOfCaseSummariesWithConfidentialAddressToCaseSummaryListLA() {
            Element<CaseSummary> caseSummaryListElementWithConfidentialAddress = element(UUID.randomUUID(),
                CaseSummary.builder().hasConfidentialAddress(YesNo.YES.getValue()).build());
            Element<CaseSummary> caseSummaryListElement = element(UUID.randomUUID(), CaseSummary.builder()
                .hasConfidentialAddress(YesNo.NO.getValue())
                .build());
            Element<CaseSummary> caseSummaryListElementTwo = element(UUID.randomUUID(), CaseSummary.builder()
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .caseSummaryList(List.of(caseSummaryListElement, caseSummaryListElementWithConfidentialAddress,
                        caseSummaryListElementTwo))
                    .build())
                .build();

            Map<String, Object> updatedFields = underTest.moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(
                caseData);
            assertThat(updatedFields).extracting("caseSummaryList").asList()
                .containsExactly(caseSummaryListElement, caseSummaryListElementTwo);
            assertThat(updatedFields).extracting("caseSummaryListLA").asList()
                .containsExactly(caseSummaryListElementWithConfidentialAddress);
        }

        @Test
        void nonConfidentialCourtBundlesShouldRemainInCourtBundleList() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> courtBundleOne = element(hearingId, HearingCourtBundle.builder()
                    .courtBundle(List.of(buildCourtBundle())).build());

            Element<HearingCourtBundle> courtBundleTwo = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(courtBundleOne, courtBundleTwo)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateCourtBundle(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("courtBundleListCTSC").asList().isEmpty();
            assertThat(updatedFields).extracting("courtBundleListV2").asList().size().isEqualTo(2);
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(courtBundleOne, courtBundleTwo);
        }

        @Test
        void confidentialCourtBundlesUploadedByCTSCShouldGoIntoCourtBundleListCTSC() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> confidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> confidentialBundleTwo = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> nonConfidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(confidentialBundle,
                        confidentialBundleTwo, nonConfidentialBundle)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateCourtBundle(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").asList().isEmpty();
            assertThat(updatedFields).extracting("courtBundleListCTSC").asList().contains(confidentialBundle,
                confidentialBundleTwo);
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(nonConfidentialBundle);
        }

        @Test
        void confidentialCourtBundlesUploadedByLAShouldGoIntoCourtBundleListLA() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> confidentialBundleLA = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildLACourtBundle())).build());

            Element<HearingCourtBundle> confidentialBundleCTSC = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> nonConfidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(confidentialBundleLA,
                        confidentialBundleCTSC, nonConfidentialBundle)).build())
                .build();

            Map<String, Object> updatedFields = underTest.migrateCourtBundle(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").asList().contains(confidentialBundleLA);
            assertThat(updatedFields).extracting("courtBundleListCTSC").asList().contains(confidentialBundleCTSC);
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(nonConfidentialBundle);
        }

        @Test
        void courtBundlesShouldBeInSameListAfterRollback() {
            UUID hearingId = UUID.randomUUID();

            Element<HearingCourtBundle> confidentialBundleLA = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildLACourtBundle())).build());

            Element<HearingCourtBundle> confidentialBundleCTSC = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCTSCCourtBundle())).build());

            Element<HearingCourtBundle> nonConfidentialBundle = element(hearingId, HearingCourtBundle.builder()
                .courtBundle(List.of(buildCourtBundle())).build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .hearingDocuments(HearingDocuments.builder()
                    .courtBundleListV2(List.of(nonConfidentialBundle))
                    .courtBundleListLA(List.of(confidentialBundleLA))
                    .courtBundleListCTSC(List.of(confidentialBundleCTSC)).build())
                .build();

            Map<String, Object> updatedFields = underTest.rollbackCourtBundleMigration(caseData);
            assertThat(updatedFields).extracting("courtBundleListLA").isNull();
            assertThat(updatedFields).extracting("courtBundleListCTSC").isNull();
            assertThat(updatedFields).extracting("courtBundleListV2").asList().contains(nonConfidentialBundle,
                confidentialBundleLA, confidentialBundleCTSC);
        }

        @Test
        void unusedCourtBundlesShouldBeRemovedAfterRollback() {
            Map<String, Object> map = new HashMap<>();
            map.put("courtBundleListLA", List.of());
            map.put("courtBundleListCTSC", List.of());

            CaseDetails caseDetails = CaseDetails.builder().data(map).build();

            underTest.removeUnusedCourtBundleFields(caseDetails);
            assertThat(caseDetails.getData()).doesNotContainKey("courtBundleListLA");
            assertThat(caseDetails.getData()).doesNotContainKey("courtBundleListCTSC");
        }
    }
}
