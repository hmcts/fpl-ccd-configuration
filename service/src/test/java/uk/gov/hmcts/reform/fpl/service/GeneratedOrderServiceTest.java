package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.BlankOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CareOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DischargeCareOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.EPOGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.SupervisionOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudgeAndLegalAdviser;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FixedTimeConfiguration.class, GeneratedOrderService.class, JacksonAutoConfiguration.class, LookupTestConfig.class
})
class GeneratedOrderServiceTest {
    private static final List<Element<Child>> CHILDREN = List.of(testChild(), testChild());

    @Autowired
    private Time time;
    @MockBean
    private ChildrenService childrenService;
    @MockBean
    private BlankOrderGenerationService blankOrderGenerationService;
    @MockBean
    private CareOrderGenerationService careOrderGenerationService;
    @MockBean
    private SupervisionOrderGenerationService supervisionOrderGenerationService;
    @MockBean
    private EPOGenerationService epoGenerationService;
    @MockBean
    private DischargeCareOrderGenerationService dischargeCareOrderGenerationService;
    @MockBean
    private DischargeCareOrderService dischargeCareOrderService;

    @Autowired
    @InjectMocks
    private GeneratedOrderService service;
    private DocumentReference testDocumentReference = testDocumentReference();

    @Nested
    class C21Tests {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnExpectedC21OrderWhenOrderTitleIsNullOrBlank(String orderTitle) {
            GeneratedOrder generatedOrder = order().title(orderTitle).build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(
                orderTypeAndDocument(BLANK_ORDER),
                testJudgeAndLegalAdviser(),
                caseData().order(generatedOrder).build());

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo("Order");
        }

        @Test
        void shouldReturnExpectedOrderWhenOrderTitlePresent() {
            GeneratedOrder generatedOrder = order().title("Example Title").build();

            GeneratedOrder builtOrder = service.buildCompleteOrder(
                orderTypeAndDocument(BLANK_ORDER),
                testJudgeAndLegalAdviser(),
                caseData().order(generatedOrder).build());

            assertCommonC21Fields(builtOrder);
            assertThat(builtOrder.getTitle()).isEqualTo(generatedOrder.getTitle());
        }

        private void assertCommonC21Fields(GeneratedOrder order) {
            assertThat(order.getType()).isEqualTo(BLANK_ORDER.getLabel());
            assertThat(order.getDocument()).isEqualTo(testDocumentReference);
            assertThat(order.getDetails()).isEqualTo("Some details");
            assertThat(order.getDate()).isNotNull();
            assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(testJudgeAndLegalAdviser());
        }
    }

    @Nested
    class ShowCloseCasePage {

        @BeforeEach
        void init() {
            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(true);
        }

        @ParameterizedTest
        @ArgumentsSource(GeneratedCareOrderProvider.class)
        void shouldReturnFalseWhenNotAllChildrenHaveFinalOrder(GeneratedOrderType type, GeneratedOrderSubtype subtype) {
            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(false);
            assertThat(service.showCloseCase(orderTypeAndDocument(type, subtype), CHILDREN, true)).isFalse();
        }

        @ParameterizedTest
        @ArgumentsSource(GeneratedCareOrderProvider.class)
        void shouldReturnFalseWhenClosingCaseIsNotEnabled(GeneratedOrderType type, GeneratedOrderSubtype subtype) {
            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(true);
            assertThat(service.showCloseCase(orderTypeAndDocument(type, subtype), CHILDREN, false)).isFalse();
        }

        @ParameterizedTest
        @ArgumentsSource(NotCloseableGeneratedCareOrderProvider.class)
        void shouldReturnFalseWhenOrderIsCloseable(GeneratedOrderType type, GeneratedOrderSubtype subtype) {
            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(true);
            assertThat(service.showCloseCase(orderTypeAndDocument(type, subtype), CHILDREN, true)).isFalse();
        }

        @ParameterizedTest
        @ArgumentsSource(CloseableGeneratedCareOrderProvider.class)
        void shouldReturnTrueWhenOrderIsCloseable(GeneratedOrderType type, GeneratedOrderSubtype subtype) {
            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(true);
            assertThat(service.showCloseCase(orderTypeAndDocument(type, subtype), CHILDREN, true)).isTrue();
        }

    }

    @Nested
    class ShouldGenerateDocument {
        @Test
        void shouldReturnTrueWhenOrderIsBlankOrder() {
            OrderTypeAndDocument typeAndDocument = orderTypeAndDocument(BLANK_ORDER);

            boolean shouldGenerateDocument = service.shouldGenerateDocument(typeAndDocument, null);

            assertThat(shouldGenerateDocument).isTrue();
        }

        @Test
        void shouldReturnTrueWhenOrderIsNotBlankOrderAndFurtherDirectionPresent() {
            OrderTypeAndDocument typeAndDocument = orderTypeAndDocument(CARE_ORDER);

            boolean shouldGenerateDocument = service.shouldGenerateDocument(typeAndDocument,
                FurtherDirections.builder().build());

            assertThat(shouldGenerateDocument).isTrue();
        }
    }

