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
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationAdded;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEdited;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ZERO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.MAINTENANCE_AGREEMENT_AWARD;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.PARENTS_CONSENT_FOR_ADOPTION;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
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
                .placementNoticeForLocalAuthorityRequired(NO)
                .placementNoticeResponseFromLocalAuthorityReceived(NO)
                .placementNoticeForCafcassRequired(NO)
                .placementNoticeResponseFromCafcassReceived(NO)
                .placementNoticeForFirstParentRequired(NO)
                .placementNoticeResponseFromFirstParentReceived(NO)
                .placementNoticeForSecondParentRequired(NO)
                .placementNoticeResponseFromSecondParentReceived(NO)
                .placementNoticeForFirstParentParentsList(respondentsList)
                .placementNoticeForSecondParentParentsList(respondentsList)
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
                .notice(testDocumentReference())
                .noticeDescription("Local authority description")
                .response(testDocumentReference())
                .responseDescription("Local authority response description")
                .build();

            final PlacementNoticeDocument cafcassNotice = PlacementNoticeDocument.builder()
                .type(CAFCASS)
                .notice(testDocumentReference())
                .noticeDescription("Cafcass description")
                .response(testDocumentReference())
                .noticeDescription("Cafcass response description")
                .build();

            final PlacementNoticeDocument firstParentNotice = PlacementNoticeDocument.builder()
                .type(PARENT_FIRST)
                .recipientName("Recipient 1")
                .respondentId(UUID.randomUUID())
                .notice(testDocumentReference())
                .noticeDescription("First parent description")
                .response(testDocumentReference())
                .noticeDescription("First parent response description")
                .build();

            final PlacementNoticeDocument secondParentNotice = PlacementNoticeDocument.builder()
                .type(PARENT_SECOND)
                .recipientName("Recipient 2")
                .respondentId(UUID.randomUUID())
                .notice(testDocumentReference())
                .noticeDescription("Second parent description")
                .response(testDocumentReference())
                .noticeDescription("Second parent response description")
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
                .placementNoticeForLocalAuthorityRequired(YES)
                .placementNoticeForLocalAuthority(localAuthorityNotice.getNotice())
                .placementNoticeForLocalAuthorityDescription(localAuthorityNotice.getNoticeDescription())
                .placementNoticeResponseFromLocalAuthorityReceived(YES)
                .placementNoticeResponseFromLocalAuthority(localAuthorityNotice.getResponse())
                .placementNoticeResponseFromLocalAuthorityDescription(localAuthorityNotice.getResponseDescription())
                .placementNoticeForCafcassRequired(YES)
                .placementNoticeForCafcass(cafcassNotice.getNotice())
                .placementNoticeForCafcassDescription(cafcassNotice.getNoticeDescription())
                .placementNoticeResponseFromCafcassReceived(YES)
                .placementNoticeResponseFromCafcass(cafcassNotice.getResponse())
                .placementNoticeResponseFromCafcassDescription(cafcassNotice.getResponseDescription())
                .placementNoticeForFirstParentRequired(YES)
                .placementNoticeForFirstParent(firstParentNotice.getNotice())
                .placementNoticeForFirstParentDescription(firstParentNotice.getNoticeDescription())
                .placementNoticeResponseFromFirstParentReceived(YES)
                .placementNoticeResponseFromFirstParent(firstParentNotice.getResponse())
                .placementNoticeResponseFromFirstParentDescription(firstParentNotice.getResponseDescription())
                .placementNoticeForSecondParentRequired(YES)
                .placementNoticeForSecondParent(secondParentNotice.getNotice())
                .placementNoticeForSecondParentDescription(secondParentNotice.getNoticeDescription())
                .placementNoticeResponseFromSecondParentReceived(YES)
                .placementNoticeResponseFromSecondParent(secondParentNotice.getResponse())
                .placementNoticeResponseFromSecondParentDescription(secondParentNotice.getResponseDescription())
                .placementNoticeForFirstParentParentsList(respondentsList)
                .placementNoticeForSecondParentParentsList(respondentsList)
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
                .placementNoticeForLocalAuthorityRequired(NO)
                .placementNoticeResponseFromLocalAuthorityReceived(NO)
                .placementNoticeForCafcassRequired(NO)
                .placementNoticeResponseFromCafcassReceived(NO)
                .placementNoticeForFirstParentRequired(NO)
                .placementNoticeResponseFromFirstParentReceived(NO)
                .placementNoticeForSecondParentRequired(NO)
                .placementNoticeResponseFromSecondParentReceived(NO)
                .placementNoticeForFirstParentParentsList(respondentsList)
                .placementNoticeForSecondParentParentsList(respondentsList)
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
                .notice(testDocumentReference())
                .noticeDescription("Local authority description")
                .response(testDocumentReference())
                .responseDescription("Local authority response description")
                .build();

            final PlacementNoticeDocument secondParentNotice = PlacementNoticeDocument.builder()
                .type(PARENT_SECOND)
                .recipientName("Recipient 2")
                .respondentId(respondent2.getId())
                .notice(testDocumentReference())
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
                .placementNoticeForLocalAuthorityRequired(YES)
                .placementNoticeForLocalAuthority(localAuthorityNotice.getNotice())
                .placementNoticeForLocalAuthorityDescription(localAuthorityNotice.getNoticeDescription())
                .placementNoticeResponseFromLocalAuthorityReceived(YES)
                .placementNoticeResponseFromLocalAuthority(localAuthorityNotice.getResponse())
                .placementNoticeResponseFromLocalAuthorityDescription(localAuthorityNotice.getResponseDescription())
                .placementNoticeForCafcassRequired(NO)
                .placementNoticeResponseFromCafcassReceived(NO)
                .placementNoticeForFirstParentRequired(NO)
                .placementNoticeResponseFromFirstParentReceived(NO)
                .placementNoticeForSecondParentRequired(YES)
                .placementNoticeForSecondParent(secondParentNotice.getNotice())
                .placementNoticeForSecondParentDescription(secondParentNotice.getNoticeDescription())
                .placementNoticeResponseFromSecondParentReceived(NO)
                .placementNoticeForFirstParentParentsList(respondentsDynamicList(respondent1, respondent2))
                .placementNoticeForSecondParentParentsList(respondentsDynamicList(1, respondent1, respondent2))
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
    class CheckNotices {

        @Test
        void shouldReturnErrorWhenFirstAndSecondParentsAreSame() {

            final DynamicList respondentsList1 = respondentsDynamicList(1, respondent1, respondent2);
            final DynamicList respondentsList2 = respondentsDynamicList(1, respondent1, respondent2);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementNoticeForFirstParentParentsList(respondentsList1)
                .placementNoticeForSecondParentParentsList(respondentsList2)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final List<String> actualErrors = underTest.checkNotices(caseData);

            assertThat(actualErrors).containsExactly("First and second parents can not be same");
        }

        @Test
        void shouldNotReturnErrorWhenFirstAndSecondParentsAreDifferent() {

            final DynamicList respondentList1 = respondentsDynamicList(0, respondent1, respondent2);
            final DynamicList respondentList2 = respondentsDynamicList(1, respondent1, respondent2);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementNoticeForFirstParentParentsList(respondentList1)
                .placementNoticeForSecondParentParentsList(respondentList2)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final List<String> actualErrors = underTest.checkNotices(caseData);

            assertThat(actualErrors).isEmpty();
        }

        @Test
        void shouldNotReturnErrorWhenFirstAndSecondParentsAreNotSelected() {

            final DynamicList respondentList1 = respondentsDynamicList(respondent1, respondent2);
            final DynamicList respondentList2 = respondentsDynamicList(respondent1, respondent2);

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementNoticeForFirstParentParentsList(respondentList1)
                .placementNoticeForSecondParentParentsList(respondentList2)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final List<String> actualErrors = underTest.checkNotices(caseData);

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

        @BeforeEach
        void init() {
            when(sealingService.sealDocument(application, SealType.ENGLISH)).thenReturn(sealedApplication);
        }

        @Test
        void shouldSealApplicationAndAddNewPlacementToListOfExistingPlacements() {

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
                .placementEventData(placementEventData)
                .build();

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
                .notice(testDocumentReference())
                .noticeDescription("Local authority description")
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

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);

            verifyNoInteractions(sealingService, time);
        }

        @Test
        void shouldAddLocalAuthorityPlacementNoticeWithResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference localAuthorityNotice = testDocumentReference();
            final DocumentReference localAuthorityNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForLocalAuthorityRequired(YES)
                .placementNoticeForLocalAuthority(localAuthorityNotice)
                .placementNoticeForLocalAuthorityDescription("LA notice")
                .placementNoticeResponseFromLocalAuthorityReceived(YES)
                .placementNoticeResponseFromLocalAuthority(localAuthorityNoticeResponse)
                .placementNoticeResponseFromLocalAuthorityDescription("LA response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(LOCAL_AUTHORITY)
                    .recipientName("Local authority")
                    .notice(localAuthorityNotice)
                    .noticeDescription("LA notice")
                    .response(localAuthorityNoticeResponse)
                    .responseDescription("LA response")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddLocalAuthorityPlacementNoticeWithoutResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference localAuthorityNotice = testDocumentReference();
            final DocumentReference localAuthorityNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForLocalAuthorityRequired(YES)
                .placementNoticeForLocalAuthority(localAuthorityNotice)
                .placementNoticeForLocalAuthorityDescription("LA notice")
                .placementNoticeResponseFromLocalAuthorityReceived(NO)
                .placementNoticeResponseFromLocalAuthority(localAuthorityNoticeResponse)
                .placementNoticeResponseFromLocalAuthorityDescription("LA response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(LOCAL_AUTHORITY)
                    .recipientName("Local authority")
                    .notice(localAuthorityNotice)
                    .noticeDescription("LA notice")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldNotAddLocalAuthorityPlacementNotice() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference localAuthorityNotice = testDocumentReference();
            final DocumentReference localAuthorityNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placementNoticeForLocalAuthorityRequired(NO)
                .placement(currentPlacement)
                .placementNoticeForLocalAuthority(localAuthorityNotice)
                .placementNoticeForLocalAuthorityDescription("LA notice")
                .placementNoticeResponseFromLocalAuthorityReceived(NO)
                .placementNoticeResponseFromLocalAuthority(localAuthorityNoticeResponse)
                .placementNoticeResponseFromLocalAuthorityDescription("LA response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddCafcassPlacementNoticeWithResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference cafcassNotice = testDocumentReference();
            final DocumentReference cafcassNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForCafcassRequired(YES)
                .placementNoticeForCafcass(cafcassNotice)
                .placementNoticeForCafcassDescription("Cafcass notice")
                .placementNoticeResponseFromCafcassReceived(YES)
                .placementNoticeResponseFromCafcass(cafcassNoticeResponse)
                .placementNoticeResponseFromCafcassDescription("Cafcass response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(CAFCASS)
                    .recipientName("Cafcass")
                    .notice(cafcassNotice)
                    .noticeDescription("Cafcass notice")
                    .response(cafcassNoticeResponse)
                    .responseDescription("Cafcass response")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddCafcassPlacementNoticeWithoutResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference cafcassNotice = testDocumentReference();
            final DocumentReference cafcassNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForCafcassRequired(YES)
                .placementNoticeForCafcass(cafcassNotice)
                .placementNoticeForCafcassDescription("Cafcass notice")
                .placementNoticeResponseFromCafcassReceived(NO)
                .placementNoticeResponseFromCafcass(cafcassNoticeResponse)
                .placementNoticeResponseFromCafcassDescription("Cafcass response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(CAFCASS)
                    .recipientName("Cafcass")
                    .notice(cafcassNotice)
                    .noticeDescription("Cafcass notice")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldNotAddCafcassPlacementNotice() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference cafcassNotice = testDocumentReference();
            final DocumentReference cafcassNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForCafcassRequired(NO)
                .placementNoticeForCafcass(cafcassNotice)
                .placementNoticeForCafcassDescription("Cafcass notice")
                .placementNoticeResponseFromCafcassReceived(NO)
                .placementNoticeResponseFromCafcass(cafcassNoticeResponse)
                .placementNoticeResponseFromCafcassDescription("Cafcass response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddFirstParentPlacementNoticeWithResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference firstParentNotice = testDocumentReference();
            final DocumentReference firstParentNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForFirstParentRequired(YES)
                .placementNoticeForFirstParent(firstParentNotice)
                .placementNoticeForFirstParentDescription("First parent notice")
                .placementNoticeForFirstParentParentsList(respondentsDynamicList(0, respondent1, respondent2))
                .placementNoticeResponseFromFirstParentReceived(YES)
                .placementNoticeResponseFromFirstParent(firstParentNoticeResponse)
                .placementNoticeResponseFromFirstParentDescription("First parent response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(PARENT_FIRST)
                    .recipientName("John Smith - father")
                    .respondentId(respondent1.getId())
                    .notice(firstParentNotice)
                    .noticeDescription("First parent notice")
                    .response(firstParentNoticeResponse)
                    .responseDescription("First parent response")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddFirstParentPlacementNoticeWithoutResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference firstParentNotice = testDocumentReference();
            final DocumentReference firstParentNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForFirstParentRequired(YES)
                .placementNoticeForFirstParent(firstParentNotice)
                .placementNoticeForFirstParentDescription("First parent notice")
                .placementNoticeForFirstParentParentsList(respondentsDynamicList(0, respondent1, respondent2))
                .placementNoticeResponseFromFirstParentReceived(NO)
                .placementNoticeResponseFromFirstParent(firstParentNoticeResponse)
                .placementNoticeResponseFromFirstParentDescription("First parent response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(PARENT_FIRST)
                    .recipientName("John Smith - father")
                    .respondentId(respondent1.getId())
                    .notice(firstParentNotice)
                    .noticeDescription("First parent notice")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldNotAddFirstParentPlacementNotice() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference firstParentNotice = testDocumentReference();
            final DocumentReference firstParentNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForFirstParentRequired(NO)
                .placementNoticeForFirstParent(firstParentNotice)
                .placementNoticeForFirstParentDescription("First parent notice")
                .placementNoticeForFirstParentParentsList(respondentsDynamicList(0, respondent1, respondent2))
                .placementNoticeResponseFromFirstParentReceived(NO)
                .placementNoticeResponseFromFirstParent(firstParentNoticeResponse)
                .placementNoticeResponseFromFirstParentDescription("First parent response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddSecondParentPlacementNoticeWithResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference secondParentNotice = testDocumentReference();
            final DocumentReference secondParentNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForSecondParentRequired(YES)
                .placementNoticeForSecondParent(secondParentNotice)
                .placementNoticeForSecondParentDescription("Second parent notice")
                .placementNoticeForSecondParentParentsList(respondentsDynamicList(1, respondent1, respondent2))
                .placementNoticeResponseFromSecondParentReceived(YES)
                .placementNoticeResponseFromSecondParent(secondParentNoticeResponse)
                .placementNoticeResponseFromSecondParentDescription("Second parent response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(PARENT_SECOND)
                    .recipientName("Eva Smith - mother")
                    .respondentId(respondent2.getId())
                    .notice(secondParentNotice)
                    .noticeDescription("Second parent notice")
                    .response(secondParentNoticeResponse)
                    .responseDescription("Second parent response")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddSecondParentPlacementNoticeWithoutResponse() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference secondParentNotice = testDocumentReference();
            final DocumentReference secondParentNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForSecondParentRequired(YES)
                .placementNoticeForSecondParent(secondParentNotice)
                .placementNoticeForSecondParentDescription("Second parent notice")
                .placementNoticeForSecondParentParentsList(respondentsDynamicList(1, respondent1, respondent2))
                .placementNoticeResponseFromSecondParentReceived(NO)
                .placementNoticeResponseFromSecondParent(secondParentNoticeResponse)
                .placementNoticeResponseFromSecondParentDescription("Second parent response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(PlacementNoticeDocument.builder()
                    .type(PARENT_SECOND)
                    .recipientName("Eva Smith - mother")
                    .respondentId(respondent2.getId())
                    .notice(secondParentNotice)
                    .noticeDescription("Second parent notice")
                    .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldNotAddSecondParentPlacementNotice() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference secondParentNotice = testDocumentReference();
            final DocumentReference secondParentNoticeResponse = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForSecondParentRequired(NO)
                .placementNoticeForSecondParent(secondParentNotice)
                .placementNoticeForSecondParentDescription("Second parent notice")
                .placementNoticeForSecondParentParentsList(respondentsDynamicList(1, respondent1, respondent2))
                .placementNoticeResponseFromSecondParentReceived(NO)
                .placementNoticeResponseFromSecondParent(secondParentNoticeResponse)
                .placementNoticeResponseFromSecondParentDescription("Second parent response")
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
                .build();

            assertThat(actualPlacementData).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldAddMultipleNoticesOfPlacement() {

            final Placement currentPlacement = Placement.builder()
                .application(application)
                .build();

            final DocumentReference localAuthorityNotice = testDocumentReference();
            final DocumentReference cafcassNotice = testDocumentReference();
            final DocumentReference firstParentNotice = testDocumentReference();
            final DocumentReference secondParentNotice = testDocumentReference();

            final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(currentPlacement)
                .placementNoticeForLocalAuthorityRequired(YES)
                .placementNoticeForLocalAuthority(localAuthorityNotice)
                .placementNoticeForLocalAuthorityDescription("LA notice")
                .placementNoticeResponseFromLocalAuthorityReceived(NO)
                .placementNoticeForCafcassRequired(YES)
                .placementNoticeForCafcass(cafcassNotice)
                .placementNoticeForCafcassDescription("Cafcass notice")
                .placementNoticeResponseFromCafcassReceived(NO)
                .placementNoticeForFirstParentRequired(YES)
                .placementNoticeForFirstParent(firstParentNotice)
                .placementNoticeForFirstParentDescription("First parent notice")
                .placementNoticeForFirstParentParentsList(respondentsDynamicList(0, respondent1, respondent2))
                .placementNoticeResponseFromFirstParentReceived(NO)
                .placementNoticeForSecondParentRequired(YES)
                .placementNoticeForSecondParent(secondParentNotice)
                .placementNoticeForSecondParentDescription("Second parent notice")
                .placementNoticeForSecondParentParentsList(respondentsDynamicList(1, respondent1, respondent2))
                .placementNoticeResponseFromSecondParentReceived(NO)
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(placementEventData)
                .build();

            final PlacementEventData actualPlacementData = underTest.savePlacement(caseData);

            final Placement expectedPlacement = currentPlacement.toBuilder()
                .application(sealedApplication)
                .noticeDocuments(wrapElements(
                    PlacementNoticeDocument.builder()
                        .type(LOCAL_AUTHORITY)
                        .recipientName("Local authority")
                        .notice(localAuthorityNotice)
                        .noticeDescription("LA notice")
                        .build(),
                    PlacementNoticeDocument.builder()
                        .type(CAFCASS)
                        .recipientName("Cafcass")
                        .notice(cafcassNotice)
                        .noticeDescription("Cafcass notice")
                        .build(),
                    PlacementNoticeDocument.builder()
                        .type(PARENT_FIRST)
                        .recipientName("John Smith - father")
                        .respondentId(respondent1.getId())
                        .notice(firstParentNotice)
                        .noticeDescription("First parent notice")
                        .build(),
                    PlacementNoticeDocument.builder()
                        .type(PARENT_SECOND)
                        .recipientName("Eva Smith - mother")
                        .respondentId(respondent2.getId())
                        .notice(secondParentNotice)
                        .noticeDescription("Second parent notice")
                        .build()))
                .build();

            final PlacementEventData expectedPlacementData = placementEventData.toBuilder()
                .placement(expectedPlacement)
                .placements(wrapElements(currentPlacement))
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

            assertThat(events).containsExactly(new PlacementApplicationAdded(caseData));
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

            assertThat(events).containsExactly(new PlacementApplicationAdded(caseData));
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

            assertThat(events).containsExactly(new PlacementApplicationEdited(caseData));
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
