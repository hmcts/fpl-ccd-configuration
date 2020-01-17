package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerTest extends AbstractControllerTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";

    private final LocalDateTime dateIn3Months = LocalDateTime.now().plusMonths(3);

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private Time time;

    GeneratedOrderControllerTest() {
        super("create-order");
    }

    @Test
    void aboutToStartShouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Test
    void shouldTriggerOrderEventWhenSubmitted() throws Exception {
        String expectedCaseReference = "19898989";
        postSubmittedEvent(buildCallbackRequest());

        verify(notificationClient, times(1)).sendEmail(
            eq(ORDER_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(expectedOrderLocalAuthorityParameters()), eq(expectedCaseReference));
    }

    private CallbackRequest buildCallbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(19898989L)
                .data(ImmutableMap.of(
                    "orderCollection", createOrders(),
                    "hearingDetails", createHearingBookings(dateIn3Months, dateIn3Months.plusHours(4)),
                    "respondents1", createRespondents(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())
            .build();
    }

    private Map<String, Object> commonNotificationParameters() {
        final String documentUrl = "http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";
        final String subjectLine = "Jones, " + FAMILY_MAN_CASE_NUMBER;

        return ImmutableMap.<String, Object>builder()
            .put("subjectLine", subjectLine)
            .put("linkToDocument", documentUrl)
            .put("hearingDetailsCallout", subjectLine + ", hearing " + dateFormatterService.formatLocalDateToString(
                dateIn3Months.toLocalDate(), FormatStyle.MEDIUM))
            .put("reference", "19898989")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/19898989")
            .build();
    }

    private Map<String, Object> expectedOrderLocalAuthorityParameters() {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonNotificationParameters())
            .put("localAuthorityOrCafcass", LOCAL_AUTHORITY_NAME)
            .build();
    }

    @Nested
    class AboutToSubmit {

        @Test
        void aboutToSubmitShouldAddC21OrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
            final CaseDetails caseDetails = buildCaseDetails(
                commonCaseDetailsComponents(BLANK_ORDER)
                    .order(GeneratedOrder.builder()
                        .title("Example Order")
                        .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                        .build()));


            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            GeneratedOrder expectedC21Order = commonExpectedOrderComponents(BLANK_ORDER)
                .title("Example Order")
                .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                .build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedC21Order);
        }

        @Test
        void aboutToSubmitShouldAddCareOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
            final CaseDetails caseDetails = buildCaseDetails(
                commonCaseDetailsComponents(CARE_ORDER)
                    .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build()));

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            GeneratedOrder expectedCareOrder = commonExpectedOrderComponents(CARE_ORDER).build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedCareOrder);
        }

        @Test
        void aboutToSubmitShouldAddSupervisionOrderToCaseDataAndRemoveTemporaryCaseDataOrderFields() {
            final CaseDetails caseDetails = buildCaseDetails(
                commonCaseDetailsComponents(SUPERVISION_ORDER)
                    .orderFurtherDirections(FurtherDirections.builder().directionsNeeded("No").build())
                    .orderMonths(14));

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(
                caseDetails);

            LocalDateTime orderExpiration = time.now().plusMonths(14);
            GeneratedOrder expectedCareOrder = commonExpectedOrderComponents(SUPERVISION_ORDER)
                .expiryDate(
                    dateFormatterService.formatLocalDateTimeBaseUsingFormat(orderExpiration, "h:mma, d MMMM y"))
                .build();

            aboutToSubmitAssertions(callbackResponse.getData(), expectedCareOrder);
        }

        private CaseDetails buildCaseDetails(CaseData.CaseDataBuilder builder) {
            return CaseDetails.builder()
                .data(mapper.convertValue(builder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseData.CaseDataBuilder commonCaseDetailsComponents(GeneratedOrderType orderType) {
            return CaseData.builder().orderTypeAndDocument(
                OrderTypeAndDocument.builder()
                    .type(orderType)
                    .document(DocumentReference.builder().build())
                    .build())
                .judgeAndLegalAdvisor(
                    JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .judgeLastName("Judy")
                        .legalAdvisorName("Peter Parker")
                        .build())
                .familyManCaseNumber("12345L");
        }

        private GeneratedOrder.GeneratedOrderBuilder commonExpectedOrderComponents(GeneratedOrderType orderType) {
            return GeneratedOrder.builder()
                .type(orderType)
                .document(DocumentReference.builder().build())
                .date(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy"))
                .judgeAndLegalAdvisor(
                    JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .judgeLastName("Judy")
                        .legalAdvisorName("Peter Parker")
                        .build()
                );
        }

        private void aboutToSubmitAssertions(Map<String, Object> data, GeneratedOrder expectedOrder) {
            List<Element<GeneratedOrder>> orders = mapper.convertValue(data.get("orderCollection"),
                new TypeReference<>() {});

            Arrays.stream(GeneratedOrderKey.values())
                .forEach(ccdField -> assertThat(data).doesNotContainKey(ccdField.getKey()));

            Arrays.stream(GeneratedEPOKey.values())
                .forEach(ccdField -> assertThat(data).doesNotContainKey(ccdField.getKey()));

            assertThat(orders.get(0).getValue()).isEqualTo(expectedOrder);
        }
    }

    @TestInstance(PER_CLASS)
    @Nested
    class MidEvent {
        private final byte[] pdf = {1, 2, 3, 4, 5};
        private Document document;

        @BeforeEach
        void setUp() {
            document = document();
            DocmosisDocument docmosisDocument = new DocmosisDocument("order.pdf", pdf);

            given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
            given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);
        }

        @ParameterizedTest
        @MethodSource("midEventArgumentSource")
        void shouldGenerateDocumentWithCorrectNameWhenOrderTypeIsValid(CaseDetails caseDetails, String fileName,
                                                                       DocmosisTemplates templateName) {
            final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

            verify(docmosisDocumentGeneratorService).generateDocmosisDocument(any(), eq(templateName));
            verify(uploadDocumentService).uploadPDF(userId, userAuthToken, pdf, fileName);

            final CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            assertThat(caseData.getOrderTypeAndDocument().getDocument()).isEqualTo(expectedDocument());
        }

        @Test
        void shouldNotGenerateOrderDocumentWhenOrderTypeIsCareOrderWithNoFurtherDirections() {
            postMidEvent(generateCareOrderCaseDetailsWithoutFurtherDirections());

            verify(docmosisDocumentGeneratorService, never()).generateDocmosisDocument(any(), any());
            verify(uploadDocumentService, never()).uploadPDF(any(), any(), any(), any());
        }

        @AfterEach
        void resetInvocations() {
            reset(docmosisDocumentGeneratorService);
            reset(uploadDocumentService);
        }

        private Stream<Arguments> midEventArgumentSource() {
            return Stream.of(
                Arguments.of(generateBlankOrderCaseDetails(), "blank_order_c21.pdf", ORDER),
                Arguments.of(generateCareOrderCaseDetailsWithFurtherDirections(), "care_order.pdf", ORDER),
                Arguments.of(generateEmergencyProtectionOrderCaseDetails(), "emergency_protection_order.pdf", EPO),
                Arguments.of(generateSupervisionOrderCaseDetails(), "supervision_order.pdf", ORDER)
            );
        }

        private CaseDetails generateEmergencyProtectionOrderCaseDetails() {
            final CaseData.CaseDataBuilder dataBuilder = CaseData.builder();

            dataBuilder.order(GeneratedOrder.builder().details("").build())
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(EMERGENCY_PROTECTION_ORDER).build());

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
            final CaseData.CaseDataBuilder dataBuilder = CaseData.builder();

            dataBuilder.order(GeneratedOrder.builder().details("").build())
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(BLANK_ORDER).build());

            generateDefaultValues(dataBuilder);

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseDetails generateCareOrderCaseDetailsWithFurtherDirections() {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonCareOrderDetails();

            dataBuilder.orderFurtherDirections(FurtherDirections.builder()
                .directionsNeeded("Yes")
                .directions("Some directions")
                .build());

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseDetails generateCareOrderCaseDetailsWithoutFurtherDirections() {
            final CaseData.CaseDataBuilder dataBuilder = generateCommonCareOrderDetails();

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
        }

        private CaseData.CaseDataBuilder generateCommonCareOrderDetails() {
            final CaseData.CaseDataBuilder builder = CaseData.builder()
                .orderTypeAndDocument(OrderTypeAndDocument.builder()
                    .type(CARE_ORDER)
                    .build());

            generateDefaultValues(builder);

            return builder;
        }

        private CaseDetails generateSupervisionOrderCaseDetails() {
            final CaseData.CaseDataBuilder dataBuilder = CaseData.builder()
                .orderTypeAndDocument(OrderTypeAndDocument.builder().type(SUPERVISION_ORDER).build())
                .orderFurtherDirections(FurtherDirections.builder()
                    .directionsNeeded("No")
                    .build())
                .orderMonths(5);

            generateDefaultValues(dataBuilder);

            return CaseDetails.builder()
                .data(mapper.convertValue(dataBuilder.build(), new TypeReference<>() {}))
                .build();
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

        private DocumentReference expectedDocument() {
            return DocumentReference.builder()
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .url(document.links.self.href)
                .build();
        }
    }
}
