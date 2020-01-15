package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.GeneratedEPOKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
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
    void shouldAddDocumentToOrderTypeAndDocumentObjectWhenDocumentExists() throws IOException {
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

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenC21OrderTitleIsEmptyString() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title("")
            .details("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenOrderTitleIsStringWithSpaceCharacter() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title(" ")
            .details("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getTitle()).isEqualTo("Order");
    }

    @Test
    void shouldReturnExpectedOrderWhenOrderTitlePresent() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title("Example Title")
            .details("Some details")
            .document(DocumentReference.builder().build())
            .build();

        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            order, JudgeAndLegalAdvisor.builder().build());

        assertCommonC21Fields(returnedElement.getValue());
        assertThat(returnedElement.getValue().getTitle()).isEqualTo("Example Title");
    }

    @Test
    void shouldReturnExpectedOrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        Element<GeneratedOrder> returnedElement = service.buildCompleteOrder(OrderTypeAndDocument.builder()
                .type(CARE_ORDER)
                .document(DocumentReference.builder().build())
                .build(),
            GeneratedOrder.builder().build(), JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .legalAdvisorName("Peter Parker")
                .build());

        assertThat(returnedElement.getValue().getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedElement.getValue().getDate()).isNotNull();
        assertThat(returnedElement.getValue().getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());
    }

    @Test
    void shouldGenerateCorrectFileNameWhenGivenC21OrderType() {
        OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
            .type(BLANK_ORDER)
            .document(DocumentReference.builder().build()).build();

        assertThat(service.generateOrderDocumentFileName(typeAndDocument.getType().getLabel())).isEqualTo(
            formatTypeToFileName(BLANK_ORDER.getLabel()));
    }

    @Test
    void shouldGenerateCorrectFileNameWhenGivenCareOrderType() {
        OrderTypeAndDocument typeAndDocument = OrderTypeAndDocument.builder()
            .type(CARE_ORDER)
            .document(DocumentReference.builder().build()).build();

        assertThat(service.generateOrderDocumentFileName(typeAndDocument.getType().getLabel())).isEqualTo(
            formatTypeToFileName(CARE_ORDER.getLabel()));
    }

    @Nested
    class TemplateDataTests {
        LocalDate localDate = time.now().toLocalDate();
        String date = dateFormatterService.formatLocalDateToString(localDate, FormatStyle.LONG);

        TemplateDataTests() {
            //NO - OP
        }

        @Test
        void shouldCreateExpectedMapForC21OrderWhenGivenPopulatedCaseData() {
            CaseData caseData = createPopulatedCaseData(BLANK_ORDER, localDate);

            Map<String, Object> expectedMap = createExpectedOrderData(date, BLANK_ORDER);
            Map<String, Object> templateData = service.getOrderTemplateData(caseData);

            assertThat(templateData).isEqualTo(expectedMap);
        }

        @Test
        void shouldCreateExpectedMapForCareOrderWhenGivenPopulatedCaseData() {
            CaseData caseData = createPopulatedCaseData(CARE_ORDER, localDate);

            Map<String, Object> expectedMap = createExpectedOrderData(date, CARE_ORDER);
            Map<String, Object> templateData = service.getOrderTemplateData(caseData);

            assertThat(templateData).isEqualTo(expectedMap);
        }

        @Test
        void shouldCreateExpectedMapForEmergencyProtectionOrderWhenGivenPopulatedCaseData() {
            CaseData caseData = createPopulatedCaseData(EMERGENCY_PROTECTION_ORDER, localDate);

            Map<String, Object> expectedMap = createExpectedOrderData(date, EMERGENCY_PROTECTION_ORDER);
            Map<String, Object> templateData = service.getOrderTemplateData(caseData);

            assertThat(templateData).isEqualTo(expectedMap);
        }
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
    void shouldRemovePropertiesOnCaseDetailsUsedForOrderCapture() {
        CaseDetails caseDetails = createPopulatedCaseDetails();
        service.removeOrderProperties(caseDetails.getData());

        Arrays.asList(GeneratedEPOKey.values(), GeneratedOrderKey.values())
            .forEach(key -> assertThat(caseDetails.getData().get(key)).isNull());
    }

    private void assertCommonC21Fields(GeneratedOrder order) {
        assertThat(order.getType()).isEqualTo(BLANK_ORDER);
        assertThat(order.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(order.getDetails()).isEqualTo("Some details");
        assertThat(order.getDate()).isNotNull();
        assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createExpectedOrderData(String date, GeneratedOrderType type) {
        ImmutableMap.Builder expectedMap = ImmutableMap.<String, Object>builder();

        switch (type) {
            case BLANK_ORDER:
                expectedMap
                    .put("orderType", BLANK_ORDER)
                    .put("orderTitle", "Example Title")
                    .put("childrenAct", "Children Act 1989")
                    .put("orderDetails", "Example details");
                break;
            case CARE_ORDER:
                expectedMap
                    .put("orderType", CARE_ORDER)
                    .put("orderTitle", "Care order")
                    .put("childrenAct", "Section 31 Children Act 1989")
                    .put("orderDetails",
                        "It is ordered that the child is placed in the care of Example Local Authority.");
                break;
            case EMERGENCY_PROTECTION_ORDER:
                expectedMap
                    .put("orderType", EMERGENCY_PROTECTION_ORDER)
                    .put("localAuthorityName", "Example Local Authority")
                    .put("childrenDescription", "Test description")
                    .put("epoType", REMOVE_TO_ACCOMMODATION)
                    .put("includePhrase", "Yes")
                    .put("removalAddress", "1 Main Street, Lurgan, BT66 7PP, Armagh, United Kingdom")
                    .put("childrenCount", 1)
                    .put("epoStartDateTime", dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                        "d MMMM yyyy 'at' h:mma"))
                    .put("epoEndDateTime",  dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(),
                        "d MMMM yyyy 'at' h:mma"));
                break;
            default:
        }

        expectedMap
            .put("furtherDirections", "Example Directions")
            .put("familyManCaseNumber", "123")
            .put("courtName", "Family Court")
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
                        .build());
                break;
            case EMERGENCY_PROTECTION_ORDER:
                caseDataBuilder
                    .orderTypeAndDocument(OrderTypeAndDocument.builder()
                        .type(EMERGENCY_PROTECTION_ORDER)
                        .document(DocumentReference.builder().build())
                        .build())
                    .epoChildren(EPOChildren.builder()
                        .descriptionNeeded("Yes")
                        .description("Test description")
                        .build())
                    .epoEndDate(time.now())
                    .epoPhrase(EPOPhrase.builder()
                        .includePhrase("Yes")
                        .build())
                    .epoType(REMOVE_TO_ACCOMMODATION)
                    .epoRemovalAddress(Address.builder()
                        .addressLine1("1 Main Street")
                        .addressLine2("Lurgan")
                        .postTown("BT66 7PP")
                        .county("Armagh")
                        .country("United Kingdom")
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
            .orderFurtherDirections(FurtherDirections.builder()
                .directionsNeeded("Yes")
                .directions("Example Directions")
                .build())
            .build();

        return caseDataBuilder.build();
    }

    private CaseDetails createPopulatedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("orderTypeAndDocument", "");
        caseData.put("order", "");
        caseData.put("judgeAndLegalAdvisor", "");
        caseData.put("EPOChildren", "");
        caseData.put("epoEndDate", "");
        caseData.put("epoPhrase", "");
        caseData.put("epoType", "");

        return CaseDetails.builder().data(caseData).build();
    }

    private String formatTypeToFileName(String type) {
        return type.toLowerCase().replaceAll("[()]", "").replaceAll("[ ]", "_") + ".pdf";
    }
}