    @Nested
    class IsFinalOrderAllowed {

        @ParameterizedTest
        @ArgumentsSource(CloseableGeneratedCareOrderProvider.class)
        void shouldNotAllowFinalOrderWhenAllChildrenHaveFinalOrderIssuedAndOrderIsClosable(
            GeneratedOrderType type, GeneratedOrderSubtype subtype) {

            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(true);

            boolean isFinalOrderAllowed = service.isFinalOrderAllowed(orderTypeAndDocument(type, subtype), CHILDREN);

            assertThat(isFinalOrderAllowed).isFalse();
        }

        @ParameterizedTest
        @ArgumentsSource(CloseableGeneratedCareOrderProvider.class)
        void shouldAllowFinalOrderWhenNotEveryChildHasFinalOrderIssuedAndOrderIsClosable(
            GeneratedOrderType type, GeneratedOrderSubtype subtype) {

            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(false);

            boolean isFinalOrderAllowed = service.isFinalOrderAllowed(orderTypeAndDocument(type, subtype), CHILDREN);

            assertThat(isFinalOrderAllowed).isTrue();
        }

        @ParameterizedTest
        @ArgumentsSource(NotCloseableGeneratedCareOrderProvider.class)
        void shouldAllowFinalOrderWhenOrderIsNotClosableAndNotEveryChildHasFinalOrderIssued(
            GeneratedOrderType type, GeneratedOrderSubtype subtype) {

            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(false);

            boolean isFinalOrderAllowed = service.isFinalOrderAllowed(orderTypeAndDocument(type, subtype), CHILDREN);

            assertThat(isFinalOrderAllowed).isTrue();
        }

        @ParameterizedTest
        @ArgumentsSource(NotCloseableGeneratedCareOrderProvider.class)
        void shouldAllowFinalOrderWhenOrderIsNotClosableAndAllChildrenHaveFinalOrderIssued(
            GeneratedOrderType type, GeneratedOrderSubtype subtype) {

            when(childrenService.allChildrenHaveFinalOrder(CHILDREN)).thenReturn(true);

            boolean isFinalOrderAllowed = service.isFinalOrderAllowed(orderTypeAndDocument(type, subtype), CHILDREN);

            assertThat(isFinalOrderAllowed).isTrue();
        }
    }

    @Test
    void shouldReturnExpectedOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        final OrderTypeAndDocument orderTypeAndDocument = orderTypeAndDocument(CARE_ORDER, FINAL);
        final JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();

        GeneratedOrder builtOrder = service.buildCompleteOrder(
            orderTypeAndDocument,
            judgeAndLegalAdvisor,
            caseData().build());

