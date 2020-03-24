package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.InterimOrderKey;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerTest extends AbstractControllerTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String CASE_ID = "12345";
    private final byte[] pdf = {1, 2, 3, 4, 5};
    private Document document;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private Time time;

    GeneratedOrderControllerTest() {
        super("create-order");
    }

    @BeforeEach
    void setUp() {
        document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("order.pdf", pdf);

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Test
    void aboutToStartShouldSetDateOfIssueAsTodayByDefault() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("familyManCaseNumber", "123"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData().get("dateOfIssue")).isEqualTo(time.now().toLocalDate().toString());
    }

    //TODO TECHDEBT move tests for each callback to separate file as done with GeneratedOrderControllerSubmittedTest
    // FPLA-1468
    @Nested
    class AboutToSubmit {

        @Test
        void aboutToSubmitShouldAddC21OrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
            final CaseDetails caseDetails = buildCaseDetails(commonCaseDetailsComponents(BLANK_ORDER, null)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .order(GeneratedOrder.builder()
                    .title("Example Order")
                    .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                    .build()));

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            GeneratedOrder expectedC21Order = commonExpectedOrderComponents(BLANK_ORDER.getLabel())
                .title("Example Order")
                .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                .build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedC21Order);
        }

        @Test
        void aboutToSubmitShouldNotHaveDraftAppendedToFilename() {
            final CaseDetails caseDetails = buildCaseDetails(
                commonCaseDetailsComponents(CARE_ORDER, FINAL)
                    .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            );

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            final CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            assertThat(caseData.getOrderCollection().get(0).getValue().getDocument()).isEqualTo(expectedDocument());
        }

        @ParameterizedTest
        @EnumSource(GeneratedOrderSubtype.class)
        void aboutToSubmitShouldAddCareOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields(
            GeneratedOrderSubtype subtype) {

            final CaseDetails caseDetails = buildCaseDetails(
                commonCaseDetailsComponents(CARE_ORDER, subtype)
                    .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
                    .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                    .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build())
            );

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            final String expiryDate = subtype == INTERIM ? "End of the proceedings" : null;
            GeneratedOrder expectedCareOrder = commonExpectedOrderComponents(
                subtype.getLabel() + " " + "care order").expiryDate(expiryDate).build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedCareOrder);
        }

        @Test
        void aboutToSubmitShouldAddInterimSupervisionOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
            final CaseDetails caseDetails = buildCaseDetails(commonCaseDetailsComponents(SUPERVISION_ORDER, INTERIM)
                .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build())
            );

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            GeneratedOrder expectedSupervisionOrder = commonExpectedOrderComponents(
                "Interim supervision order").expiryDate("End of the proceedings").build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedSupervisionOrder);
        }

        @Test
        void aboutToSubmitShouldAddFinalSupervisionOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
            final CaseDetails caseDetails = buildCaseDetails(commonCaseDetailsComponents(SUPERVISION_ORDER, FINAL)
                .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .orderMonths(14));

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            LocalDateTime orderExpiration = time.now().plusMonths(14);
            GeneratedOrder expectedSupervisionOrder = commonExpectedOrderComponents(
                "Final supervision order")
                .expiryDate(formatLocalDateTimeBaseUsingFormat(orderExpiration, "h:mma, d MMMM y"))
                .build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedSupervisionOrder);
        }

        private CaseDetails buildCaseDetails(CaseData.CaseDataBuilder builder) {
            return CaseDetails.builder()
                .data(mapper.convertValue(builder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseData.CaseDataBuilder commonCaseDetailsComponents(GeneratedOrderType orderType,
                                                                     GeneratedOrderSubtype subtype) {
            return CaseData.builder().orderTypeAndDocument(
                OrderTypeAndDocument.builder()
                    .type(orderType)
                    .subtype(subtype)
                    .document(DocumentReference.builder().build())
                    .build())
                .judgeAndLegalAdvisor(
                    JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .judgeLastName("Judy")
                        .legalAdvisorName("Peter Parker")
                        .build())
                .familyManCaseNumber("12345L")
                .dateOfIssue(time.now().toLocalDate());
        }

        private GeneratedOrder.GeneratedOrderBuilder commonExpectedOrderComponents(String fullType) {
            return GeneratedOrder.builder()
                .type(fullType)
                .dateOfIssue(formatLocalDateTimeBaseUsingFormat(time.now(), "d MMMM yyyy"))
                .document(expectedDocument())
                .date(formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy"))
                .judgeAndLegalAdvisor(
                    JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .judgeLastName("Judy")
                        .legalAdvisorName("Peter Parker")
                        .build()
                );
        }

        private DocumentReference expectedDocument() {
            return DocumentReference.builder()
                .binaryUrl(document.links.binary.href)
                .filename("file.pdf")
                .url(document.links.self.href)
                .build();
        }

        private void aboutToSubmitAssertions(Map<String, Object> data, GeneratedOrder expectedOrder) {
            List<String> keys = stream(GeneratedOrderKey.values()).map(GeneratedOrderKey::getKey).collect(toList());
            keys.addAll(stream(GeneratedEPOKey.values()).map(GeneratedEPOKey::getKey).collect(toList()));
            keys.addAll(stream(InterimOrderKey.values()).map(InterimOrderKey::getKey).collect(toList()));

            assertThat(data).doesNotContainKeys(keys.toArray(String[]::new));

            List<Element<GeneratedOrder>> orders = mapper.convertValue(data.get("orderCollection"),
                new TypeReference<>() {});

            assertThat(orders.get(0).getValue()).isEqualTo(expectedOrder);
        }
    }

    @Nested
    class PopulateChildSelectorMidEvent {
        @Test
        void shouldPopulateChildSelectorAndLabelWhenNoIsSelected() {
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseDetails("No"), "populate-selector");

            CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            assertThat(callbackResponse.getData().get("children_label"))
                .isEqualTo("Child 1: Wallace\nChild 2: Gromit\n");

            assertThat(caseData.getChildSelector()).isEqualTo(getExpectedChildSelector());
        }

        @Test
        void shouldNotPopulateChildSelectorAndLabelWhenYesIsSelected() {
            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                buildCaseDetails("Yes"), "populate-selector");

            CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            assertThat(callbackResponse.getData().get("children_label")).isNull();
            assertThat(caseData.getChildSelector()).isNull();
        }

        private ChildSelector getExpectedChildSelector() {
            return ChildSelector.builder()
                .childCount("12")
                .build();
        }

        private CaseDetails buildCaseDetails(String choice) {
            CaseData caseData = CaseData.builder()
                .children1(createChildren("Wallace", "Gromit"))
                .orderAppliesToAllChildren(choice)
                .build();

            return CaseDetails.builder()
                .data(mapper.convertValue(caseData, new TypeReference<>() {}))
                .build();
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
    }

    @TestInstance(PER_CLASS)
    @Nested
    class GenerateDocumentMidEvent {
        @ParameterizedTest
        @MethodSource("generateDocumentMidEventArgumentSource")
        void shouldGenerateDocumentWithCorrectNameWhenOrderTypeIsValid(CaseDetails caseDetails,
                                                                       String fileName,
                                                                       DocmosisTemplates templateName) {
            final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
                caseDetails, "generate-document");

            verify(docmosisDocumentGeneratorService).generateDocmosisDocument(any(), eq(templateName));
            verify(uploadDocumentService).uploadPDF(userId, userAuthToken, pdf, fileName);

            final CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            assertThat(caseData.getOrderTypeAndDocument().getDocument()).isEqualTo(expectedDraftDocument());
        }

        @Test
        void shouldNotGenerateOrderDocumentWhenOrderTypeIsCareOrderWithNoFurtherDirections() {
            postMidEvent(generateCareOrderCaseDetailsWithoutFurtherDirections(), "generate-document");

            verify(docmosisDocumentGeneratorService, never()).generateDocmosisDocument(any(), any());
            verify(uploadDocumentService, never()).uploadPDF(any(), any(), any(), any());
        }

        @AfterEach
        void resetInvocations() {
            reset(docmosisDocumentGeneratorService);
            reset(uploadDocumentService);
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

        private CaseDetails generateEmergencyProtectionOrderCaseDetails() {
            final CaseData.CaseDataBuilder dataBuilder = CaseData.builder();

            dataBuilder.order(GeneratedOrder.builder().details("").build())
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(EMERGENCY_PROTECTION_ORDER).build())
                .dateOfIssue(time.now().toLocalDate());

            generateDefaultValues(dataBuilder);
            generateEpoValues(dataBuilder);

            dataBuilder.orderFurtherDirections(FurtherDirections.builder()
                .directionsNeeded("Yes")
                .directions("Some directions")
                .build());

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseDetails generateBlankOrderCaseDetails() {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonOrderDetails(BLANK_ORDER, null)
                .order(GeneratedOrder.builder().details("").build());

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseDetails generateCareOrderCaseDetailsWithoutFurtherDirections() {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonOrderDetails(CARE_ORDER, INTERIM);

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseDetails generateCareOrderCaseDetails(GeneratedOrderSubtype subtype) {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonOrderDetails(CARE_ORDER, subtype);

            dataBuilder.orderFurtherDirections(generateOrderFurtherDirections());

            if (subtype == INTERIM) {
                dataBuilder.interimEndDate(generateInterimEndDate());
            }

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseDetails generateSupervisionOrderCaseDetails(GeneratedOrderSubtype subtype) {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonOrderDetails(SUPERVISION_ORDER, subtype);

            dataBuilder.orderFurtherDirections(generateOrderFurtherDirections())
                .orderMonths(5);

            if (subtype == INTERIM) {
                dataBuilder.interimEndDate(generateInterimEndDate());
            }

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseData.CaseDataBuilder generateCommonOrderDetails(GeneratedOrderType type,
                                                                    GeneratedOrderSubtype subtype) {
            final CaseData.CaseDataBuilder builder = CaseData.builder()
                .orderTypeAndDocument(OrderTypeAndDocument.builder()
                    .type(type)
                    .subtype(subtype)
                    .build())
                .dateOfIssue(time.now().toLocalDate());

            generateDefaultValues(builder);

            return builder;
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
                .epoEndDate(time.now())
                .epoType(REMOVE_TO_ACCOMMODATION)
                .epoRemovalAddress(Address.builder()
                    .addressLine1("Unit 1")
                    .addressLine2("Petty France")
                    .postTown("Lurgan")
                    .postcode("BT66 7RR")
                    .build());
        }

        private void generateDefaultValues(CaseData.CaseDataBuilder builder) {
            builder.caseLocalAuthority(LOCAL_AUTHORITY_CODE);
            builder.familyManCaseNumber(FAMILY_MAN_CASE_NUMBER);
            builder.judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build());
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
    }
}
