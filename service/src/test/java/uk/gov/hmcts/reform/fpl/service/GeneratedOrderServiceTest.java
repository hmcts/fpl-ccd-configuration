package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.InterimOrderKey;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.docmosis.BlankOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CareOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.EPOGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.SupervisionOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FixedTimeConfiguration.class, GeneratedOrderService.class, JacksonAutoConfiguration.class
})
class GeneratedOrderServiceTest {

    @Autowired
    private Time time;

    @MockBean
    private BlankOrderGenerationService blankOrderGenerationService;
    @MockBean
    private CareOrderGenerationService careOrderGenerationService;
    @MockBean
    private SupervisionOrderGenerationService supervisionOrderGenerationService;
    @MockBean
    private EPOGenerationService epoGenerationService;

    @Autowired
    @InjectMocks
    private GeneratedOrderService service;

    @Nested
    class C21Tests {

        @Test
        void shouldReturnExpectedC21OrderWhenOrderTitleIsNull() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title(null)
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenC21OrderTitleIsEmptyString() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title("")
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenOrderTitleIsStringWithSpaceCharacter() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title(" ")
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenOrderTitlePresent() {
            GeneratedOrder order = GeneratedOrder.builder()
                .title("Example Title")
                .details("Some details")
                .document(DocumentReference.builder().build())
                .build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(BLANK_ORDER)
                    .document(DocumentReference.builder().build())
                    .build(),
                order, JudgeAndLegalAdvisor.builder().build(), time.now().toLocalDate(), null, null).getValue();

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Example Title");
        }

        private void assertCommonC21Fields(GeneratedOrder order) {
            assertThat(order.getType()).isEqualTo(BLANK_ORDER.getLabel());
            assertThat(order.getDocument()).isEqualTo(DocumentReference.builder().build());
            assertThat(order.getDetails()).isEqualTo("Some details");
            assertThat(order.getDate()).isNotNull();
            assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
        }
    }

    @Test
    void shouldReturnExpectedOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(CARE_ORDER)
                .subtype(FINAL)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(), time.now().toLocalDate(), null, null).getValue();

        assertThat(builtOrder.getType()).isEqualTo("Final care order");
        assertThat(builtOrder.getTitle()).isNull();
        assertThat(builtOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(builtOrder.getDate()).isNotNull();
        assertThat(builtOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldReturnEndOfProceedingsExpiryDateWhenInterimSubtypeAndEndOfProceedingsSelected(GeneratedOrderType type) {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(type)
                .subtype(INTERIM)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(), time.now().toLocalDate(), null,
            InterimEndDate.builder().type(END_OF_PROCEEDINGS).build()).getValue();

        assertThat(builtOrder.getExpiryDate()).isEqualTo("End of the proceedings");
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldReturnFormattedExpiryDateWhenInterimSubtypeAndNamedDateSelected(GeneratedOrderType type) {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(type)
                .subtype(INTERIM)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(),
            JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(),
            time.now().toLocalDate(),
            null,
            InterimEndDate.builder()
                .type(NAMED_DATE)
                .endDate(time.now().toLocalDate())
                .build())
            .getValue();

        assertThat(builtOrder.getExpiryDate())
            .isEqualTo(formatLocalDateToString(time.now().toLocalDate(), "'11:59pm', d MMMM y"));
    }

    @Test
    void shouldReturnExpectedSupervisionOrderWhenFinalSubtypeSelected() {
        GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(SUPERVISION_ORDER)
                .subtype(FINAL)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Dredd")
                .legalAdvisorName("Frank N. Stein")
                .build(), time.now().toLocalDate(), 5, null).getValue();

        final LocalDateTime orderExpiration = time.now().plusMonths(5);
        final String expectedExpiryDate = formatLocalDateTimeBaseUsingFormat(orderExpiration, "h:mma, d MMMM y");

        assertThat(builtOrder.getType()).isEqualTo("Final supervision order");
        assertThat(builtOrder.getExpiryDate()).isEqualTo(expectedExpiryDate);
    }

    @Test
    void shouldAddDocumentToOrderTypeAndDocumentObjectWhenDocumentExists() {
        Document document = document();

        OrderTypeAndDocument returnedTypeAndDoc = service.buildOrderTypeAndDocument(OrderTypeAndDocument.builder()
            .type(BLANK_ORDER).build(), document);

        assertThat(returnedTypeAndDoc.getDocument()).isEqualTo(DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build());
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void shouldGenerateCorrectFileNameGivenOrderType(GeneratedOrderType type,
        GeneratedOrderSubtype subtype,
        String expected) {
        final String fileName = service.generateOrderDocumentFileName(type, subtype);
        assertThat(fileName).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    @Test
    void shouldGetOrderTemplateDataForBlankOrderType() {
        CaseData caseData = CaseData.builder().orderTypeAndDocument(
            OrderTypeAndDocument.builder().type(BLANK_ORDER).build())
            .build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(blankOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForCareOrderType() {
        CaseData caseData = CaseData.builder().orderTypeAndDocument(
            OrderTypeAndDocument.builder().type(CARE_ORDER).build())
            .build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(careOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForEPOType() {
        CaseData caseData = CaseData.builder().orderTypeAndDocument(
            OrderTypeAndDocument.builder().type(EMERGENCY_PROTECTION_ORDER).build())
            .build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(epoGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForSupervisionOrderType() {
        CaseData caseData = CaseData.builder().orderTypeAndDocument(
            OrderTypeAndDocument.builder().type(SUPERVISION_ORDER).build())
            .build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(supervisionOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldReturnMostRecentUploadedOrderDocumentUrl() {
        DocumentReference lastOrderDocumentReference = DocumentReference.builder()
            .filename("C21 3.pdf")
            .url("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079")
            .binaryUrl("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary")
            .build();
        final DocumentReference mostRecentDocument = service.getMostRecentUploadedOrderDocument(createOrders(
            lastOrderDocumentReference));

        assertThat(mostRecentDocument.getFilename()).isEqualTo(lastOrderDocumentReference.getFilename());
        assertThat(mostRecentDocument.getUrl()).isEqualTo(lastOrderDocumentReference.getUrl());
        assertThat(mostRecentDocument.getBinaryUrl()).isEqualTo(lastOrderDocumentReference.getBinaryUrl());
    }

    @Test
    void shouldRemovePropertiesOnCaseDetailsUsedForOrderCapture() {
        Map<String, Object> data = stream(GeneratedOrderKey.values())
            .collect(toMap(GeneratedOrderKey::getKey, value -> ""));
        data.putAll(stream(GeneratedEPOKey.values()).collect(toMap(GeneratedEPOKey::getKey, value -> "")));
        data.putAll(stream(InterimOrderKey.values()).collect(toMap(InterimOrderKey::getKey, value -> "")));

        data.put("DO NOT REMOVE", "");
        service.removeOrderProperties(data);

        assertThat(data).containsOnlyKeys("DO NOT REMOVE");
    }

    private static Stream<Arguments> fileNameSource() {
        return Stream.of(
            Arguments.of(BLANK_ORDER, null, "blank_order_c21.pdf"),
            Arguments.of(CARE_ORDER, INTERIM, "interim_care_order.pdf"),
            Arguments.of(CARE_ORDER, FINAL, "final_care_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, INTERIM, "interim_supervision_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, FINAL, "final_supervision_order.pdf"),
            Arguments.of(EMERGENCY_PROTECTION_ORDER, null, "emergency_protection_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, null, "supervision_order.pdf"),
            Arguments.of(CARE_ORDER, null, "care_order.pdf")
        );
    }

}