        assertThat(builtOrder.getType()).isEqualTo("Final care order");
        assertThat(builtOrder.getTitle()).isNull();
        assertThat(builtOrder.getDocument()).isEqualTo(testDocumentReference);
        assertThat(builtOrder.getDate()).isNotNull();
        assertThat(builtOrder.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldReturnEndOfProceedingsExpiryDateWhenInterimSubtypeAndEndOfProceedingsSelected(GeneratedOrderType type) {
        final OrderTypeAndDocument orderTypeAndDocument = orderTypeAndDocument(type, INTERIM);
        final JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        final CaseData caseData = caseData()
            .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build())
            .build();

        final GeneratedOrder builtOrder = service.buildCompleteOrder(orderTypeAndDocument, judgeAndLegalAdvisor,
            caseData);

        assertThat(builtOrder.getExpiryDate()).isEqualTo("End of the proceedings");
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldReturnFormattedExpiryDateWhenInterimSubtypeAndNamedDateSelected(GeneratedOrderType type) {
        final OrderTypeAndDocument orderTypeAndDocument = orderTypeAndDocument(type, INTERIM);
        final JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        final CaseData caseData = caseData()
            .interimEndDate(InterimEndDate.builder()
                .type(NAMED_DATE)
                .endDate(time.now().toLocalDate())
                .build())
            .build();

        GeneratedOrder builtOrder = service.buildCompleteOrder(orderTypeAndDocument, judgeAndLegalAdvisor, caseData);

        assertThat(builtOrder.getExpiryDate())
            .isEqualTo(formatLocalDateToString(time.now().toLocalDate(), "'11:59pm', d MMMM y"));
    }

    @Test
    void shouldReturnExpectedSupervisionOrderWhenFinalSubtypeSelected() {
        final OrderTypeAndDocument orderTypeAndDocument = orderTypeAndDocument(SUPERVISION_ORDER, FINAL);
        final JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        final CaseData caseData = caseData()
            .orderMonths(5)
            .build();

        GeneratedOrder builtOrder = service.buildCompleteOrder(orderTypeAndDocument, judgeAndLegalAdvisor, caseData);

        final LocalDateTime orderExpiration = time.now().plusMonths(5);
        final String expectedExpiryDate = formatLocalDateTimeBaseUsingFormat(orderExpiration, "h:mma, d MMMM y");

        assertThat(builtOrder.getType()).isEqualTo("Final supervision order");
        assertThat(builtOrder.getExpiryDate()).isEqualTo(expectedExpiryDate);
    }

    @Test
    void shouldEnhanceOrder() {
        final OrderTypeAndDocument orderTypeAndDocument = orderTypeAndDocument(CARE_ORDER, FINAL);
        final JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        final CaseData caseData = caseData().build();

        when(childrenService.getSelectedChildren(caseData)).thenReturn(CHILDREN);

        GeneratedOrder builtOrder = service.buildCompleteOrder(orderTypeAndDocument, judgeAndLegalAdvisor, caseData);

        assertThat(builtOrder.getDateOfIssue()).isEqualTo(formatLocalDateToString(time.now().toLocalDate(), DATE));
        assertThat(builtOrder.getDate()).isEqualTo(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE));
        assertThat(builtOrder.getType()).isEqualTo("Final care order");
        assertThat(builtOrder.getCourtName()).isEqualTo("Family Court");
        assertThat(builtOrder.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
        assertThat(builtOrder.getChildren()).isEqualTo(CHILDREN);
    }

    @Test
    void shouldAddDocumentToOrderTypeAndDocumentObjectWhenDocumentExists() {
        Document document = document();
        OrderTypeAndDocument order = orderTypeAndDocument(BLANK_ORDER);
        OrderTypeAndDocument returnedTypeAndDoc = service.buildOrderTypeAndDocument(order, document);

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

    @Test
    void shouldGetOrderTemplateDataForBlankOrderType() {
        CaseData caseData = caseData().orderTypeAndDocument(orderTypeAndDocument(BLANK_ORDER)).build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(blankOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForCareOrderType() {
        CaseData caseData = caseData().orderTypeAndDocument(orderTypeAndDocument(CARE_ORDER, INTERIM)).build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(careOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForEPOType() {
        CaseData caseData = caseData().orderTypeAndDocument(orderTypeAndDocument(EMERGENCY_PROTECTION_ORDER)).build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(epoGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForSupervisionOrderType() {
        CaseData caseData = caseData().orderTypeAndDocument(orderTypeAndDocument(SUPERVISION_ORDER)).build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(supervisionOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

        DocmosisGeneratedOrder result = service.getOrderTemplateData(caseData);

        assertThat(result).isEqualTo(docmosisGeneratedOrder);
    }

    @Test
    void shouldGetOrderTemplateDataForDischargeCareOrderType() {
        CaseData caseData = caseData().orderTypeAndDocument(orderTypeAndDocument(DISCHARGE_OF_CARE_ORDER)).build();

        DocmosisGeneratedOrder docmosisGeneratedOrder = DocmosisGeneratedOrder.builder().build();
        given(dischargeCareOrderGenerationService.getTemplateData(caseData)).willReturn(docmosisGeneratedOrder);

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
            Arguments.of(DISCHARGE_OF_CARE_ORDER, null, "discharge_of_care_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, INTERIM, "interim_supervision_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, FINAL, "final_supervision_order.pdf"),
            Arguments.of(EMERGENCY_PROTECTION_ORDER, null, "emergency_protection_order.pdf"),
            Arguments.of(SUPERVISION_ORDER, null, "supervision_order.pdf"),
            Arguments.of(CARE_ORDER, null, "care_order.pdf")
        );
    }

    private OrderTypeAndDocument orderTypeAndDocument(GeneratedOrderType type, GeneratedOrderSubtype subtype) {
        return OrderTypeAndDocument.builder()
            .type(type)
            .subtype(subtype)
            .document(testDocumentReference)
            .build();
    }

    private OrderTypeAndDocument orderTypeAndDocument(GeneratedOrderType type) {
        return orderTypeAndDocument(type, null);
    }

    private CaseData.CaseDataBuilder caseData() {
        return CaseData.builder()
            .order(GeneratedOrder.builder()
                .title(null)
                .details("Some details")
                .document(testDocumentReference).build())
            .dateOfIssue(time.now().toLocalDate())
            .orderMonths(null)
            .interimEndDate(null)
            .caseLocalAuthority("example");
    }

    private GeneratedOrder.GeneratedOrderBuilder order() {
        return GeneratedOrder.builder()
            .title(null)
            .details("Some details")
            .document(testDocumentReference);
    }

    private static class NotCloseableGeneratedCareOrderProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(BLANK_ORDER, null),
                Arguments.of(CARE_ORDER, INTERIM),
                Arguments.of(SUPERVISION_ORDER, INTERIM));
        }
    }

    private static class CloseableGeneratedCareOrderProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(CARE_ORDER, FINAL),
                Arguments.of(SUPERVISION_ORDER, FINAL),
                Arguments.of(EMERGENCY_PROTECTION_ORDER, null),
                Arguments.of(DISCHARGE_OF_CARE_ORDER, null));
        }
    }

    private static class GeneratedCareOrderProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.concat(
                new NotCloseableGeneratedCareOrderProvider().provideArguments(context),
                new CloseableGeneratedCareOrderProvider().provideArguments(context));
        }
    }
}
