package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class GeneratedOrderServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String COURT_CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME,
        COURT_EMAIL);
    private static final String LA_NAME_CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_NAME);
    private static final LocalDateTime NOW = LocalDateTime.now();

    private final Time time = () -> NOW;

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(
        COURT_CONFIG);
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration =
        new LocalAuthorityNameLookupConfiguration(LA_NAME_CONFIG);

    private GeneratedOrderService service;

    @BeforeEach
    void setup() {
        this.service = new GeneratedOrderService(dateFormatterService, hmctsCourtLookupConfiguration,
            localAuthorityNameLookupConfiguration, time);
    }

    @Test
    void shouldAddDocumentToOrderTypeAndDocumentObjectWhenDocumentExists() throws IOException {
        Document document = document();

        OrderTypeAndDocument returnedTypeAndDoc = service.updateTypeAndDocument(OrderTypeAndDocument.builder()
            .orderType(BLANK_ORDER).build(), document);

        assertThat(returnedTypeAndDoc.getDocument()).isEqualTo(DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build());
    }

    @Test
    void shouldReturnExpectedC21OrderWhenOrderTitleIsNull() {
        GeneratedOrder order = GeneratedOrder.builder()
            .orderTitle(null)
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .orderType(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getOrderTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenC21OrderTitleIsEmptyString() {
        GeneratedOrder order = GeneratedOrder.builder()
            .orderTitle("")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .orderType(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getOrderTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenOrderTitleIsStringWithSpaceCharacter() {
        GeneratedOrder order = GeneratedOrder.builder()
            .orderTitle(" ")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .orderType(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getOrderTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenOrderTitlePresent() {
        GeneratedOrder order = GeneratedOrder.builder()
            .orderTitle("Example Title")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .orderType(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getOrderTitle()).isEqualTo("Example Title");
    }

    @Test
    void shouldReturnExpectedOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .orderType(CARE_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build());

        assertThat(returnedElement.getValue().getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedElement.getValue().getOrderDate()).isNotNull();
        assertThat(returnedElement.getValue().getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());
    }

    @Test
    void shouldGenerateCorrectFileNameWhenGivenC21OrderType() {
        OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
            .orderType(BLANK_ORDER)
            .document(DocumentReference.builder().build()).build();

        assertThat(service.generateDocumentFileName(typeAndDocument)).isEqualTo(
            BLANK_ORDER.getType().replaceAll("[()]", "") + ".pdf");
    }

    @Test
    void shouldGenerateCorrectFileNameWhenGivenCareOrderType() {
        OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
            .orderType(CARE_ORDER)
            .document(DocumentReference.builder().build()).build();

        assertThat(service.generateDocumentFileName(typeAndDocument)).isEqualTo(CARE_ORDER.getType() + ".pdf");
    }


    @Nested
    class TemplateDataTests {
        LocalDate localDate = LocalDate.now();
        String date = dateFormatterService.formatLocalDateToString(localDate, FormatStyle.LONG);

        TemplateDataTests() throws IOException {
            //NO - OP
        }

        @Test
        void shouldCreateExpectedMapForC21OrderWhenGivenPopulatedCaseData() {
            CaseData caseData = populatedCaseData(BLANK_ORDER, localDate);

            Map<String, Object> expectedMap = expectedData(date, BLANK_ORDER);
            Map<String, Object> templateData = service.getOrderTemplateData(caseData);

            assertThat(templateData).isEqualTo(expectedMap);
        }

        @Test
        void shouldCreateExpectedMapForCareOrderWhenGivenPopulatedCaseData() {
            CaseData caseData = populatedCaseData(CARE_ORDER, localDate);

            Map<String, Object> expectedMap = expectedData(date, CARE_ORDER);
            Map<String, Object> templateData = service.getOrderTemplateData(caseData);

            assertThat(templateData).isEqualTo(expectedMap);
        }
    }

    @Test
    void shouldReturnMostRecentUploadedOrderDocumentUrl() {
        final String expectedMostRecentUploadedOrderDocumentUrl =
            "http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";
        final String returnedMostRecentUploadedOrderDocumentUrl = service.mostRecentUploadedOrderDocumentUrl(
            createOrders());

        assertThat(expectedMostRecentUploadedOrderDocumentUrl).isEqualTo(
            returnedMostRecentUploadedOrderDocumentUrl);
    }

    private void assertCommonC21Fields(GeneratedOrder order) {
        assertThat(order.getType()).isEqualTo(BLANK_ORDER);
        assertThat(order.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(order.getOrderDetails()).isEqualTo("Some details");
        assertThat(order.getOrderDate()).isNotNull();
        assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> expectedData(String date, GeneratedOrderType orderType) {
        ImmutableMap.Builder expectedMap = ImmutableMap.<String, Object>builder();

        switch (orderType) {
            case BLANK_ORDER:
                expectedMap
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", "Example Title")
                    .put("childrenAct", "Section 31 Children Act 1989")
                    .put("orderDetails", "Example details");
                break;
            case CARE_ORDER:
                expectedMap
                    .put("orderType", CARE_ORDER)
                    .put("orderTitle", "Care Order")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails",
                        "It is ordered that the child is placed in the care of Example Local Authority.");
                break;
            default:
        }

        expectedMap
            .put("familyManCaseNumber", "123")
            .put("courtName", "Example Court")
            .put("todaysDate", date)
            .put("judgeTitleAndName", "Her Honour Judge Judy")
            .put("legalAdvisorName", "Peter Parker")
            .put("children", ImmutableList.of(
                ImmutableMap.of(
                    "name", "Timmy Jones",
                    "gender", "Boy",
                    "dateOfBirth", date)));

        return expectedMap.build();
    }

    private CaseData populatedCaseData(GeneratedOrderType orderType, LocalDate localDate) {
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();

        switch (orderType) {
            case BLANK_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .orderType(BLANK_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .order(GeneratedOrder.builder()
                        .orderTitle("Example Title")
                        .orderDetails("Example details")
                        .build());
                break;
            case CARE_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .orderType(CARE_ORDER)
                        .document(DocumentReference.builder().build())
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
