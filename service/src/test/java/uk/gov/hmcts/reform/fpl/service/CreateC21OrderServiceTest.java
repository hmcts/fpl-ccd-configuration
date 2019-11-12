package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class CreateC21OrderServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);
    private static final LocalDateTime NOW = LocalDateTime.now();

    private final Time time = () -> NOW;

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);

    private CreateC21OrderService service;

    @BeforeEach
    void setup() {
        this.service = new CreateC21OrderService(dateFormatterService, hmctsCourtLookupConfiguration, time);
    }

    @Test
    void shouldAddDocumentToEmptyC21WhenDocumentExists() throws IOException {
        Document document = document();

        C21Order returnedOrder = service.addDocumentToC21(C21Order.builder().build(), document);

        assertThat(returnedOrder).hasFieldOrPropertyWithValue("document", DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build());
    }

    @Test
    void shouldAddDocumentToPopulatedC21WhenDocumentExists() throws IOException {
        Document document = document();
        C21Order.C21OrderBuilder order = C21Order.builder()
            .orderTitle("Order")
            .orderDetails("Some details");

        C21Order returnedOrder = service.addDocumentToC21(order.build(), document);

        assertThat(returnedOrder).isEqualTo(order.document(DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build()).build());
    }

    @Test
    void shouldReturnExpectedC21OrderWhenOrderTitleIsNull() {
        C21Order c21Order = C21Order.builder()
            .orderTitle(null)
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        C21Order returnedOrder = service.addCustomValuesToC21Order(c21Order, JudgeAndLegalAdvisor.builder().build());

        assertThat(returnedOrder.getOrderTitle()).isEqualTo("Order");
        assertThat(returnedOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedOrder.getOrderDetails()).isEqualTo("Some details");
        assertThat(returnedOrder.getOrderDate()).isNotNull();
        assertThat(returnedOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    @Test
    void shouldReturnExpectedC21OrderWhenOrderTitleIsEmptyString() {
        C21Order c21Order = C21Order.builder()
            .orderTitle("")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        C21Order returnedOrder = service.addCustomValuesToC21Order(c21Order, JudgeAndLegalAdvisor.builder().build());

        assertThat(returnedOrder.getOrderTitle()).isEqualTo("Order");
        assertThat(returnedOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedOrder.getOrderDetails()).isEqualTo("Some details");
        assertThat(returnedOrder.getOrderDate()).isNotNull();
        assertThat(returnedOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    @Test
    void shouldReturnExpectedC21OrderWhenOrderTitleIsStringWithSpaceCharacter() {
        C21Order c21Order = C21Order.builder()
            .orderTitle(" ")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        C21Order returnedOrder = service.addCustomValuesToC21Order(c21Order, JudgeAndLegalAdvisor.builder().build());

        assertThat(returnedOrder.getOrderTitle()).isEqualTo("Order");
        assertThat(returnedOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedOrder.getOrderDetails()).isEqualTo("Some details");
        assertThat(returnedOrder.getOrderDate()).isNotNull();
        assertThat(returnedOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    @Test
    void shouldReturnExpectedC21OrderWhenOrderTitlePresent() {
        C21Order c21Order = C21Order.builder()
            .orderTitle("Example Title")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        C21Order returnedOrder = service.addCustomValuesToC21Order(c21Order, JudgeAndLegalAdvisor.builder().build());

        assertThat(returnedOrder.getOrderTitle()).isEqualTo("Example Title");
        assertThat(returnedOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedOrder.getOrderDetails()).isEqualTo("Some details");
        assertThat(returnedOrder.getOrderDate()).isNotNull();
        assertThat(returnedOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    @Test
    void shouldReturnExpectedC21OrderWhenJudgeAndLegalAdvisorFullyPopulated() {
        C21Order c21Order = C21Order.builder()
            .orderTitle("Example Title")
            .orderDetails("Some details")
            .document(DocumentReference.builder().build())
            .build();

        C21Order returnedOrder = service.addCustomValuesToC21Order(c21Order, JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());

        assertThat(returnedOrder.getOrderTitle()).isEqualTo("Example Title");
        assertThat(returnedOrder.getDocument()).isEqualTo(DocumentReference.builder().build());
        assertThat(returnedOrder.getOrderDetails()).isEqualTo("Some details");
        assertThat(returnedOrder.getOrderDate()).isNotNull();
        assertThat(returnedOrder.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .legalAdvisorName("Peter Parker")
            .build());
    }

    @Test
    void shouldCreateExpectedMapWhenGivenPopulatedCaseData() {
        LocalDate localDate = LocalDate.now();
        String date = localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));

        CaseData caseData = populatedCaseData(localDate);

        Map<String, Object> expectedMap = expectedData(date);

        Map<String, Object> templateData = service.getC21OrderTemplateData(caseData);
        assertThat(templateData).isEqualTo(expectedMap);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> expectedData(String date) {
        ImmutableMap.Builder expectedMap = ImmutableMap.<String, Object>builder();

        expectedMap.put("familyManCaseNumber", "123")
            .put("courtName", "Example Court")
            .put("orderTitle", "Example Title")
            .put("orderDetails", "Example details")
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

    private CaseData populatedCaseData(LocalDate localDate) {
        return CaseData.builder()
            .familyManCaseNumber("123")
            .caseLocalAuthority("example")
            .c21Order(C21Order.builder()
                .orderTitle("Example Title")
                .orderDetails("Example details")
                .build())
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
    }

}
