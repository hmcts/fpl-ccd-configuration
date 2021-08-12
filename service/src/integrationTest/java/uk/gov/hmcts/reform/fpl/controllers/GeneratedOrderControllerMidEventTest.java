package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.EPOExclusionRequirementType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.UploadedOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.OrderHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerMidEventTest extends AbstractCallbackTest {

    private Document document;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    GeneratedOrderControllerMidEventTest() {
        super("create-order");
    }

    @BeforeEach
    void setUp() {
        document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("order.pdf", DOCUMENT_CONTENT);

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class),
            any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);
    }

    private List<Element<Child>> createChildren(String... firstNames) {
        Child[] children = new Child[firstNames.length];
        for (int i = 0; i < firstNames.length; i++) {
            children[i] = Child.builder()
                .party(ChildParty.builder()
                    .firstName(firstNames[i])
                    .build())
                .build();
        }
        return wrapElements(children);
    }

    private Child createChild(String name, boolean finalOrderIssued) {
        Child child = Child.builder()
            .party(ChildParty.builder()
                .firstName(name)
                .build())
            .build();
        if (finalOrderIssued) {
            child.setFinalOrderIssued(YesNo.YES.getValue());
            child.setFinalOrderIssuedType(CARE_ORDER.getLabel());
        }
        return child;
    }

    private CaseDetails buildCaseDetails(List<Element<Child>> children) {
        return buildCaseDetails(null, children);
    }

    private CaseDetails buildCaseDetails(String choice, List<Element<Child>> children) {
        return buildCaseDetails(choice, children, CARE_ORDER, FINAL);
    }

    private CaseDetails buildCaseDetails(String choice, GeneratedOrderType type, GeneratedOrderSubtype subType) {
        return buildCaseDetails(choice, createChildren("Wallace", "Gromit"), type, subType);
    }

    private CaseDetails buildCaseDetails(String choice,
                                         List<Element<Child>> children,
                                         GeneratedOrderType type,
                                         GeneratedOrderSubtype subType) {
        CaseData caseData = CaseData.builder()
            .children1(children)
            .orderAppliesToAllChildren(choice)
            .orderTypeAndDocument(OrderTypeAndDocument.builder().type(type).subtype(subType).build())
            .build();

        return asCaseDetails(caseData);
    }

    @Nested
    class PopulateChildSelectorMidEvent {

        private final String callbackType = "populate-children-selector";

        @Test
        void shouldPopulateChildSelectorAndLabelWithBasicInfoWhenNoIsSelectedAndOrderIsNotFinal() {
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseDetails("No", SUPERVISION_ORDER, INTERIM), callbackType);

            CaseData caseData = extractCaseData(callbackResponse);

            assertThat(callbackResponse.getData().get("children_label"))
                .isEqualTo("Child 1: Wallace\nChild 2: Gromit\n");

            assertThat(caseData.getChildSelector()).isEqualTo(newSelector(2));
        }

        @Test
        void shouldPopulateChildSelectorAndLabelWhenNoIsSelectedAndOrderIsFinalAndNoOneHasFinalOrderIssued() {
            AboutToStartOrSubmitCallbackResponse response = postMidEvent(
                buildCaseDetails("No", CARE_ORDER, FINAL), callbackType);

            CaseData caseData = extractCaseData(response);

            assertThat(response.getData().get("children_label"))
                .isEqualTo("Child 1: Wallace\nChild 2: Gromit\n");

            assertThat(caseData.getChildSelector()).isEqualTo(newSelector(2));
        }

        @Test
        void shouldNotPopulateChildSelectorAndLabelWhenYesIsSelected() {
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseDetails("Yes", CARE_ORDER, FINAL), callbackType);

            assertThat(callbackResponse.getData()).extracting("children_label", "childSelector").containsOnlyNulls();
        }

        @Test
        void shouldPopulateChildSelectorAndLabelWhenNoIsSelectedAndFinalOrderIssuedOnChildren() {

            List<Element<Child>> children = wrapElements(createChild("Fred", false),
                createChild("Jane", true),
                createChild("Paul", true),
                createChild("Bill", false));

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseDetails("No", children), callbackType);

            CaseData caseData = extractCaseData(callbackResponse);

            assertThat(callbackResponse.getData().get("children_label"))
                .isEqualTo("Child 1: Fred\nChild 2: Jane - Care order issued\nChild 3: Paul - Care order issued\n"
                    + "Child 4: Bill\n");

            Selector expected = newSelector(4);
            assertThat(caseData.getChildSelector()).isEqualTo(expected);
        }
    }

    @Nested
    class FinalOrderFlagsPreparation {

        private final String callbackType = "prepare-selected-order";

        @Test
        void shouldPopulateLabelsWhenSingleChildHasFinalOrderRemaining() {
            List<Element<Child>> children = wrapElements(createChild("Fred", true),
                createChild("Jane", false),
                createChild("Paul", true),
                createChild("Bill", true));

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(
                buildCaseDetails(children), callbackType);

            assertThat(response.getData().get("remainingChildIndex"))
                .isEqualTo("1");

            assertThat(response.getData().get("remainingChild"))
                .isEqualTo("Jane");

            assertThat(response.getData().get("otherFinalOrderChildren"))
                .isEqualTo("Fred - Care order issued\nPaul - Care order issued\nBill - Care order issued");
        }

        @Test
        void shouldNotPopulateLabelsWhenMultipleChildrenHaveFinalOrderRemaining() {
            List<Element<Child>> children = wrapElements(createChild("Fred", false),
                createChild("Jane", false),
                createChild("Paul", true),
                createChild("Bill", true));

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(
                buildCaseDetails(children), callbackType);

            assertThat(response.getData())
                .extracting("remainingChildIndex", "remainingChild", "otherFinalOrderChildren")
                .containsOnlyNulls();
        }

        @Test
        void shouldNotPopulateLabelsWhenOrderTypeIsNotClosable() {
            List<Element<Child>> children = wrapElements(createChild("Fred", true),
                createChild("Jane", false),
                createChild("Paul", true),
                createChild("Bill", true));

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(
                buildCaseDetails(null, children, SUPERVISION_ORDER, INTERIM), callbackType);

            assertThat(response.getData())
                .extracting("remainingChildIndex", "remainingChild", "otherFinalOrderChildren")
                .containsOnlyNulls();
        }

        @Test
        void shouldReturnAnErrorWhenAllChildrenHaveAFinalOrderIssuedAndTryToIssueAnother() {
            List<Element<Child>> chuckleBrothers = wrapElements(createChild("Paul", true),
                createChild("Barry", true));

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(
                buildCaseDetails(chuckleBrothers), callbackType);

            assertThat(response.getErrors()).containsOnly("All children in the case already have final orders");
        }

        @Test
        void shouldNotReturnAnErrorWhenAllChildrenHaveAFinalOrderIssuedButIssuingANonFinalOrder() {
            List<Element<Child>> chuckleBrothers = wrapElements(createChild("Paul", true),
                createChild("Barry", true));

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(
                buildCaseDetails(null, chuckleBrothers, BLANK_ORDER, null), callbackType);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class DischargeCaseOrderPreparation {

        private final String callbackType = "prepare-selected-order";

        @Test
        void shouldReturnErrorWhenCurrentOrderIsDischargeOfCareOrderAndNoExistingCareOrders() {
            GeneratedOrder order = order(SUPERVISION_ORDER, "1 May 2020");
            CaseDetails caseDetails = caseWithOrders(DISCHARGE_OF_CARE_ORDER, order);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, callbackType);

            assertThat(callbackResponse.getData().get("singleCareOrder_label")).isNull();
            assertThat(callbackResponse.getData().get("multipleCareOrder_label")).isNull();
            assertThat(callbackResponse.getData().get("careOrderSelector")).isNull();
            assertThat(callbackResponse.getErrors()).containsExactly("No care orders to be discharged");
        }

        @Test
        void shouldPopulateLabelForSingleCareOrder() {
            GeneratedOrder order = order(CARE_ORDER, INTERIM, "1 May 2020", child("John", "Smith"));

            CaseDetails caseDetails = caseWithOrders(DISCHARGE_OF_CARE_ORDER, order);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, callbackType);

            assertThat(callbackResponse.getData().get("singleCareOrder_label"))
                .isEqualTo("Create discharge of care order for John Smith");
            assertThat(callbackResponse.getData().get("multipleCareOrder_label")).isNull();
            assertThat(callbackResponse.getData().get("careOrderSelector")).isNull();
            assertThat(callbackResponse.getErrors()).isEmpty();
        }

        @Test
        void shouldPopulateLabelAndSelectorForMultipleCareOrders() {
            GeneratedOrder order1 = order(CARE_ORDER, INTERIM, "1 June 2019", child("John", "Smith"));
            GeneratedOrder order2 = order(CARE_ORDER, FINAL, "12 June 2019",
                child("John", "Smith"),
                child("Alex", "White"));

            CaseDetails caseDetails = caseWithOrders(DISCHARGE_OF_CARE_ORDER, order1, order2);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, callbackType);

            CaseData updatedCaseData = extractCaseData(callbackResponse);

            assertThat(callbackResponse.getData().get("multipleCareOrder_label"))
                .isEqualTo("Order 1: John Smith, 1 June 2019\nOrder 2: John Smith and Alex White, 12 June 2019");
            assertThat(callbackResponse.getData().get("singleCareOrder_label")).isNull();
            assertThat(updatedCaseData.getCareOrderSelector()).isEqualTo(newSelector(2));
            assertThat(callbackResponse.getErrors()).isEmpty();
        }

        @SafeVarargs
        private GeneratedOrder order(GeneratedOrderType type, GeneratedOrderSubtype subType, String issueDate,
                                     Element<Child>... children) {
            return GeneratedOrder.builder()
                .type(OrderHelper.getFullOrderType(type, subType))
                .dateOfIssue(issueDate)
                .children(Arrays.asList(children))
                .build();
        }

        @SafeVarargs
        private GeneratedOrder order(GeneratedOrderType type, String issueDate, Element<Child>... children) {
            return order(type, null, issueDate, children);
        }

        private Element<Child> child(String firstName, String lastName) {
            return testChild(firstName, lastName, null, null);
        }

        private CaseDetails caseWithOrders(GeneratedOrderType generatedOrderType, GeneratedOrder... orders) {
            return asCaseDetails(CaseData.builder()
                .orderCollection(wrapElements(orders))
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(generatedOrderType).build())
                .build());
        }
    }

    @TestInstance(PER_CLASS)
    @Nested
    class GenerateDocumentMidEvent {

        private final String callbackType = "generate-document";

        @Test
        void shouldAddCloseCaseLabelAndSetFlagToYesWhenCloseCasePageCanBeShown() {

            CaseData caseData = generateFinalCareOrderWithChildren("Yes");

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callbackType);

            verify(docmosisDocumentGeneratorService, never()).generateDocmosisDocument(anyMap(), any());

            assertThat(response.getData()).extracting("showCloseCaseFromOrderPage")
                .isEqualTo("YES");
        }

        @Test
        void shouldNotAddCloseCaseLabelAndSetFlagToNoWhenCloseCasePageCanNotBeShown() {

            CaseData caseData = generateFinalCareOrderWithChildren("No");

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callbackType);

            verify(docmosisDocumentGeneratorService, never()).generateDocmosisDocument(anyMap(), any());

            assertThat(response.getData()).extracting("showCloseCaseFromOrderPage", "close_case_label")
                .containsOnly("NO", null);
        }

        @Test
        void shouldAddCheckYourOrderDetailsWhenOrderTypeIsSubmitted() {

            List<Element<Child>> children = testChildren();
            String familyManCaseNumber = "famNum";
            DocumentReference uploadedOrder = testDocumentReference();

            CaseData caseData = CaseData.builder()
                .dateOfIssue(dateNow())
                .orderTypeAndDocument(OrderTypeAndDocument.builder()
                    .type(UPLOAD)
                    .uploadedOrderType(UploadedOrderType.C27)
                    .build())
                .uploadedOrder(uploadedOrder)
                .children1(children)
                .orderAppliesToAllChildren("Yes")
                .familyManCaseNumber(familyManCaseNumber)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callbackType);

            String childrenNames = children.stream()
                .map(child -> child.getValue().getParty().getFullName())
                .collect(Collectors.joining("\n"));

            Map<String, Object> documentMap = mapper.convertValue(uploadedOrder, new TypeReference<>() {
            });

            assertThat(response.getData())
                .extracting("readOnlyFamilyManCaseNumber", "readOnlyOrder", "readOnlyChildren")
                .containsExactly(familyManCaseNumber, documentMap, childrenNames);
        }

        @ParameterizedTest
        @MethodSource("generateDocumentMidEventArgumentSource")
        void shouldGenerateDocumentWithCorrectNameWhenOrderTypeIsValid(CaseData caseData,
                                                                       String fileName,
                                                                       DocmosisTemplates templateName) {
            final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, callbackType);
            final CaseData updatedCaseData = extractCaseData(callbackResponse);

            verify(docmosisDocumentGeneratorService).generateDocmosisDocument(any(DocmosisData.class),
                eq(templateName));
            verify(uploadDocumentService).uploadPDF(DOCUMENT_CONTENT, fileName);

            assertThat(updatedCaseData.getOrderTypeAndDocument().getDocument()).isEqualTo(expectedDraftDocument());
        }

        @Test
        void shouldNotGenerateOrderDocumentWhenOrderTypeIsCareOrderWithNoFurtherDirections() {
            postMidEvent(generateCareOrderCaseDetailsWithoutFurtherDirections(), callbackType);

            verify(docmosisDocumentGeneratorService, never()).generateDocmosisDocument(any(DocmosisData.class), any());
            verify(uploadDocumentService, never()).uploadPDF(any(), any());
        }

        @AfterEach
        void resetInvocations() {
            reset(docmosisDocumentGeneratorService);
            reset(uploadDocumentService);
        }

        @Test
        void shouldReturnAValidationErrorWhenJudgeEmailIsInvalid() {
            CaseData caseData = CaseData.builder()
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(CARE_ORDER).build())
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeEmailAddress("<John Doe> johndoe@email.com")
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), callbackType);

            assertThat(callbackResponse.getErrors()).contains(
                "Enter an email address in the correct format, for example name@example.com");
        }

        @Test
        void shouldNotReturnAValidationErrorWhenJudgeEmailIsValid() {
            CaseData caseData = CaseData.builder()
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(CARE_ORDER).build())
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeEmailAddress("test@test.com")
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), callbackType);

            assertThat(callbackResponse.getErrors()).isNull();
        }

        @Test
        void shouldNotReturnValidationErrorsWhenOrderTypeIsUpload() {
            List<Element<Child>> children = testChildren();
            String familyManCaseNumber = "famNum";
            DocumentReference uploadedOrder = testDocumentReference();

            CaseData caseData = CaseData.builder()
                .dateOfIssue(dateNow())
                .orderTypeAndDocument(OrderTypeAndDocument.builder()
                    .type(UPLOAD)
                    .uploadedOrderType(UploadedOrderType.C27)
                    .build())
                .uploadedOrder(uploadedOrder)
                .children1(children)
                .orderAppliesToAllChildren("Yes")
                .familyManCaseNumber(familyManCaseNumber)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeEmailAddress("<John Doe> johndoe@email.com")
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), callbackType);

            assertThat(callbackResponse.getErrors()).isNull();
        }

        private Stream<Arguments> generateDocumentMidEventArgumentSource() {
            return Stream.of(
                Arguments.of(generateBlankOrderCaseDetails(), "blank_order_c21.pdf", ORDER),
                Arguments.of(generateCareOrderCaseDetails(INTERIM), "interim_care_order.pdf", ORDER),
                Arguments.of(generateCareOrderCaseDetails(FINAL), "final_care_order.pdf", ORDER),
                Arguments.of(generateSupervisionOrderCaseDetails(INTERIM), "interim_supervision_order.pdf", ORDER),
                Arguments.of(generateSupervisionOrderCaseDetails(FINAL), "final_supervision_order.pdf", ORDER),
                Arguments.of(generateEmergencyProtectionOrderCaseDetails(), "emergency_protection_order.pdf", EPO)
            );
        }

        private CaseData generateEmergencyProtectionOrderCaseDetails() {
            final CaseData.CaseDataBuilder dataBuilder = CaseData.builder()
                .order(GeneratedOrder.builder().details("").build())
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(EMERGENCY_PROTECTION_ORDER).build())
                .dateAndTimeOfIssue(now())
                .epoExclusionRequirementType(EPOExclusionRequirementType.NO_TO_EXCLUSION)
                .orderFurtherDirections(FurtherDirections.builder()
                    .directionsNeeded("Yes")
                    .directions("Some directions")
                    .build());

            generateDefaultValues(dataBuilder);
            generateEpoValues(dataBuilder);

            return dataBuilder.build();
        }

        private CaseData generateBlankOrderCaseDetails() {
            return generateCommonOrderDetails(BLANK_ORDER, null)
                .order(GeneratedOrder.builder().details("").build())
                .build();
        }

        private CaseData generateCareOrderCaseDetailsWithoutFurtherDirections() {
            return generateCommonOrderDetails(CARE_ORDER, INTERIM).build();
        }

        private CaseData generateCareOrderCaseDetails(GeneratedOrderSubtype subtype) {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonOrderDetails(CARE_ORDER, subtype)
                .orderFurtherDirections(generateOrderFurtherDirections());

            if (subtype == INTERIM) {
                dataBuilder.interimEndDate(generateInterimEndDate());
            }

            return dataBuilder.build();
        }

        private CaseData generateSupervisionOrderCaseDetails(GeneratedOrderSubtype subtype) {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonOrderDetails(SUPERVISION_ORDER, subtype)
                .orderFurtherDirections(generateOrderFurtherDirections())
                .orderMonths(5);

            if (subtype == INTERIM) {
                dataBuilder.interimEndDate(generateInterimEndDate());
            }

            return dataBuilder.build();
        }

        private CaseData generateFinalCareOrderWithChildren(String finalOrderIssued) {
            return generateCommonOrderDetails(CARE_ORDER, FINAL)
                .children1(List.of(
                    childWithFinalOrderIssued("Yes"),
                    childWithFinalOrderIssued(finalOrderIssued)
                ))
                .build();
        }

        private CaseData.CaseDataBuilder generateCommonOrderDetails(GeneratedOrderType type,
                                                                    GeneratedOrderSubtype subtype) {
            final CaseData.CaseDataBuilder builder = CaseData.builder()
                .orderTypeAndDocument(OrderTypeAndDocument.builder()
                    .type(type)
                    .subtype(subtype)
                    .build())
                .dateOfIssue(dateNow());

            return generateDefaultValues(builder);
        }

        private InterimEndDate generateInterimEndDate() {
            return InterimEndDate.builder().type(END_OF_PROCEEDINGS).build();
        }

        private void generateEpoValues(CaseData.CaseDataBuilder builder) {
            builder
                .epoChildren(EPOChildren.builder()
                    .description("Description")
                    .descriptionNeeded("Yes")
                    .build())
                .epoPhrase(EPOPhrase.builder()
                    .includePhrase("Yes")
                    .build())
                .epoEndDate(now())
                .epoType(REMOVE_TO_ACCOMMODATION)
                .epoRemovalAddress(Address.builder()
                    .addressLine1("Unit 1")
                    .addressLine2("Petty France")
                    .postTown("Lurgan")
                    .postcode("BT66 7RR")
                    .build());
        }

        private CaseData.CaseDataBuilder generateDefaultValues(CaseData.CaseDataBuilder builder) {
            return builder.caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .familyManCaseNumber("SACCCCCCCC5676576567")
                .id(1234123412341234L)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build());
        }

        private FurtherDirections generateOrderFurtherDirections() {
            return FurtherDirections.builder()
                .directionsNeeded("Yes")
                .directions("Some directions")
                .build();
        }

        private DocumentReference expectedDraftDocument() {
            return DocumentReference.builder()
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .url(document.links.self.href)
                .build();
        }

        private Element<Child> childWithFinalOrderIssued(String finalOrderIssued) {
            Element<Child> childElement = testChild();
            childElement.getValue().setFinalOrderIssued(finalOrderIssued);
            return childElement;
        }
    }

    @Nested
    class PrePopulateEpoFieldsMidEvent {

        private final String callbackType = "populate-epo-parameters";

        @Test
        void shouldPrePopulateAddressFieldIfPresentInCaseData() {
            String address = "1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom";
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseData(), callbackType);

            CaseData caseData = extractCaseData(callbackResponse);

            assertThat(caseData.getEpoRemovalAddress().toString().equals(address));
        }

        @Test
        void shouldPrePopulateEpoTypeFieldIfPresentInCaseData() {
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseData(), callbackType);

            CaseData caseData = extractCaseData(callbackResponse);

            assertThat(caseData.getEpoType().equals(PREVENT_REMOVAL));
        }

        @Test
        void shouldPrePopulateWhoIsExcludedFieldIfPresentInCaseData() {
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseData(), callbackType);

            CaseData caseData = extractCaseData(callbackResponse);

            assertThat(caseData.getEpoWhoIsExcluded().equals("Test User"));
        }

        private CaseData buildCaseData() {
            return CaseData.builder()
                .orders(Orders.builder()
                    .epoType(EPOType.PREVENT_REMOVAL)
                    .excluded("Test User")
                    .address(Address.builder()
                        .addressLine1("1 Main Street")
                        .addressLine2("Lurgan")
                        .postTown("BT66 7PP")
                        .county("Armagh")
                        .country("United Kingdom")
                        .build())

                    .build())
                .build();
        }
    }
}
