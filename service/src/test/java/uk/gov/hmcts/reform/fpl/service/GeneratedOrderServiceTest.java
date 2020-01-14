package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FixedTimeConfiguration.class, LookupTestConfig.class, DateFormatterService.class, GeneratedOrderService.class
})
class GeneratedOrderServiceTest {
    @Autowired
    private Time time;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private GeneratedOrderService service;

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

    @Test
    void shouldReturnExpectedC21OrderWhenOrderTitleIsNull() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title(null)
            .details("Some details")
            .document(DocumentReference.builder().build())
            .build();

        GeneratedOrder builtOrder = service.buildCompleteOrder(
            OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order,
            JudgeAndLegalAdvisor.builder().build(),
            null).getValue();

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

        GeneratedOrder returnedElement = service.buildCompleteOrder(
            OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order,
            JudgeAndLegalAdvisor.builder().build(),
            null).getValue();

        assertCommonC21Fields(returnedElement);
        assertThat(returnedElement.getTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenOrderTitleIsStringWithSpaceCharacter() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title(" ")
            .details("Some details")
            .document(DocumentReference.builder().build())
            .build();

        GeneratedOrder builtOrder = service.buildCompleteOrder(
            OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order,
            JudgeAndLegalAdvisor.builder().build(),
            null).getValue();

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

        GeneratedOrder builtOrder = service.buildCompleteOrder(
            OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order,
            JudgeAndLegalAdvisor.builder().build(),
            null).getValue();

        assertCommonC21Fields(builtOrder);
        assertThat(builtOrder.getTitle()).isEqualTo("Example Title");
    }

