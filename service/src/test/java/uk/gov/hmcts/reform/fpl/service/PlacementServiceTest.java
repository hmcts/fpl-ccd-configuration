package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ZERO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.MAINTENANCE_AGREEMENT_AWARD;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.PARENTS_CONSENT_FOR_ADOPTION;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondent;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class PlacementServiceTest {

    private final Element<Respondent> respondent1 = testRespondent("John", "Smith", "father");
    private final Element<Respondent> respondent2 = testRespondent("Eva", "Smith", "mother");

    private final Element<Child> child1 = testChild("Alex", "White");
    private final Element<Child> child2 = testChild("Emma", "Brown");
    private final Element<Child> child3 = testChild("George", "Green");

    @Mock
    private Time time;

    @Mock
    private FeeService feeService;

    @Mock
    private PbaNumberService pbaNumberService;

    @Mock
    private DocumentSealingService sealingService;

    @Mock
    private HearingVenueLookUpService hearingVenueLookUpService;

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @Mock
    private RespondentService respondentService;

    @Mock
    private CourtService courtService;

    @InjectMocks
    private PlacementService underTest;

    @Nested
    class PrepareChildren {

        final DynamicList respondentsList = respondentsDynamicList(respondent1, respondent2);

        final PlacementConfidentialDocument defaultAnnexB = PlacementConfidentialDocument.builder()
            .type(ANNEX_B)
            .build();

        final PlacementSupportingDocument defaultBirthCertificate = PlacementSupportingDocument.builder()
            .type(BIRTH_ADOPTION_CERTIFICATE)
            .build();

        final PlacementSupportingDocument defaultStatementOfFacts = PlacementSupportingDocument.builder()
            .type(STATEMENT_OF_FACTS)
            .build();

        @Test
        void shouldPrepareListOfChildren() {

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2))
                .respondents1(List.of(respondent1, respondent2))
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenDynamicList(child1, child2))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldPreparePlacementForOnlyChildInACase() {

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1))
                .respondents1(List.of(respondent1, respondent2))
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(ONE)
                .placementChildName("Alex White")
                .placement(Placement.builder()
                    .childId(child1.getId())
                    .childName("Alex White")
                    .confidentialDocuments(wrapElements(defaultAnnexB))
                    .supportingDocuments(wrapElements(defaultBirthCertificate, defaultStatementOfFacts))
                    .build())
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldPrepareExistingPlacementForOnlyChildInACase() {

            final PlacementConfidentialDocument annexB = PlacementConfidentialDocument.builder()
                .document(testDocumentReference())
                .type(ANNEX_B)
                .build();

            final PlacementSupportingDocument birthCertificate = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .build();

            final PlacementSupportingDocument statementOfFacts = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(STATEMENT_OF_FACTS)
                .build();

            final PlacementNoticeDocument localAuthorityNotice = PlacementNoticeDocument.builder()
                .type(LOCAL_AUTHORITY)
                .response(testDocumentReference())
                .responseDescription("Local authority response description")
                .build();

            final PlacementNoticeDocument cafcassNotice = PlacementNoticeDocument.builder()
                .type(CAFCASS)
                .response(testDocumentReference())
                .responseDescription("Cafcass response description")
                .build();

            final PlacementNoticeDocument firstParentNotice = PlacementNoticeDocument.builder()
                .type(PARENT_FIRST)
                .recipientName("Recipient 1")
                .respondentId(UUID.randomUUID())
                .response(testDocumentReference())
                .responseDescription("First parent response description")
                .build();

            final PlacementNoticeDocument secondParentNotice = PlacementNoticeDocument.builder()
                .type(PARENT_SECOND)
                .recipientName("Recipient 2")
                .respondentId(UUID.randomUUID())
                .response(testDocumentReference())
                .responseDescription("Second parent response description")
                .build();

            final Placement existingPlacement = Placement.builder()
                .application(testDocumentReference())
                .childId(child1.getId())
                .childName("Alex White")
                .placementUploadDateTime(LocalDateTime.now())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .noticeDocuments(wrapElements(localAuthorityNotice, cafcassNotice,
                    firstParentNotice, secondParentNotice))
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1))
                .respondents1(List.of(respondent1, respondent2))
                .placementEventData(PlacementEventData.builder()
                    .placements(wrapElements(existingPlacement))
                    .build())
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(ONE)
                .placementChildName("Alex White")
                .placement(existingPlacement)
                .placements(wrapElements(existingPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldSetZeroChildrenCardinalityWhenNoChildrenWithoutPlacement() {

            final Placement placement1 = Placement.builder()
                .childId(child1.getId())
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(emptyList())
                .placementEventData(PlacementEventData.builder()
                    .placements(wrapElements(placement1))
                    .build())
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(ZERO)
                .placements(wrapElements(placement1))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

    }

    @Nested
    class PreparePlacement {

        final LocalDate today = LocalDate.of(2020, 1, 10);
        final byte[] document = readBytes("documents/document1.pdf");
        final DocmosisDocument docmosisDocument = testDocmosisDocument(document)
            .toBuilder().documentTitle("pdf.pdf").build();

        @Test
        void shouldGenerateA92Document() {
            final LocalDateTime now = today.atTime(23, 10);

            when(time.now()).thenReturn(now);
            when(hearingVenueLookUpService.getHearingVenue(any(String.class))).thenReturn(HearingVenue.builder()
                .venue("name").build());
            when(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any(), any()))
                .thenReturn(docmosisDocument);
            when(uploadDocumentService.uploadDocument(any(), any(), any())).thenReturn(testDocument());

            final DynamicList childrenList = childrenDynamicList(1, child1, child2, child3);

            final Element<Placement> placement = element(Placement.builder()
                .childId(child2.getId())
                .childName(child2.getValue().getParty().getFullName())
                .placementRespondentsToNotify(newArrayList(respondent1, respondent2))
                .build());

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenList)
                .placement(placement.getValue())
                .placements(newArrayList(placement))
                .placementNoticeVenue("82")
                .placementNoticeDateTime(time.now())
                .placementNoticeDuration("1")
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .court(Court.builder().name("Test court").build())
                .familyManCaseNumber("familymanid")
                .respondents1(List.of(respondent1, respondent2))
                .placementEventData(placementEventData)
                .placementList(
                    asDynamicList(placementEventData.getPlacements(), placement.getId(), Placement::getChildName))
                .build();

            when(courtService.getCourtSeal(caseData, SEALED))
                    .thenReturn(COURT_SEAL.getValue(caseData.getImageLanguage()));

            final PlacementEventData actualPlacementData = underTest.generateDraftA92(caseData);

            assertThat(actualPlacementData.getPlacementNotice()).isNotNull();
        }

        @Test
        void shouldPreparePlacementFromExisting() {
            final DynamicList childrenList = childrenDynamicList(1, child1, child2, child3);

            final Element<Placement> placement = element(Placement.builder()
                .childId(child2.getId())
                .childName(child2.getValue().getParty().getFullName())
                .placementRespondentsToNotify(newArrayList(respondent1, respondent2))
                .build());

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenList)
                .placements(newArrayList(placement))
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .respondents1(List.of(respondent1, respondent2))
                .placementEventData(placementEventData)
                .placementList(
                    asDynamicList(placementEventData.getPlacements(), placement.getId(), Placement::getChildName))
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePlacement(caseData);

            assertThat(actualPlacementData.getPlacement()).isNotNull();
            assertThat(actualPlacementData.getPlacement().getChildId()).isEqualTo(child2.getId());
        }

        @Test
        void shouldPreparePlacementForSelectedChild() {

            final DynamicList respondentsList = respondentsDynamicList(respondent1, respondent2);

            final DynamicList childrenList = childrenDynamicList(1, child1, child2, child3);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenList)
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .respondents1(List.of(respondent1, respondent2))
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePlacement(caseData);

            final PlacementConfidentialDocument defaultAnnexB = PlacementConfidentialDocument.builder()
                .type(ANNEX_B)
                .build();

            final PlacementSupportingDocument defaultBirthCertificate = PlacementSupportingDocument.builder()
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .build();

            final PlacementSupportingDocument defaultStatementOfFacts = PlacementSupportingDocument.builder()
                .type(STATEMENT_OF_FACTS)
                .build();

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildName("Emma Brown")
                .placement(Placement.builder()
                    .childName("Emma Brown")
                    .childId(child2.getId())
                    .confidentialDocuments(wrapElements(defaultAnnexB))
                    .supportingDocuments(wrapElements(defaultBirthCertificate, defaultStatementOfFacts))
                    .build())
                .placementChildrenList(childrenList)
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }


        @Test
        void shouldPrepareExistingPlacementForSelectedChild() {

            final PlacementConfidentialDocument annexB = PlacementConfidentialDocument.builder()
                .document(testDocumentReference())
                .type(ANNEX_B)
                .build();

            final PlacementSupportingDocument birthCertificate = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .build();

            final PlacementSupportingDocument statementOfFacts = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(STATEMENT_OF_FACTS)
                .build();

            final PlacementNoticeDocument localAuthorityNotice = PlacementNoticeDocument.builder()
                .type(LOCAL_AUTHORITY)
                .response(testDocumentReference())
                .responseDescription("Local authority response description")
                .build();

            final PlacementNoticeDocument secondParentNotice = PlacementNoticeDocument.builder()
                .type(PARENT_SECOND)
                .recipientName("Recipient 2")
                .respondentId(respondent2.getId())
                .build();

            final Placement existingPlacement = Placement.builder()
                .application(testDocumentReference())
                .childId(child2.getId())
                .childName("Emma Brown")
                .placementUploadDateTime(LocalDateTime.now())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .noticeDocuments(wrapElements(localAuthorityNotice, secondParentNotice))
                .build();

            final DynamicList childrenList = childrenDynamicList(1, child1, child2, child3);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenList)
                .placements(wrapElements(existingPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .respondents1(List.of(respondent1, respondent2))
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePlacement(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildName("Emma Brown")
                .placement(existingPlacement)
                .placementChildrenList(childrenList)
                .placements(wrapElements(existingPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldThrowsExceptionWhenChildNotSelected() {

            final DynamicList childrenList = childrenDynamicList(child1, child2, child3);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenList)
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .placementEventData(placementEventData)
                .build();

            assertThatThrownBy(() -> underTest.preparePlacement(caseData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Child for placement application not selected");
        }
    }

    @Nested
    class CheckDocuments {

        @Test
        void shouldReturnErrorsAboutAllMissingDocuments() {

            final Placement placement = Placement.builder().build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(placement)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final List<String> actualErrors = underTest.checkDocuments(caseData);

            assertThat(actualErrors).containsExactly(
                "Add required placement application",
                "Add required Birth/Adoption Certificate supporting document",
                "Add required Statement of facts supporting document",
                "Add required Annex B confidential document");
        }

        @Test
        void shouldReturnErrorsAboutSomeOfMissingDocuments() {

            final PlacementSupportingDocument supportingDocument1 = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(MAINTENANCE_AGREEMENT_AWARD)
                .build();

            final PlacementSupportingDocument supportingDocument2 = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(STATEMENT_OF_FACTS)
                .build();

            final PlacementConfidentialDocument confidentialDocument = PlacementConfidentialDocument.builder()
                .document(testDocumentReference())
                .type(ANNEX_B)
                .build();

            final Placement placement = Placement.builder()
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(supportingDocument1, supportingDocument2))
                .confidentialDocuments(wrapElements(confidentialDocument))
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(placement)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final List<String> actualErrors = underTest.checkDocuments(caseData);

            assertThat(actualErrors).containsExactly("Add required Birth/Adoption Certificate supporting document");
        }

        @Test
        void shouldReturnEmptyErrorListWhenAllRequiredDocumentsArePresent() {

            final PlacementSupportingDocument supportingDocument1 = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .build();

            final PlacementSupportingDocument supportingDocument2 = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(STATEMENT_OF_FACTS)
                .build();

            final PlacementConfidentialDocument confidentialDocument = PlacementConfidentialDocument.builder()
                .document(testDocumentReference())
                .type(ANNEX_B)
                .build();

            final Placement placement = Placement.builder()
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(supportingDocument1, supportingDocument2))
                .confidentialDocuments(wrapElements(confidentialDocument))
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(placement)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final List<String> actualErrors = underTest.checkDocuments(caseData);

            assertThat(actualErrors).isEmpty();
        }

    }

    @Nested
    class PreparePayment {

        final LocalDate today = LocalDate.of(2020, 1, 10);

        final FeesData feesData = FeesData.builder()
            .totalAmount(BigDecimal.valueOf(400.5))
            .build();

        @Test
        void shouldNotFetchPlacementFeeWhenPreviousPlacementPaymentWasTakenOnTheSameDay() {

            final LocalDateTime now = today.atTime(23, 10);
            final LocalDateTime lastPayment = today.atTime(7, 5);

            when(time.now()).thenReturn(now);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementLastPaymentTime(lastPayment)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePayment(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementLastPaymentTime(lastPayment)
                .placementPaymentRequired(NO)
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);

            verifyNoInteractions(feeService);
        }

        @Test
        void shouldFetchPlacementFeeWhenPreviousPlacementPaymentWasTakenOnDifferentDay() {

            final LocalDateTime now = today.atTime(7, 10);
            final LocalDateTime lastPayment = today.minusDays(1).atTime(23, 50);

            when(time.now()).thenReturn(now);
            when(feeService.getFeesDataForPlacement()).thenReturn(feesData);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementLastPaymentTime(lastPayment)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePayment(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementLastPaymentTime(lastPayment)
                .placementPaymentRequired(YES)
                .placementFee("40050")
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);

            verify(feeService).getFeesDataForPlacement();
        }

        @Test
        void shouldFetchPlacementFeeWhenNoPreviousPlacementPayment() {

            when(feeService.getFeesDataForPlacement()).thenReturn(feesData);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePayment(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementPaymentRequired(YES)
                .placementFee("40050")
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);

            verify(feeService).getFeesDataForPlacement();
            verifyNoInteractions(time);
        }

        @Test
        void shouldReturnAllChildPlacementApplications() {
            UUID placementId = randomUUID();
            Element<Child> childForPlacement = testChild();
            CaseData caseData = CaseData.builder()
                .children1(List.of(childForPlacement))
                .placementEventData(
                    PlacementEventData.builder().placements(
                        List.of(
                            element(placementId, Placement.builder().childId(childForPlacement.getId()).build())
                        )
                    ).build()
                ).build();

            List<Element<String>> childPlacementOrders = underTest.getPlacements(caseData);

            assertThat(childPlacementOrders).contains(
                element(placementId, childForPlacement.getValue().getParty().getFullName())
            );
        }

        @Test
        void shouldReturnPlacementById() {
            UUID placementId = randomUUID();
            Placement chosenPlacement = Placement.builder().childName("Jonas Watson").build();
            CaseData caseData = CaseData.builder()
                .placementEventData(PlacementEventData.builder().placements(List.of(
                    element(placementId, chosenPlacement),
                    element(Placement.builder().childName("Brian Watson").build()),
                    element(Placement.builder().childName("Nancy Watson").build())
                    )).build()
                ).build();

            Placement placement = underTest.getPlacementById(caseData, placementId);

            assertThat(placement).isEqualTo(chosenPlacement);
        }

        @Test
        void shouldReturnChildByPlacementById() {
            List<Element<Child>> children = testChildren();
            Element<Child> selectedChild = children.get(0);
            Element<Placement> placement = element(Placement.builder().childId(selectedChild.getId()).build());
            CaseData caseData = CaseData.builder()
                .children1(children)
                .placementEventData(PlacementEventData.builder().placements(List.of(placement)).build())
                .build();

            Element<Child> actualChild = underTest.getChildByPlacementId(caseData, placement.getId());

            assertThat(actualChild).isEqualTo(selectedChild);
        }
    }

    @Nested
    class CheckPayment {

        final String testPBANumber = "1234567";
        final String normalisedTestPBANumber = "PBA" + testPBANumber;

        final PBAPayment payment = PBAPayment.builder()
            .pbaNumber(testPBANumber)
            .build();

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placementPayment(payment)
                .build())
            .build();

        @BeforeEach
        void init() {
            when(pbaNumberService.update(testPBANumber)).thenReturn(normalisedTestPBANumber);
        }

        @Test
        void shouldNormaliseAndValidateCorrectPBANumber() {

            when(pbaNumberService.validate(normalisedTestPBANumber)).thenReturn(emptyList());

            final List<String> actualErrors = underTest.checkPayment(caseData);

            assertThat(actualErrors).isEmpty();
            assertThat(payment.getPbaNumber()).isEqualTo(normalisedTestPBANumber);
        }

        @Test
        void shouldNormaliseAndValidateIncorrectPBANumber() {

            when(pbaNumberService.validate(normalisedTestPBANumber)).thenReturn(List.of("Invalid PBA"));

            final List<String> actualErrors = underTest.checkPayment(caseData);

            assertThat(actualErrors).containsExactly("Invalid PBA");
            assertThat(payment.getPbaNumber()).isEqualTo(normalisedTestPBANumber);
        }
    }

    @Nested
    class SavePlacement {

        private final DocumentReference application = testDocumentReference();
        private final DocumentReference sealedApplication = testDocumentReference();
        private final Court court = Court.builder().build();

        @BeforeEach
        void init() {
            when(sealingService.sealDocument(application, court, SealType.ENGLISH)).thenReturn(sealedApplication);
        }

        @Test
        void shouldSaveApplicationAndAddNewPlacementToListOfExistingPlacements() {

            final Placement existingPlacement = Placement.builder()
                .childId(child1.getId())
                .application(testDocumentReference())
                .build();

            final Placement currentPlacement = Placement.builder()
                .childId(child2.getId())
                .application(application)
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(existingPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(Court.builder().build())
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(application)
                .build();

            assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
            assertThat(unwrapElements(actualPlacementData.getPlacements()))
                .containsAll(List.of(existingPlacement, currentPlacement));
            assertThat(actualPlacementData.getPlacementIdToBeSealed())
                .isEqualTo(actualPlacementData.getPlacements().stream()
                    .filter(elm -> application.getBinaryUrl().equals(elm.getValue().getApplication().getBinaryUrl()))
                    .map(Element::getId)
                    .findFirst().orElse(null));
        }

        @Test
        void shouldReplaceExistingPlacementWithUpdatedVersion() {

            final PlacementConfidentialDocument annexB = PlacementConfidentialDocument.builder()
                .document(testDocumentReference())
                .type(ANNEX_B)
                .build();

            final PlacementSupportingDocument birthCertificate = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .build();

            final PlacementSupportingDocument statementOfFacts = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(STATEMENT_OF_FACTS)
                .build();

            final PlacementSupportingDocument parentsConsentForAdoption = PlacementSupportingDocument.builder()
                .document(testDocumentReference())
                .type(PARENTS_CONSENT_FOR_ADOPTION)
                .build();

            final PlacementNoticeDocument localAuthorityNotice = PlacementNoticeDocument.builder()
                .type(LOCAL_AUTHORITY)
                .response(testDocumentReference())
                .responseDescription("Local authority response description")
                .build();

            final Placement existingPlacement = Placement.builder()
                .application(testDocumentReference())
                .childId(child1.getId())
                .childName("Alex White")
                .placementUploadDateTime(LocalDateTime.now())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .noticeDocuments(wrapElements(localAuthorityNotice))
                .build();

            final Placement currentPlacement = existingPlacement.toBuilder()
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts, parentsConsentForAdoption))
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(existingPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            assertThat(actualPlacementData.getPlacement()).isEqualTo(currentPlacement);
            assertThat(unwrapElements(actualPlacementData.getPlacements())).containsAll(List.of(currentPlacement));
            assertThat(actualPlacementData.getPlacementIdToBeSealed()).isNull();

            verifyNoInteractions(sealingService, time);
        }

        @Test
        void shouldAddLocalAuthorityPlacementNoticeResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .noticeDocuments(emptyList())
                .build();

            final DocumentReference laResponseDocument = testDocumentReference();

            final PlacementNoticeDocument localAuthorityResponse = PlacementNoticeDocument.builder()
                .response(laResponseDocument)
                .responseDescription("Local authority response description")
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(court)
                .placementEventData(placementEventData)
                .placementNoticeResponses(wrapElements(localAuthorityResponse))
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacementNoticeResponses(
                caseData, LOCAL_AUTHORITY);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(application)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(LOCAL_AUTHORITY)
                    .response(laResponseDocument)
                    .responseDescription("Local authority response description")
                    .build()))
                .build();

            assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
            assertThat(unwrapElements(actualPlacementData.getPlacements())).containsAll(List.of(currentPlacement));
        }

        @Test
        void shouldAddCafcassPlacementNoticeResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference cafcassNoticeDocument = testDocumentReference();
            final PlacementNoticeDocument cafcassNoticeResponse = PlacementNoticeDocument.builder()
                .type(CAFCASS)
                .response(cafcassNoticeDocument)
                .responseDescription("Cafcass response description")
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(court)
                .placementEventData(placementEventData)
                .placementNoticeResponses(wrapElements(cafcassNoticeResponse))
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacementNoticeResponsesAdmin(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(application)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(CAFCASS)
                    .response(cafcassNoticeDocument)
                    .responseDescription("Cafcass response description")
                    .build()))
                .build();

            assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
            assertThat(unwrapElements(actualPlacementData.getPlacements())).containsAll(List.of(currentPlacement));
        }

        @Test
        void shouldAddFirstParentPlacementNoticeWithResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference respondentResponseDocument = testDocumentReference();
            final PlacementNoticeDocument respondentResponse = PlacementNoticeDocument.builder()
                .response(respondentResponseDocument)
                .responseDescription("Respondent notice description")
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(court)
                .placementEventData(placementEventData)
                .placementNoticeResponses(wrapElements(respondentResponse))
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacementNoticeResponses(caseData, RESPONDENT);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(application)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(RESPONDENT)
                    .response(respondentResponseDocument)
                    .responseDescription("Respondent notice description")
                    .build()))
                .build();

            assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
            assertThat(unwrapElements(actualPlacementData.getPlacements())).containsAll(List.of(currentPlacement));
        }

        @Test
        void shouldAddMultipleNoticeOfPlacementResponses() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference localAuthorityNoticeResponse = testDocumentReference();
            final DocumentReference cafcassNoticeResponse = testDocumentReference();
            final DocumentReference firstParentNoticeResponse = testDocumentReference();

            final PlacementNoticeDocument laResponse = PlacementNoticeDocument.builder()
                .type(LOCAL_AUTHORITY)
                .response(localAuthorityNoticeResponse)
                .responseDescription("LA response description")
                .build();
            final PlacementNoticeDocument cafcassResponse = PlacementNoticeDocument.builder()
                .type(CAFCASS)
                .response(cafcassNoticeResponse)
                .responseDescription("Cafcass response description")
                .build();
            final PlacementNoticeDocument firstParentResponse = PlacementNoticeDocument.builder()
                .type(RESPONDENT)
                .response(firstParentNoticeResponse)
                .responseDescription("First parent response description")
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(court)
                .placementEventData(placementEventData)
                .placementNoticeResponses(wrapElements(laResponse, cafcassResponse, firstParentResponse))
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacementNoticeResponsesAdmin(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(application)
                .noticeDocuments(wrapElements(
                    PlacementNoticeDocument.builder()
                        .type(LOCAL_AUTHORITY)
                        .response(localAuthorityNoticeResponse)
                        .responseDescription("LA response description")
                        .build(),
                    PlacementNoticeDocument.builder()
                        .type(CAFCASS)
                        .response(cafcassNoticeResponse)
                        .responseDescription("Cafcass response description")
                        .build(),
                    PlacementNoticeDocument.builder()
                        .type(RESPONDENT)
                        .response(firstParentNoticeResponse)
                        .responseDescription("First parent response description")
                        .build()))
                .build();

            assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
            assertThat(unwrapElements(actualPlacementData.getPlacements())).containsAll(List.of(currentPlacement));
        }

        @Test
        void shouldThrowsExceptionWhenNoPlacementApplicationDocument() {

            final Placement currentPlacement = Placement.builder()
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            assertThatThrownBy(() -> underTest.savePlacement(caseData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Missing placement application document");
        }

        @Test
        void shouldSealPlacementApplication() {
            final UUID uuid = randomUUID();
            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            CaseData caseData = CaseData.builder()
                .court(court)
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(element(uuid, currentPlacement)))
                    .placementIdToBeSealed(uuid)
                    .build())
                .build();

            PlacementEventData sealedResult = underTest.sealPlacementApplicationAfterEventSubmitted(caseData);

            final Placement expectedPlacement = Placement.builder().application(sealedApplication).build();

            assertThat(sealedResult.getPlacements()).isEqualTo(List.of(element(uuid, expectedPlacement)));
        }
    }

    @Nested
    class Events {

        @Test
        void shouldReturnPlacementApplicationAddedEventWhenPlacementAdded() {

            final Placement currentPlacement = Placement.builder()
                .childId(child1.getId())
                .application(testDocumentReference())
                .build();

            final Placement existingPlacement1 = Placement.builder()
                .childId(child2.getId())
                .application(testDocumentReference())
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(existingPlacement1, currentPlacement))
                .build();

            final PlacementEventData placementEventDataBefore = PlacementEventData.builder()
                .placements(wrapElements(existingPlacement1))
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final CaseData caseDataBefore = CaseData.builder()
                .placementEventData(placementEventDataBefore)
                .build();

            final List<Object> events = underTest.getEvents(caseData, caseDataBefore);

            assertThat(events).containsExactly(new PlacementApplicationSubmitted(caseData, currentPlacement));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnPlacementApplicationAddedEventWhenFirstPlacementAdded(List<Element<Placement>> placements) {

            final Placement currentPlacement = Placement.builder()
                .childId(child1.getId())
                .application(testDocumentReference())
                .build();


            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            final PlacementEventData placementEventDataBefore = PlacementEventData.builder()
                .placements(placements)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final CaseData caseDataBefore = CaseData.builder()
                .placementEventData(placementEventDataBefore)
                .build();

            final List<Object> events = underTest.getEvents(caseData, caseDataBefore);

            assertThat(events).containsExactly(new PlacementApplicationSubmitted(caseData, currentPlacement));
        }

        @Test
        void shouldReturnPlacementApplicationEditedEvent() {

            final Placement existingPlacement1 = Placement.builder()
                .childId(child1.getId())
                .application(testDocumentReference())
                .build();

            final Placement currentPlacement = existingPlacement1.toBuilder()
                .childId(child1.getId())
                .application(testDocumentReference())
                .confidentialDocuments(wrapElements(PlacementConfidentialDocument.builder()
                    .document(testDocumentReference())
                    .build()))
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            final PlacementEventData placementEventDataBefore = PlacementEventData.builder()
                .placements(wrapElements(existingPlacement1))
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final CaseData caseDataBefore = CaseData.builder()
                .placementEventData(placementEventDataBefore)
                .build();

            final List<Object> events = underTest.getEvents(caseData, caseDataBefore);

            assertThat(events).containsExactly(new PlacementApplicationChanged(caseData, currentPlacement));
        }
    }

    @Nested
    class GetUpdatedPlacement {

        @Test
        void shouldGetUpdatedPlacementWhenTotallyNew() {
            Element<Placement> newPlacement = element(UUID.randomUUID(), Placement.builder()
                .childId(UUID.randomUUID()).build());
            CaseData caseDataBefore = CaseData.builder().build();
            CaseData caseData = caseDataBefore.toBuilder()
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(newPlacement))
                    .build())
                .build();

            Element<Placement> updated = underTest.getUpdatedPlacement(caseData, caseDataBefore);

            assertThat(updated).isEqualTo(newPlacement);
        }

        @Test
        void shouldGetUpdatedPlacementWhenUpdatingApplication() {
            UUID placementId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();
            Element<Placement> oldPlacement = element(placementId, Placement.builder()
                .childId(childId)
                .application(testDocumentReference())
                .build());
            Element<Placement> newPlacement = element(placementId, Placement.builder()
                .childId(childId)
                .application(testDocumentReference()) // new doc ref = replaced document
                .build());

            CaseData caseDataBefore = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(oldPlacement))
                    .build())
                .build();
            CaseData caseData = caseDataBefore.toBuilder()
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(newPlacement))
                    .build())
                .build();

            Element<Placement> updated = underTest.getUpdatedPlacement(caseData, caseDataBefore);

            assertThat(updated).isEqualTo(newPlacement);
        }

        @Test
        void shouldGetUpdatedPlacementWhenAddingPlacementNotice() {
            UUID placementId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();
            Placement oldPlacement = Placement.builder()
                .childId(childId)
                .application(testDocumentReference())
                .build();

            Placement newPlacement = oldPlacement.toBuilder()
                .placementNotice(testDocumentReference())
                .build();

            CaseData caseDataBefore = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(element(placementId, oldPlacement)))
                    .build())
                .build();

            CaseData caseData = caseDataBefore.toBuilder()
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(element(placementId, newPlacement)))
                    .build())
                .build();

            Element<Placement> updated = underTest.getUpdatedPlacement(caseData, caseDataBefore);

            assertThat(updated).isEqualTo(element(placementId, newPlacement));
        }

    }

    @SafeVarargs
    private DynamicList childrenDynamicList(Element<Child>... children) {

        final List<Pair<UUID, String>> pairs = Stream.of(children)
            .map(child -> Pair.of(child.getId(), format("%s %s",
                child.getValue().getParty().getFirstName(), child.getValue().getParty().getLastName())))
            .collect(toList());

        return buildDynamicList(pairs);
    }

    @SafeVarargs
    private DynamicList childrenDynamicList(int selected, Element<Child>... children) {
        final DynamicList dynamicList = childrenDynamicList(children);
        dynamicList.setValue(dynamicList.getListItems().get(selected));
        return dynamicList;
    }

    @SafeVarargs
    private DynamicList respondentsDynamicList(Element<Respondent>... respondents) {

        final List<Pair<UUID, String>> pairs = Stream.of(respondents)
            .map(respondent -> Pair.of(respondent.getId(), format("%s %s - %s",
                respondent.getValue().getParty().getFirstName(),
                respondent.getValue().getParty().getLastName(),
                respondent.getValue().getParty().getRelationshipToChild()
            )))
            .collect(toList());

        return buildDynamicList(pairs);
    }

    @SafeVarargs
    private DynamicList respondentsDynamicList(int selected, Element<Respondent>... respondents) {
        final DynamicList dynamicList = respondentsDynamicList(respondents);
        dynamicList.setValue(dynamicList.getListItems().get(selected));
        return dynamicList;
    }

}
