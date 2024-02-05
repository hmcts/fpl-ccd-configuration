package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Mock
    private MigrateRelatingLAService migrateRelatingLAService;

    @Mock
    private OrganisationService organisationService;

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

    @Mock
    private CaseConverter caseConverter;

    @Nested
    class FixIncorrectCaseManagementLocation {

        private final long caseId = 1L;

        @Test
        public void shouldFixIncorrectCaseManagementLocation() {
            CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();
            when(caseConverter.convert(caseDetails)).thenReturn(CaseData.builder().id(caseId)
                .court(Court.builder().code("270").build()).build());

            assertThat(underTest.fixIncorrectCaseManagementLocation(caseDetails, MIGRATION_ID))
                .extracting("caseManagementLocation", "court")
                .satisfies(tuple -> {
                    assertThat(tuple.get(0))
                        .isInstanceOf(CaseLocation.class)
                        .hasFieldOrPropertyWithValue("region", "3")
                        .hasFieldOrPropertyWithValue("baseLocation", "195537");

                    // Add assertions for 'court' if needed
                    assertThat(tuple.get(1))
                        .isInstanceOf(Court.class)
                        .hasFieldOrPropertyWithValue("epimmsId", "195537");
                });
        }

        @Test
        public void shouldThrowExceptionIfTargetCourtDoesNotMatch() {
            CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();
            when(caseConverter.convert(caseDetails)).thenReturn(CaseData.builder().id(caseId)
                .court(Court.builder().code("999").build()).build());

            assertThatThrownBy(() -> underTest
                .fixIncorrectCaseManagementLocation(caseDetails, MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, Case data does not contain the target court: 270",
                    MIGRATION_ID, caseId));
        }

        @Test
        public void shouldThrowExceptionIfCaseHavingCorrectCaseManagmentLocation() {
            CaseDetails caseDetails = CaseDetails.builder().data(Map.of("caseManagementLocation",
                Map.of("baseLocation", "195537", "region", "3"))).build();
            when(caseConverter.convert(caseDetails)).thenReturn(CaseData.builder().id(caseId)
                .court(Court.builder().code("270").build()).build());

            assertThatThrownBy(() -> underTest
                .fixIncorrectCaseManagementLocation(caseDetails, MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, `caseManagementLocation` is correct.",
                    MIGRATION_ID, caseId));
        }
    }

    @Nested
    class UpdateThirdPartyStandaloneApplicant {

        private final String previousOrgId = "ABCDEFG";
        private final String previousOrgName = "Previous Organisation Name";

        private final String newOrgId = "HIJKLMN";
        private final String newOrgName = "New Organisation Name";

        private final String caseRole = "[SOLICITORA]";

        private final Organisation previousOrganisation = Organisation.builder()
            .organisationID(previousOrgId)
            .organisationName(previousOrgName)
            .build();

        private final Organisation newOrganisation = Organisation.builder()
            .organisationID(newOrgId)
            .organisationName(newOrgName)
            .build();

        @Test
        void updateOutsourcingPolicy() {
            when(organisationService.findOrganisation(newOrgId))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.rd.model.Organisation.builder()
                        .name(newOrgName)
                    .build()));
            CaseData caseData = CaseData.builder()
                .id(1L)
                .outsourcingPolicy(OrganisationPolicy.builder()
                    .organisation(previousOrganisation)
                    .orgPolicyCaseAssignedRole(caseRole)
                    .build())
                .build();

            Map<String, OrganisationPolicy> fields = underTest.changeThirdPartyStandaloneApplicant(caseData, newOrgId);
            OrganisationPolicy updatedOrgPolicy = fields.get("outsourcingPolicy");
            assertThat(updatedOrgPolicy).isEqualTo(OrganisationPolicy.builder()
                .organisation(newOrganisation)
                .orgPolicyCaseAssignedRole(caseRole)
                .build());
        }

        @Test
        void removeApplicantEmailAndStopNotifyingTheirColleagues() {
            Element<Colleague> colleague1 = element(Colleague.builder().email("colleague1@email.com")
                .notificationRecipient(YesNo.YES.getValue()).build());
            Element<Colleague> colleague2 = element(Colleague.builder().email("colleague2@email.com")
                    .notificationRecipient(YesNo.YES.getValue()).build());
            Element<Colleague> colleague3 = element(Colleague.builder().email("colleague3@email.com")
                .notificationRecipient(YesNo.YES.getValue()).build());

            Element<LocalAuthority> localAuthority1 = element(LocalAuthority.builder()
                .email("localAuthority1@email.com")
                .colleagues(List.of(colleague1, colleague2))
                .build());
            Element<LocalAuthority> localAuthority2 = element(LocalAuthority.builder()
                .email("localAuthority2@email.com")
                .colleagues(List.of(colleague3))
                .build());

            Element<LocalAuthority> expectedlocalAuthority1 = element(localAuthority1.getId(),
                localAuthority1.getValue().toBuilder()
                    .email(null)
                    .colleagues(List.of(
                        element(colleague1.getId(),
                            colleague1.getValue().toBuilder().notificationRecipient(YesNo.NO.getValue()).build()),
                        element(colleague2.getId(), colleague2.getValue())))
                    .build());

            Element<LocalAuthority> expectedlocalAuthority2 = element(localAuthority2.getId(),
                localAuthority2.getValue().toBuilder()
                    .colleagues(List.of(element(colleague3.getId(), colleague3.getValue().toBuilder().build())))
                    .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .localAuthorities(List.of(localAuthority1, localAuthority2))
                .build();

            Map<String, Object> result = underTest.removeApplicantEmailAndStopNotifyingTheirColleagues(caseData,
                MIGRATION_ID, localAuthority1.getId().toString());

            assertThat(result.get("localAuthorities"))
                .isEqualTo(List.of(expectedlocalAuthority1, expectedlocalAuthority2));
        }

        @Test
        void throwExceptionIfApplicantNotFound() {
            Element<Colleague> colleague1 = element(Colleague.builder().email("colleague1@email.com")
                .notificationRecipient(YesNo.YES.getValue()).build());
            Element<Colleague> colleague2 = element(Colleague.builder().email("colleague2@email.com")
                .notificationRecipient(YesNo.YES.getValue()).build());
            Element<Colleague> colleague3 = element(Colleague.builder().email("colleague3@email.com")
                .notificationRecipient(YesNo.YES.getValue()).build());

            Element<LocalAuthority> localAuthority1 = element(LocalAuthority.builder()
                .email("localAuthority1@email.com")
                .colleagues(List.of(colleague1, colleague2))
                .build());

            CaseData caseData = CaseData.builder()
                .id(1L)
                .localAuthorities(List.of(localAuthority1))
                .build();

            AssertionError actualException = assertThrows(AssertionError.class, () -> {
                underTest.removeApplicantEmailAndStopNotifyingTheirColleagues(caseData,
                    MIGRATION_ID, UUID.randomUUID().toString());
            });
            assertThat(actualException.getMessage()).isEqualTo(format(
                "Migration {id = %s, case reference = %s}, invalid local authorities (applicant)",
                MIGRATION_ID, caseData.getId()));
        }
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
        private final UUID doc2IdToRemove = UUID.randomUUID();
        private final UUID docIdToKeep = UUID.randomUUID();

        private final Element<PositionStatementChild> docToRemove = element(docIdToRemove,
            PositionStatementChild.builder().build());
        private final Element<PositionStatementChild> doc2ToRemove = element(doc2IdToRemove,
            PositionStatementChild.builder().build());

        private final Element<PositionStatementChild> docToKeep = element(docIdToKeep,
            PositionStatementChild.builder().build());

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRemovePositionStatementChildByUUID(boolean isInLaList) {
            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildListLA(List.of(docToRemove)).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildList(List.of(docToRemove)).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementChild(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove);

            assertThat(fields.get("posStmtChildList" + (isInLaList ? "LA" : ""))).isEqualTo(List.of());
            assertThat(fields.get("posStmtChildList" + (isInLaList ? "" : "LA"))).isNull();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRemovePositionStatementChildByUUIDs(boolean isInLaList) {
            List<Element<PositionStatementChild>> positionStatements = new ArrayList<>();
            positionStatements.add(docToRemove);
            positionStatements.add(doc2ToRemove);

            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildListLA(positionStatements).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildList(positionStatements).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementChild(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove, doc2IdToRemove);

            assertThat(fields.get("posStmtChildList" + (isInLaList ? "LA" : ""))).isEqualTo(List.of());
            assertThat(fields.get("posStmtChildList" + (isInLaList ? "" : "LA"))).isNull();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntact(boolean isInLaList) {
            List<Element<PositionStatementChild>> positionStatements = new ArrayList<>();
            positionStatements.add(docToKeep);
            positionStatements.add(docToRemove);

            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildListLA(positionStatements).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildList(positionStatements).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementChild(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove);

            List<Element<PositionStatementChild>> resultsPositionStatements =
                (List<Element<PositionStatementChild>>) fields.get("posStmtChildList" + (isInLaList ? "LA" : ""));

            assertThat(resultsPositionStatements).hasSize(1);
            assertThat(resultsPositionStatements).containsExactly(docToKeep);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntactWhenRemovingMultipleTargets(boolean isInLaList) {
            List<Element<PositionStatementChild>> positionStatements = new ArrayList<>();
            positionStatements.add(docToKeep);
            positionStatements.add(docToRemove);
            positionStatements.add(doc2ToRemove);

            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildListLA(positionStatements).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtChildList(positionStatements).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementChild(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove, doc2IdToRemove);

            List<Element<PositionStatementChild>> resultsPositionStatements =
                (List<Element<PositionStatementChild>>) fields.get("posStmtChildList" + (isInLaList ? "LA" : ""));

            assertThat(resultsPositionStatements).hasSize(1);
            assertThat(resultsPositionStatements).containsExactly(docToKeep);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldThrowExceptionIfDocumentNotFound(boolean isInLaList) {
            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder()
                    .posStmtChildListLA(List.of(element(PositionStatementChild.builder().build()))).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder()
                    .posStmtChildList(List.of(element(PositionStatementChild.builder().build()))).build());
            }
            CaseData caseData = builder.build();

            assertThrows(AssertionError.class, () ->
                underTest.removePositionStatementChild(caseData, MIGRATION_ID, false, docIdToRemove));
        }
    }

    @Nested
    class RemovePositionStatementRespondent {

        private final UUID docIdToRemove = UUID.randomUUID();
        private final UUID doc2IdToRemove = UUID.randomUUID();
        private final UUID docIdToKeep = UUID.randomUUID();

        private final Element<PositionStatementRespondent> docToRemove = element(docIdToRemove,
            PositionStatementRespondent.builder().build());
        private final Element<PositionStatementRespondent> doc2ToRemove = element(doc2IdToRemove,
            PositionStatementRespondent.builder().build());

        private final Element<PositionStatementRespondent> docToKeep = element(docIdToKeep,
            PositionStatementRespondent.builder().build());

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRemovePositionStatementRespondentByUUID(boolean isInLaList) {
            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespListLA(List.of(docToRemove)).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespList(List.of(docToRemove)).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementRespondent(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove);

            assertThat(fields.get("posStmtRespList" + (isInLaList ? "LA" : ""))).isEqualTo(List.of());
            assertThat(fields.get("posStmtRespList" + (isInLaList ? "" : "LA"))).isNull();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldRemovePositionStatementRespondentByUUIDs(boolean isInLaList) {
            List<Element<PositionStatementRespondent>> positionStatements = new ArrayList<>();
            positionStatements.add(docToRemove);
            positionStatements.add(doc2ToRemove);

            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespListLA(positionStatements).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespList(positionStatements).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementRespondent(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove, doc2IdToRemove);

            assertThat(fields.get("posStmtRespList" + (isInLaList ? "LA" : ""))).isEqualTo(List.of());
            assertThat(fields.get("posStmtRespList" + (isInLaList ? "" : "LA"))).isNull();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntact(boolean isInLaList) {
            List<Element<PositionStatementRespondent>> positionStatements = new ArrayList<>();
            positionStatements.add(docToKeep);
            positionStatements.add(docToRemove);

            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespListLA(positionStatements).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespList(positionStatements).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementRespondent(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove);

            List<Element<PositionStatementRespondent>> resultsPositionStatements =
                (List<Element<PositionStatementRespondent>>) fields.get("posStmtRespList" + (isInLaList ? "LA" : ""));

            assertThat(resultsPositionStatements).hasSize(1);
            assertThat(resultsPositionStatements).containsExactly(docToKeep);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @SuppressWarnings("unchecked")
        void shouldLeaveOtherDocsIntactWhenRemovingMultipleTargets(boolean isInLaList) {
            List<Element<PositionStatementRespondent>> positionStatements = new ArrayList<>();
            positionStatements.add(docToKeep);
            positionStatements.add(docToRemove);
            positionStatements.add(doc2ToRemove);

            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespListLA(positionStatements).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder().posStmtRespList(positionStatements).build());
            }
            CaseData caseData = builder.build();

            Map<String, Object> fields = underTest.removePositionStatementRespondent(caseData, MIGRATION_ID, isInLaList,
                docIdToRemove, doc2IdToRemove);

            List<Element<PositionStatementRespondent>> resultsPositionStatements =
                (List<Element<PositionStatementRespondent>>) fields.get("posStmtRespList" + (isInLaList ? "LA" : ""));

            assertThat(resultsPositionStatements).hasSize(1);
            assertThat(resultsPositionStatements).containsExactly(docToKeep);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldThrowExceptionIfDocumentNotFound(boolean isInLaList) {
            CaseData.CaseDataBuilder builder = CaseData.builder();
            if (isInLaList) {
                builder.hearingDocuments(HearingDocuments.builder()
                    .posStmtRespListLA(List.of(element(PositionStatementRespondent.builder().build()))).build());
            } else {
                builder.hearingDocuments(HearingDocuments.builder()
                    .posStmtRespList(List.of(element(PositionStatementRespondent.builder().build()))).build());
            }
            CaseData caseData = builder.build();

            assertThrows(AssertionError.class, () ->
                underTest.removePositionStatementRespondent(caseData, MIGRATION_ID, false, docIdToRemove));
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
        private final String fileName = "Test Filname.pdf";

        @Test
        void shouldThrowAssertionIfOrderNotFound() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .build();

            assertThrows(AssertionError.class, () ->
                underTest.verifyGatekeepingOrderUrgentHearingOrderExistWithGivenFileName(caseData, MIGRATION_ID,
                    fileName));
        }

        @Test
        void shouldThrowExceptionIfUrgentDirectionIsNullOrEmpty() {
            UUID documentId = UUID.randomUUID();
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .build();

            assertThrows(AssertionError.class, () -> underTest
                .verifyUrgentDirectionsOrderExists(caseData, MIGRATION_ID, documentId));
        }

        @Test
        void shouldThrowExceptionIfStandardDirectionNotMatching() {
            UUID document1Id = UUID.randomUUID();
            String document2Url = "http://dm-store-prod.service.core-compute-prod.internal/documents/"
                + UUID.randomUUID();
            DocumentReference documentReference = DocumentReference.builder()
                .url(document2Url)
                .filename("Test Document")
                .build();

            CaseData caseData = CaseData.builder()
                .id(caseId)
                .urgentDirectionsOrder(
                    StandardDirectionOrder.builder()
                        .orderDoc(documentReference)
                        .build())
                .build();

            assertThrows(AssertionError.class, () -> underTest
                .verifyUrgentDirectionsOrderExists(caseData, MIGRATION_ID, document1Id));
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
                    fileName));
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
    class RemoveGatekeepingOrderStandardDirectionOrder {

        private final long caseId = 1L;
        private final String fileName = "Test Filname.pdf";

        @Test
        void shouldThrowExceptionIfStandardDirectionOrderIsNullOrEmpty() {
            UUID documentId = UUID.randomUUID();
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .build();

            assertThrows(AssertionError.class, () -> underTest
                .verifyStandardDirectionOrderExists(caseData, MIGRATION_ID, documentId));
        }

        @Test
        void shouldThrowExceptionIfStandardDirectionOrderNotMatching() {
            UUID document1Id = UUID.randomUUID();
            String document2Url = "http://dm-store-prod.service.core-compute-prod.internal/documents/"
                + UUID.randomUUID();
            DocumentReference documentReference = DocumentReference.builder()
                .url(document2Url)
                .filename("Test Document")
                .build();

            CaseData caseData = CaseData.builder()
                .id(caseId)
                .standardDirectionOrder(
                    StandardDirectionOrder.builder()
                        .orderDoc(documentReference)
                        .build())
                .build();

            assertThrows(AssertionError.class, () -> underTest
                .verifyStandardDirectionOrderExists(caseData, MIGRATION_ID, document1Id));
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
            var placementRemaining = element(placementToRemain, Placement.builder()
                            .build());

            List<Element<Placement>> placements = List.of(
                element(placementToRemove, Placement.builder()
                    .build()), placementRemaining);

            List<Element<Placement>> placementsRemaining = List.of(placementRemaining);

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

        @Test
        void shouldRemoveClosedJudicialMessage() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .closedJudicialMessages(List.of(message1, message2, mesageToBeRemoved))
                .build();

            Map<String, Object> updates =
                underTest.removeClosedJudicialMessage(caseData, MIGRATION_ID, mesageToBeRemoved.getId().toString());
            assertThat(updates).extracting("closedJudicialMessages").asList().containsExactly(message1, message2);
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveNoticeOfProceedingsBundle {
        private final Element<DocumentBundle> noticeOfProceedings1 =
            element(DocumentBundle.builder().build());
        private final Element<DocumentBundle> noticeOfProceedings2 =
            element(DocumentBundle.builder().build());
        private final Element<DocumentBundle> noticeOfProceedingsToBeRemoved =
            element(DocumentBundle.builder().build());

        @Test
        void shouldRemoveNoticeOfProceedingsBundle() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .noticeOfProceedingsBundle(List.of(noticeOfProceedings1, noticeOfProceedings2,
                    noticeOfProceedingsToBeRemoved))
                .build();

            Map<String, Object> updatedFields = underTest.removeNoticeOfProceedingsBundle(caseData,
                noticeOfProceedingsToBeRemoved.getId().toString(), MIGRATION_ID);

            assertThat(updatedFields).extracting("noticeOfProceedingsBundle").asList()
                .containsExactly(noticeOfProceedings1, noticeOfProceedings2);
        }

        @Test
        void shouldRemoveNoticeOfProceedingsBundleIfOnlyOneExists() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .noticeOfProceedingsBundle(List.of(noticeOfProceedingsToBeRemoved))
                .build();

            Map<String, Object> updatedFields = underTest.removeNoticeOfProceedingsBundle(caseData,
                noticeOfProceedingsToBeRemoved.getId().toString(), MIGRATION_ID);

            assertThat(updatedFields).extracting("noticeOfProceedingsBundle").asList().isEmpty();
        }

        @Test
        void shouldThrowExceptionIfNoticeOfProceedingsBundleDoesNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .noticeOfProceedingsBundle(List.of(noticeOfProceedings1, noticeOfProceedings2))
                .build();

            assertThatThrownBy(() -> underTest.removeNoticeOfProceedingsBundle(caseData,
                noticeOfProceedingsToBeRemoved.getId().toString(), MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s},"
                        + " notice of proceedings bundle %s not found",
                    MIGRATION_ID, 1, noticeOfProceedingsToBeRemoved.getId().toString()));
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
    class RemoveCorrespondenceDocument {
        private final Element<SupportingEvidenceBundle> correspondenceDocument1 =
            element(SupportingEvidenceBundle.builder().build());
        private final Element<SupportingEvidenceBundle> correspondenceDocument2 =
            element(SupportingEvidenceBundle.builder().build());
        private final Element<SupportingEvidenceBundle> correspondenceDocumentToBeRemoved =
            element(SupportingEvidenceBundle.builder().build());
        private final Element<SupportingEvidenceBundle> correspondenceDocumentConfidential =
            element(SupportingEvidenceBundle.builder().hasConfidentialAddress("Yes").build());

        @Test
        void shouldRemoveCorrespondenceDocument() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(
                    List.of(correspondenceDocument1, correspondenceDocument2, correspondenceDocumentToBeRemoved))
                .build();

            Map<String, Object> updatedFields = underTest.removeCorrespondenceDocument(caseData,
                MIGRATION_ID, correspondenceDocumentToBeRemoved.getId());

            assertThat(updatedFields).extracting("correspondenceDocuments").asList()
                .containsExactly(correspondenceDocument1, correspondenceDocument2);
            assertThat(updatedFields).extracting("correspondenceDocumentsNC").asList()
                .containsExactly(correspondenceDocument1, correspondenceDocument2);
        }

        @Test
        void shouldRemoveCorrespondenceDocumentIfOnlyOneExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(List.of(correspondenceDocumentToBeRemoved))
                .build();

            Map<String, Object> updatedFields = underTest.removeCorrespondenceDocument(caseData,
                MIGRATION_ID, correspondenceDocumentToBeRemoved.getId());

            assertThat(updatedFields).extracting("correspondenceDocuments").asList().isEmpty();
            assertThat(updatedFields).extracting("correspondenceDocumentsNC").asList().isEmpty();
        }

        @Test
        void shouldThrowExceptionIfCorrespondenceDocumentNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(List.of(correspondenceDocument1, correspondenceDocument2))
                .build();

            assertThatThrownBy(() -> underTest.removeCorrespondenceDocument(caseData,
                MIGRATION_ID, correspondenceDocumentToBeRemoved.getId()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, correspondence document not found",
                    MIGRATION_ID, 1));
        }

        @Test
        void shouldNotPutConfidentialDocsInNc() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .correspondenceDocuments(List.of(correspondenceDocument1, correspondenceDocument2,
                    correspondenceDocumentToBeRemoved, correspondenceDocumentConfidential))
                .build();

            Map<String, Object> updatedFields = underTest.removeCorrespondenceDocument(caseData,
                MIGRATION_ID, correspondenceDocumentToBeRemoved.getId());

            assertThat(updatedFields).extracting("correspondenceDocuments").asList()
                .containsExactly(correspondenceDocument1, correspondenceDocument2, correspondenceDocumentConfidential);
            assertThat(updatedFields).extracting("correspondenceDocumentsNC").asList()
                .containsExactly(correspondenceDocument1, correspondenceDocument2);
        }
    }

    @Nested
    class MigrateRelatingLA {

        CaseData caseData = CaseData.builder()
            .id(1234L)
            .build();

        @Test
        void shouldThrowExceptionIfCaseNotInConfig() {
            when(migrateRelatingLAService.getRelatingLAString("1234")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> underTest.addRelatingLA(MIGRATION_ID, caseData.getId()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, case not found in migration list",
                    MIGRATION_ID, "1234"));
        }

        @Test
        void shouldPopulateRelatingLAIfCaseNotInConfig() {
            when(migrateRelatingLAService.getRelatingLAString("1234")).thenReturn(Optional.of("ABC"));

            Map<String, Object> updatedFields = underTest.addRelatingLA(MIGRATION_ID, caseData.getId());

            assertThat(updatedFields).extracting("relatingLA").isEqualTo("ABC");
        }

    }

    @Nested
    class RemoveSealedCMO {
        private final Element<HearingOrder> sealedCmo1 = ElementUtils.element(HearingOrder.builder().build());
        private final Element<HearingOrder> sealedCmo2 = ElementUtils.element(HearingOrder.builder().build());
        private final List<Element<HearingOrder>> sealedCmos = List.of(sealedCmo1, sealedCmo2);
        private final List<Element<HearingOrder>> orderToBeSent = List.of(sealedCmo1, sealedCmo2);

        @Test
        void shouldRemoveSealedCMO() {
            CaseData caseData = CaseData.builder().id(1L)
                .sealedCMOs(sealedCmos)
                .ordersToBeSent(orderToBeSent)
                .build();

            Map<String, Object> updatedFields = underTest.removeSealedCMO(caseData, MIGRATION_ID, sealedCmo1.getId(),
                true);

            List<Element<HearingOrder>> expectedList = List.of(sealedCmo2);

            assertThat(updatedFields).extracting("sealedCMOs").isEqualTo(expectedList);
            assertThat(updatedFields).extracting("ordersToBeSent").isEqualTo(expectedList);
        }

        @Test
        void shouldRemoveSealedCMOIfNoOrdersToBeSent() {
            CaseData caseData = CaseData.builder().id(1L)
                .sealedCMOs(sealedCmos)
                .build();

            Map<String, Object> updatedFields =
                underTest.removeSealedCMO(caseData, MIGRATION_ID, sealedCmo1.getId(), false);

            List<Element<HearingOrder>> expectedList = List.of(sealedCmo2);

            assertThat(updatedFields).extracting("sealedCMOs").isEqualTo(expectedList);
            assertThat(updatedFields).extracting("ordersToBeSent").isNull();
        }

        @Test
        void shouldThrowExceptionIfCMONotFound() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() -> underTest.removeSealedCMO(caseData, MIGRATION_ID, sealedCmo1.getId(), false))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, Sealed CMO not found, %s",
                    MIGRATION_ID, "1", sealedCmo1.getId()));
        }

        @Test
        void shouldThrowExceptionIfOrderToBeSentNotFound() {
            CaseData caseData = CaseData.builder().id(1L)
                .sealedCMOs(sealedCmos)
                .ordersToBeSent(List.of(sealedCmo2))
                .build();

            assertThatThrownBy(() -> underTest.removeSealedCMO(caseData, MIGRATION_ID, sealedCmo1.getId(), true))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format(
                    "Migration {id = %s, case reference = %s}, Order to be sent not found, %s",
                    MIGRATION_ID, "1", sealedCmo1.getId()));
        }
    }

    @Nested
    class ClearChangeOrganisationRequest {

        @Test
        void shouldClearChangeOrganisationRequestFields() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(new HashMap<>(Map.of("changeOrganisationRequestField", ChangeOrganisationRequest.builder()
                    .caseRoleId(DynamicList.builder()
                        .value(DynamicListElement.builder().code("TEST").build())
                        .build())
                    .build())))
                .build();
            underTest.clearChangeOrganisationRequest(caseDetails);
            assertThat(caseDetails.getData().get("changeOrganisationRequestField")).isNull();
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class RemoveLocalAuthority {
        private final Element<LocalAuthority> localAuthority1 = element(LocalAuthority.builder().build());
        private final Element<LocalAuthority> localAuthority2 = element(LocalAuthority.builder().build());
        private final Element<LocalAuthority> localAuthorityToBeRemoved =
            element(LocalAuthority.builder().build());

        @Test
        void shouldRemoveLocalAuthority() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .localAuthorities(List.of(localAuthority1, localAuthority2, localAuthorityToBeRemoved))
                .build();

            Map<String, List<Element<LocalAuthority>>> updatedFields =
                underTest.removeElementFromLocalAuthorities(caseData, MIGRATION_ID, localAuthorityToBeRemoved.getId());

            assertThat(updatedFields).extracting("localAuthorities").asList()
                .containsExactly(localAuthority1, localAuthority2);
        }

        @Test
        void shouldRemoveLocalAuthorityIfOnlyOneExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .localAuthorities(List.of(localAuthorityToBeRemoved))
                .build();

            Map<String, List<Element<LocalAuthority>>> updatedFields =
                underTest.removeElementFromLocalAuthorities(caseData, MIGRATION_ID, localAuthorityToBeRemoved.getId());

            assertThat(updatedFields).extracting("localAuthorities").asList().isEmpty();
        }

        @Test
        void shouldThrowExceptionIfLocalAuthorityNotExist() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .localAuthorities(List.of(localAuthority1, localAuthority2))
                .build();

            assertThatThrownBy(() -> underTest.removeElementFromLocalAuthorities(caseData, MIGRATION_ID,
                localAuthorityToBeRemoved.getId()))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, invalid local authorities",
                    MIGRATION_ID, 1, localAuthorityToBeRemoved.getId().toString()));
        }
    }

    @Nested
    class RemoveStringFromThresholdDetails {
        private final String testThresholdDetails = "\nBETWEEN\n\nNON-DESCRIPT BOROUGH COUNCIL\nApplicant\n-and-\n"
            + "\nJIM DAVIES\n1st Respondent\n-and-\n\nPETER PARKER (PUTATIVE FATHER)\n2nd Respondent\n-and-\n\nTIMOTHY"
            + "\t\n3rd Respondent\n\nFREDRICK (FRED) FREDERSON AND BOB BINS \n(By their Children’s Guardian)"
            + "\n3rd-5th Respondents\n\n___________________________________________\n\nTHRESHOLD DOCUMENT";

        private final String expectedThresholdDetails = "\nBETWEEN\n\nNON-DESCRIPT BOROUGH COUNCIL\nApplicant\n-and-\n"
            + "\nJIM DAVIES\n1st Respondent\n-and-\n\nPETER PARKER (PUTATIVE FATHER)\n2nd Respondent\n-and-\n\nTIMOTHY"
            + "\t\n3rd Respondent\n\n___________________________________________\n\nTHRESHOLD DOCUMENT";

        @Test
        void shouldRemoveSpecificStringFromThresholdDetails() {
            var thresholdDetailsStartIndex = 167;
            var thresholdDetailsEndIndex = 259;

            final Grounds expectedGrounds = Grounds.builder()
                .thresholdDetails(expectedThresholdDetails)
                .thresholdReason(List.of("noCare"))
                .build();

            final Grounds grounds = Grounds.builder()
                .thresholdDetails(testThresholdDetails)
                .thresholdReason(List.of("noCare"))
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .grounds(grounds)
                .build();

            Map<String, Object> updatedGrounds = underTest.removeCharactersFromThresholdDetails(caseData, MIGRATION_ID,
                thresholdDetailsStartIndex, thresholdDetailsEndIndex);

            assertThat(updatedGrounds).extracting("grounds").isEqualTo(expectedGrounds);
        }

        @Test
        void shouldThrowExceptionIfNoThresholdDetailsOrOutOfLimit() {
            var thresholdDetailsStartIndex = 380;
            var thresholdDetailsEndIndex = 389;

            final Grounds grounds = Grounds.builder()
                .thresholdDetails(testThresholdDetails)
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .grounds(grounds)
                .build();

            assertThatThrownBy(() -> underTest.removeCharactersFromThresholdDetails(caseData, MIGRATION_ID,
                thresholdDetailsStartIndex, thresholdDetailsEndIndex))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s},"
                        + " threshold details is shorter than provided index",
                    MIGRATION_ID, 1));
        }

        @Test
        void shouldThrowExceptionIfBlankText() {
            var thresholdDetailsStartIndex = 8;
            var thresholdDetailsEndIndex = 9;

            final Grounds grounds = Grounds.builder()
                .thresholdDetails("\nBETWEEN\n\n            ")
                .build();

            CaseData caseData = CaseData.builder()
                .id(1L)
                .grounds(grounds)
                .build();

            assertThatThrownBy(() -> underTest.removeCharactersFromThresholdDetails(caseData, MIGRATION_ID,
                thresholdDetailsStartIndex, thresholdDetailsEndIndex))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s}, "
                        + "threshold details does not contain provided text",
                    MIGRATION_ID, 1));
        }
    }

    @Nested
    class ClearHearingOption {
        @Test
        void shouldClearHearingOption() {
            HashMap<String, Object> data = new HashMap<>();
            data.put("hearingOption", "EDIT_HEARING");
            CaseDetails caseDetails = CaseDetails.builder().data(data).build();

            underTest.clearHearingOption(caseDetails);

            assertThat(caseDetails.getData()).extracting("hearingOption").isNull();
        }

        @Test
        void shouldDoNothingIfNoHearingOption() {
            HashMap<String, Object> data = new HashMap<>();
            CaseDetails caseDetails = CaseDetails.builder().data(data).build();

            assertThat(caseDetails.getData()).isEmpty();
        }
    }

    @Nested
    class MigrateCaseClosedDateToLatestFinalOrderApprovalDate {
        private static final LocalDateTime LATEST_APPROVAL_DATE_TIME = LocalDateTime.now();
        private static final LocalDate LATEST_APPROVAL_DATE = LATEST_APPROVAL_DATE_TIME.toLocalDate();
        private static final LocalDate ORIGINAL_CLOSE_CASE_DATE = LATEST_APPROVAL_DATE.minusYears(1);
        private static final CloseCase CLOSE_CASE_TAB_FIELD = CloseCase.builder()
            .date(ORIGINAL_CLOSE_CASE_DATE).build();

        @ParameterizedTest
        @EnumSource(value = State.class, names = {"OPEN", "SUBMITTED", "GATEKEEPING", "GATEKEEPING_LISTING",
            "CASE_MANAGEMENT", "DELETED", "RETURNED"})
        void shouldThrowExceptionIfCaseNotClosed(State state) {
            CaseData caseData = CaseData.builder().id(1L).state(state).build();

            assertThatThrownBy(() -> underTest.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData,
                MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s} Case is not closed yet",
                    MIGRATION_ID, 1));
        }

        @Test
        void shouldThrowExceptionIfOrderCollectionIsNull() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED).build();

            assertThatThrownBy(() -> underTest.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData,
                MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s} Order collection is null/empty",
                    MIGRATION_ID, 1));
        }

        @Test
        void shouldThrowExceptionIfOrderCollectionIsEmpty() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED).orderCollection(List.of()).build();

            assertThatThrownBy(() -> underTest.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData,
                MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s} Order collection is null/empty",
                    MIGRATION_ID, 1));
        }

        @Test
        void shouldThrowExceptionIfFinalOrderNotFound() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME)
                        .markedFinal(YesNo.NO.getValue())
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim Blank order (C21)")
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim Care order")
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim Discharge of care order")
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim Supervision order")
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim testing order")
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim testing order")
                        .markedFinal(YesNo.YES.getValue())
                        .build())
                    )
                ).build();

            assertThatThrownBy(() -> underTest.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData,
                MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s} No final order found",
                    MIGRATION_ID, 1));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Final Emergency protection order",
            "Interim Emergency protection order", "Final Blank order (C21)", "Final Care order",
            "Final Discharge of care order", "Final Supervision order", "Final testing order"})
        void shouldUpdateCloseDateIfOldVersionOfOrderFound(String orderType) {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .type(orderType)
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build()))
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Final Emergency protection order",
            "Interim Emergency protection order", "Final Blank order (C21)", "Final Care order",
            "Final Discharge of care order", "Final Supervision order", "Final testing order", "Just an order"})
        void shouldUpdateCloseDateIfNewVersionOfFinalOrderFound(String orderType) {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .type(orderType)
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(1))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build()))
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldUpdateCloseDateAsApprovalDateIfApprovalDateTimeIsNull() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(1))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(LATEST_APPROVAL_DATE)
                        .approvalDateTime(null)
                        .build()))
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldUpdateCloseDateAsApprovalDateTimeIfApprovalDateIsNull() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(1))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(null)
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build()))
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldUpdateCloseDateIfApprovalDateIsTheLatestDate() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(2))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(LATEST_APPROVAL_DATE)
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME.minusDays(1))
                        .build()))
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldUpdateCloseDateIfApprovalDateTimeIsTheLatestDate() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(2))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(LATEST_APPROVAL_DATE.minusDays(1))
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build()))
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldUpdateCloseDateAsLatestApprovalDateIfMultipleFinalOrderExist() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CLOSE_CASE_TAB_FIELD)
                .orderCollection(List.of(
                    // not final order
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.NO.getValue())
                        .approvalDate(LATEST_APPROVAL_DATE)
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Interim Care order")
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build()),

                    // no approval date
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(null)
                        .approvalDateTime(null)
                        .build()),

                    // approved final orders
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME.minusDays(1))
                        .build()),
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(LATEST_APPROVAL_DATE.minusDays(2))
                        .build()),
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME.minusDays(3))
                        .build()),
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(LATEST_APPROVAL_DATE.minusDays(4))
                        .build()),
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .build()),
                    element(GeneratedOrder.builder()
                        .type("Final Care order")
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME.minusDays(5))
                        .build())
                    )
                ).build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE.minusDays(1), ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldOnlyUpdateCloseDateAndKeepDeprecatedFieldUnchanged() {
            CloseCase closeCase = CLOSE_CASE_TAB_FIELD.toBuilder()
                .details("details")
                .fullReason("fullReason")
                .partialReason("partialReason")
                .showFullReason("showFullReason")
                .build();
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(closeCase)
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(1))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDate(null)
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build()))
                ).build();

            Map<String, Object> actual =
                underTest.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData, MIGRATION_ID);

            assertThat(actual)
                .isEqualTo(Map.of("closeCaseTabField", closeCase.toBuilder()
                    .date(LATEST_APPROVAL_DATE)
                    .dateBackup(ORIGINAL_CLOSE_CASE_DATE)
                    .build()));
        }

        @Test
        void shouldRollbackCloseCaseTabFieldMigration() {
            CaseData caseData = CaseData.builder()
                .id(1L).state(State.CLOSED)
                .closeCaseTabField(CloseCase.builder()
                    .date(LATEST_APPROVAL_DATE)
                    .dateBackup(ORIGINAL_CLOSE_CASE_DATE)
                    .build())
                .build();

            Map<String, Object> actual = underTest.rollbackCloseCaseTabFieldMigration(caseData, MIGRATION_ID);

            assertThat(actual).isEqualTo(Map.of("closeCaseTabField", CloseCase.builder()
                .date(ORIGINAL_CLOSE_CASE_DATE)
                .build()));
        }

        @Test
        void shouldNotUpdateBackupFieldIfNotEmpty() {
            CaseData caseData = CaseData.builder().id(1L).state(State.CLOSED)
                .closeCaseTabField(CloseCase.builder()
                    .date(LATEST_APPROVAL_DATE)
                    .dateBackup(ORIGINAL_CLOSE_CASE_DATE)
                    .build())
                .orderCollection(List.of(
                    element(GeneratedOrder.builder()
                        .dateTimeIssued(LATEST_APPROVAL_DATE_TIME.minusDays(10))
                        .markedFinal(YesNo.YES.getValue())
                        .approvalDateTime(LATEST_APPROVAL_DATE_TIME)
                        .build())))
                .build();

            assertCloseCaseDate(caseData, LATEST_APPROVAL_DATE, ORIGINAL_CLOSE_CASE_DATE);
        }

        @Test
        void shouldThrowExceptionIfCloseCaseTabFieldNotFound() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() -> underTest.rollbackCloseCaseTabFieldMigration(caseData,
                MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s} closeCaseField is null",
                    MIGRATION_ID, 1));
        }

        @Test
        void shouldClearCloseCaseTabBackupField() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .closeCaseTabField(CloseCase.builder()
                    .date(LATEST_APPROVAL_DATE)
                    .dateBackup(ORIGINAL_CLOSE_CASE_DATE)
                    .build())
                .build();

            Map<String, Object> actual = underTest.clearCloseCaseTabBackupField(caseData);

            assertThat(actual).isEqualTo(Map.of("closeCaseTabField", CloseCase.builder()
                .date(LATEST_APPROVAL_DATE).build()));
        }

        private void assertCloseCaseDate(CaseData caseData, LocalDate expectedCloseDate, LocalDate expectedBackupDate) {
            Map<String, Object> actual =
                underTest.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData, MIGRATION_ID);

            assertThat(actual)
                .isEqualTo(Map.of("closeCaseTabField", CloseCase.builder()
                    .date(expectedCloseDate)
                    .dateBackup(expectedBackupDate)
                    .build()));
        }

        @Test
        void shouldReplaceUnknownAllocatedJudgeOtherTitleAsJudge() {

            CaseData caseData = CaseData.builder()
                .allocatedJudge(Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .otherTitle("Unknown")
                    .judgeFullName("Random Title Here John Smith")
                    .build())
                .build();

            Judge expected = Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                .otherTitle("Judge")
                .judgeFullName("Random Title Here John Smith")
                .build();

            Map<String, Object> actual = underTest.migrateCaseRemoveUnknownAllocatedJudgeTitle(caseData, MIGRATION_ID);

            assertThat(actual).isEqualTo(Map.of("allocatedJudge", expected));
        }


        private static Stream<String> allJudgeTitlesStream() {
            return JudicialUserProfile.TITLES.stream();
        }

        @ParameterizedTest
        @MethodSource("allJudgeTitlesStream")
        void shouldReplaceUnknownAllocatedJudgeOtherTitleWithJudgeTitleEnum(String otherTitle) {

            CaseData caseData = CaseData.builder()
                .allocatedJudge(Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .otherTitle("Unknown")
                    .judgeFullName(otherTitle + " John Smith")
                    .build())
                .build();

            Map<String, Object> actual = underTest.migrateCaseRemoveUnknownAllocatedJudgeTitle(caseData, MIGRATION_ID);

            Judge expected = Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                .judgeFullName(otherTitle + " John Smith")
                .otherTitle(otherTitle)
                .build();

            assertThat(actual).isEqualTo(Map.of("allocatedJudge", expected));
        }


        @Test
        void shouldThrowExceptionWhenAllocationJudgeOtherTitleIsNotUknownAndSomethingElse() {
            CaseData caseData = CaseData.builder()
                .allocatedJudge(Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .otherTitle("Test")
                    .judgeFullName("Random Title Here John Smith")
                    .build())
                .build();

            assertThatThrownBy(() -> underTest.migrateCaseRemoveUnknownAllocatedJudgeTitle(caseData, MIGRATION_ID))
                .isInstanceOf(AssertionError.class)
                .hasMessage(format("Migration {id = %s, case reference = %s} otherTitle is %s",
                    MIGRATION_ID, caseData.getId(), caseData.getAllocatedJudge().getOtherTitle()));
        }
    }
}