    @Test
    void shouldReturnExpectedOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        GeneratedOrder builtOrder = service.buildCompleteOrder(
            OrderTypeAndDocument.builder()
                .type(CARE_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(),
            JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build(),
            null).getValue();

        assertThat(builtOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(builtOrder.getDate()).isNotNull();
        assertThat(builtOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());
    }

    @Test
    void shouldReturnExpectedOrderWhenGivenNumberOfMonthsOnSupervisionOrder() {
        GeneratedOrder builtOrder = service.buildCompleteOrder(
            OrderTypeAndDocument.builder()
                .type(SUPERVISION_ORDER)
                .build(),
            GeneratedOrder.builder().build(),
            JudgeAndLegalAdvisor.builder().build(),
            5).getValue();

        String expected = dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now().plusMonths(5),
            "h:mma, d MMMM y");

        assertThat(builtOrder.getExpiryDate()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource(value = "fileNameSource")
    void shouldGenerateCorrectFileNameGivenOrderType(OrderTypeAndDocument typeAndDoc, String expected) {
        final String fileName = service.generateOrderDocumentFileName(typeAndDoc.getType().getLabel());
        assertThat(fileName).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = GeneratedOrderType.class, names = {"BLANK_ORDER", "CARE_ORDER", "SUPERVISION_ORDER"})
    void shouldCreateExpectedMapWhenGivenPopulatedCaseData(GeneratedOrderType orderType) {
        LocalDateTime now = time.now();
        CaseData caseData = createPopulatedCaseData(orderType, now.toLocalDate());

        Map<String, Object> expectedMap = createExpectedDocmosisData(orderType, now);
        Map<String, Object> templateData = service.getOrderTemplateData(caseData);

        assertThat(templateData).isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnMostRecentUploadedOrderDocumentUrl() {
        final String expectedMostRecentUploadedOrderDocumentUrl =
            "http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";
        final String returnedMostRecentUploadedOrderDocumentUrl = service.getMostRecentUploadedOrderDocumentUrl(
            createOrders());

        assertThat(expectedMostRecentUploadedOrderDocumentUrl).isEqualTo(
            returnedMostRecentUploadedOrderDocumentUrl);
    }

    @Test
    void shouldRemoveOrderPropertiesWhenTheyExistInCaseDetails() {
        Map<String, Object> data = Arrays.stream(GeneratedOrderKey.values())
            .collect(Collectors.toMap(GeneratedOrderKey::getKey, value -> ""));

        service.removeOrderProperties(data);

        assertThat(data).isEmpty();
    }

    private static Stream<Arguments> fileNameSource() {
        return Stream.of(
            Arguments.of(OrderTypeAndDocument.builder().type(BLANK_ORDER).build(), "blank_order_c21.pdf"),
            Arguments.of(OrderTypeAndDocument.builder().type(CARE_ORDER).build(), "care_order.pdf"),
            Arguments.of(OrderTypeAndDocument.builder().type(SUPERVISION_ORDER).build(), "supervision_order.pdf")
        );
    }

    private void assertCommonC21Fields(GeneratedOrder order) {
        assertThat(order.getType()).isEqualTo(BLANK_ORDER);
        assertThat(order.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(order.getDetails()).isEqualTo("Some details");
        assertThat(order.getDate()).isNotNull();
        assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    private Map<String, Object> createExpectedDocmosisData(GeneratedOrderType type, LocalDateTime date) {
        ImmutableMap.Builder<String, Object> expectedMap = ImmutableMap.builder();
        String formattedDate = dateFormatterService.formatLocalDateToString(date.toLocalDate(), FormatStyle.LONG);

        switch (type) {
            case BLANK_ORDER:
                expectedMap
                    .put("orderTitle", "Example Title")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", "Example details");
                break;
            case CARE_ORDER:
                expectedMap
                    .put("orderTitle", "Care order")
                    .put("childrenAct", "Section 31 Children Act 1989")
                    .put("orderDetails",
                        "It is ordered that the child is placed in the care of Example Local Authority.");
                break;
            case SUPERVISION_ORDER:
                final String suffix = dateFormatterService.getDayOfMonthSuffix(date.getDayOfMonth());
                final String formattedDateTime =
                    dateFormatterService.formatLocalDateTimeBaseUsingFormat(date.plusMonths(5),
                        "h:mma 'on the' d'" + suffix + "' MMMM y");
                expectedMap
                    .put("orderTitle", "Supervision order")
                    .put("childrenAct", "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                    .put("orderDetails",
                        String.format(
                            "It is ordered that Example Local Authority supervises the child for 5 months from the "
                                + "date of this order until %s.", formattedDateTime));
                break;
            default:
        }

        expectedMap
            .put("orderType", type)
            .put("furtherDirections", (type != BLANK_ORDER) ? "Example Directions" : "")
            .put("familyManCaseNumber", "123")
            .put("courtName", "Family Court")
            .put("todaysDate", formattedDate)
            .put("judgeTitleAndName", "Her Honour Judge Judy")
            .put("legalAdvisorName", "Peter Parker")
            .put("children", ImmutableList.of(
                ImmutableMap.of(
                    "name", "Timmy Jones",
                    "gender", "Boy",
                    "dateOfBirth", formattedDate)));

        return expectedMap.build();
    }

    private CaseData createPopulatedCaseData(GeneratedOrderType type, LocalDate localDate) {
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();

        switch (type) {
            case BLANK_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(BLANK_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .order(GeneratedOrder.builder()
                        .title("Example Title")
                        .details("Example details")
                        .build());
                break;
            case CARE_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(CARE_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build());
                break;
            case SUPERVISION_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(SUPERVISION_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build())
                    .orderMonths(5);
                break;
            default:
        }

        caseDataBuilder
            .familyManCaseNumber("123")
            .caseLocalAuthority("example")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build())
            .children1(ImmutableList.of(Element.<Child>builder()
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Timmy")
                        .lastName("Jones")
                        .gender("Boy")
                        .dateOfBirth(localDate)
                        .build())
                    .build())
                .build()))
            .build();

        return caseDataBuilder.build();
    }
}
