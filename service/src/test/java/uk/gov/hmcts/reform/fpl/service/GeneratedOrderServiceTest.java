package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
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

    @Nested
    class C21Tests {

        C21Tests() {
            //NO - OP
        }

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
                order, JudgeAndLegalAdvisor.builder().build()).getValue();

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
                order, JudgeAndLegalAdvisor.builder().build()).getValue();

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
                order, JudgeAndLegalAdvisor.builder().build()).getValue();

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
                order, JudgeAndLegalAdvisor.builder().build()).getValue();

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

    @Nested
    class CareOrderTests {

        CareOrderTests() {
            //NO - OP
        }

        @Test
        void shouldReturnExpectedTypeOfOrderWhenInterimCareOrderSelected() {
            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(CARE_ORDER)
                    .subtype(INTERIM)
                    .document(DocumentReference.builder().build())
                    .build(),
                GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder().build()).getValue();

            assertThat(builtOrder.getType()).isEqualTo("Interim care order");
        }

        @Test
        void shouldReturnExpectedCareOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
            GeneratedOrder builtOrder = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                    .type(CARE_ORDER)
                    .subtype(FINAL)
                    .document(DocumentReference.builder().build())
                    .build(),
                GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Judy")
                    .legalAdvisorName("Peter Parker")
                    .build()).getValue();

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
    void shouldGenerateCorrectFileNameGivenOrderType(OrderTypeAndDocument typeAndDoc, String expected) {
        final String fileName = service.generateOrderDocumentFileName(typeAndDoc);
        assertThat(fileName).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldCreateExpectedMapWhenGivenPopulatedCaseData(GeneratedOrderType orderType,
                                                           GeneratedOrderSubtype subtype) {
        LocalDateTime now = time.now();
        CaseData caseData = createPopulatedCaseData(orderType, subtype, now.toLocalDate());

        Map<String, Object> expectedMap = createExpectedDocmosisData(orderType, subtype, now);
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
        Map<String, Object> data = new HashMap<>();
        data.put("orderTypeAndDocument", "some data");
        data.put("order", "some data");
        data.put("judgeAndLegalAdvisor", "some data");
        data.put("orderFurtherDirections", "some data");

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .build();

        service.removeOrderProperties(caseDetails);
        assertThat(caseDetails.getData()).isEmpty();
    }

    private static Stream<Arguments> fileNameSource() {
        return Stream.of(
            Arguments.of(OrderTypeAndDocument.builder().type(BLANK_ORDER).build(), "blank_order_c21.pdf"),
            Arguments.of(OrderTypeAndDocument.builder().type(CARE_ORDER).subtype(INTERIM).build(),
                "interim_care_order.pdf"),
            Arguments.of(OrderTypeAndDocument.builder().type(CARE_ORDER).subtype(FINAL).build(), "care_order.pdf")
        );
    }

    private static Stream<Arguments> docmosisDataGenerationSource() {
        return Stream.of(
            Arguments.of(BLANK_ORDER, null),
            Arguments.of(CARE_ORDER, INTERIM),
            Arguments.of(CARE_ORDER, FINAL)
        );
    }

    private Map<String, Object> createExpectedDocmosisData(GeneratedOrderType type,
                                                           GeneratedOrderSubtype subtype,
                                                           LocalDateTime date) {
        ImmutableMap.Builder<String, Object> expectedMap = ImmutableMap.builder();
        String formattedDate = dateFormatterService.formatLocalDateToString(date.toLocalDate(), FormatStyle.LONG);

        switch (type) {
            case BLANK_ORDER:
                expectedMap
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", "Example Title")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", "Example details");
                break;

            case CARE_ORDER:
                if (subtype == INTERIM) {
                    expectedMap
                        .put("orderTitle", "Interim care order")
                        .put("childrenAct", "Section 38 Children Act 1989")
                        .put("orderDetails",
                            "It is ordered that the child is placed in the care of Example Local Authority"
                                + " until the end of the proceedings.");
                } else if (subtype == FINAL) {
                    expectedMap
                        .put("orderTitle", "Care order")
                        .put("childrenAct", "Section 31 Children Act 1989")
                        .put("orderDetails",
                            "It is ordered that the child is placed in the care of Example Local Authority.");
                }

                expectedMap
                    .put("orderType", CARE_ORDER);
                break;
            default:
        }

        expectedMap
            .put("furtherDirections", "Example Directions")
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

    private CaseData createPopulatedCaseData(GeneratedOrderType type,
                                             GeneratedOrderSubtype subtype,
                                             LocalDate localDate) {
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
                        .build())
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build());
                break;
            case CARE_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(CARE_ORDER)
                        .subtype(subtype)
                        .document(DocumentReference.builder().build())
                        .build())
                    .orderFurtherDirections(FurtherDirections.builder()
                        .directionsNeeded("Yes")
                        .directions("Example Directions")
                        .build());
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
