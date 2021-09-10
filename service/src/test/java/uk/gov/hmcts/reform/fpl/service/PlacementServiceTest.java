package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ZERO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.MAINTENANCE_AGREEMENT_AWARD;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    @Mock
    private Time time;

    @Mock
    private FeeService feeService;

    @Mock
    private PbaNumberService pbaNumberService;

    @Mock
    private DocumentSealingService sealingService;

    @InjectMocks
    private PlacementService underTest;

    @Nested
    class PrepareChildren {

        final Element<Child> child1 = testChild("Alex", "White");
        final Element<Child> child2 = testChild("Emma", "Brown");
        final Element<Child> child3 = testChild("George", "Green");

        @Test
        void shouldPrepareListOfChildrenWhenThereIsNoPlacement() {

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2))
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(dynamicList(child1, child2))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldPrepareListOfChildrenWithoutChildrenThatHavePlacement() {

            final Placement child2Placement = Placement.builder()
                .childId(child2.getId())
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .placementEventData(PlacementEventData.builder().placements(wrapElements(child2Placement)).build())
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(dynamicList(child1, child3))
                .placements(wrapElements(child2Placement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldPrepareChildPlacementWhenAllOtherChildrenHavePlacements() {

            final Placement child1Placement = Placement.builder()
                .childId(child1.getId())
                .build();

            final Placement child2Placement = Placement.builder()
                .childId(child2.getId())
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .placementEventData(PlacementEventData.builder()
                    .placements(wrapElements(child1Placement, child2Placement))
                    .build())
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(ONE)
                .placementChildName("George Green")
                .placement(Placement.builder()
                    .childId(child3.getId())
                    .childName("George Green")
                    .build())
                .placements(wrapElements(child1Placement, child2Placement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldPreparePlacementForOnlyChildInACase() {

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1))
                .build();

            final PlacementEventData actualPlacementData = underTest.prepareChildren(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(ONE)
                .placementChildName("Alex White")
                .placement(Placement.builder()
                    .childId(child1.getId())
                    .childName("Alex White")
                    .build())
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldSetZeroChildrenCardinalityWhenNoChildrenWithoutPlacement() {

            final Placement placement1 = Placement.builder()
                .childId(child1.getId())
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1))
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

        final Element<Child> child1 = testChild("Alex", "White");
        final Element<Child> child2 = testChild("Emma", "Brown");
        final Element<Child> child3 = testChild("George", "Green");

        @Test
        void shouldPreparePlacementForSelectedChild() {

            final DynamicList childrenList = dynamicList(1, child1, child2, child3);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildrenList(childrenList)
                .build();

            final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2, child3))
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.preparePlacement(caseData);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placementChildrenCardinality(MANY)
                .placementChildName("Emma Brown")
                .placement(Placement.builder()
                    .childName("Emma Brown")
                    .childId(child2.getId())
                    .build())
                .placementChildrenList(childrenList)
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldThrowsExceptionWhenChildNotSelected() {

            final DynamicList childrenList = dynamicList(child1, child2, child3);

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

        @Test
        void shouldSealApplicationAndAddNewPlacementToListOfExistingPlacements() {

            final DocumentReference application = testDocumentReference();
            final DocumentReference sealedApplication = testDocumentReference();

            final Placement existingPlacement = Placement.builder()
                .application(testDocumentReference())
                .build();

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(existingPlacement))
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            when(sealingService.sealDocument(application)).thenReturn(sealedApplication);

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .build();

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placement(expectedPlacement)
                .placements(wrapElements(existingPlacement, currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
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

    }

    @SafeVarargs
    private DynamicList dynamicList(Element<Child>... children) {

        final List<Pair<UUID, String>> pairs = Stream.of(children)
            .map(child -> Pair.of(child.getId(), format("%s %s",
                child.getValue().getParty().getFirstName(), child.getValue().getParty().getLastName())))
            .collect(Collectors.toList());

        return buildDynamicList(pairs);
    }

    @SafeVarargs
    private DynamicList dynamicList(int selected, Element<Child>... children) {
        final DynamicList dynamicList = dynamicList(children);
        dynamicList.setValue(dynamicList.getListItems().get(selected));
        return dynamicList;
    }

}
