//package uk.gov.hmcts.reform.fpl.service;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
//import uk.gov.hmcts.reform.fpl.model.C21Order;
//import uk.gov.hmcts.reform.fpl.model.CaseData;
//import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
//import uk.gov.hmcts.reform.fpl.model.common.Element;
//import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
//import uk.gov.hmcts.reform.fpl.service.time.Time;
//
//import java.time.LocalDateTime;
//import java.time.format.FormatStyle;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
//import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
//
//@ExtendWith(SpringExtension.class)
//class CreateC21OrderServiceTest {
//    private static final String LOCAL_AUTHORITY_CODE = "example";
//    private static final String COURT_NAME = "Example Court";
//    private static final String COURT_EMAIL = "example@court.com";
//    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);
//    private static final LocalDateTime NOW = LocalDateTime.now();
//
//    private final Time time = () -> NOW;
//
//    private DateFormatterService dateFormatterService = new DateFormatterService();
//    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);
//
//    @Autowired
//    private UploadDocumentService uploadDocumentService;
//
//    @Autowired
//    private DocmosisDocumentGeneratorService docmosisService;
//
//    private CreateC21OrderService service;
//
//    @BeforeEach
//    void setup() {
//        this.service = new CreateC21OrderService(dateFormatterService,
//            hmctsCourtLookupConfiguration, time);
//    }
//    @Test
//    void shouldAppendNewC21OrderToEmptyC21Orders() {
//        List<Element<C21Order>> emptyC21Orders = new ArrayList<>();
//
//        CaseData caseData = addC21OrderAndBundleToCaseData(emptyC21Orders, "C21_1.pdf");
//
//        List<Element<C21Orders>> c21OrdersWithOrder = service.addToC21Orders(
//            caseData.getC21Order(), caseData.getJudgeAndLegalAdvisor(), caseData.getC21Orders());
//        assertThat(c21OrdersWithOrder.size()).isEqualTo(1);
//
//        C21Orders c21Orders = c21OrdersWithOrder.get(0).getValue();
//
//        //check c21Orders contains
//        assertThat(c21Orders.getC21OrderDocument().getFilename()).isEqualTo("C21_1.pdf");
//        assertThat(c21Orders.getOrderDate()).isEqualTo(
//            dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy"));
//        assertThat(c21Orders.getOrderTitle()).isEqualTo("Example order title");
//        assertThat(c21Orders.getJudgeTitleAndName()).isEqualTo("His Honour Judge Johnson");
//    }
//
//    @Test
//    void shouldAppendNewC21OrderToExistingC21Orders() {
//        CaseData caseData = addC21OrderAndBundleToCaseData(getExistingC21Orders(), "C21_2.pdf");
//
//        List<Element<C21Orders>> c21OrdersWithTwoOrders = service.addToC21Orders(
//            caseData.getC21Order(), caseData.getJudgeAndLegalAdvisor(), caseData.getC21Orders());
//        assertThat(c21OrdersWithTwoOrders.size()).isEqualTo(2);
//        C21Orders previousC21 = c21OrdersWithTwoOrders.get(0).getValue();
//        C21Orders appendedC21 = c21OrdersWithTwoOrders.get(1).getValue();
//
//        assertThat(previousC21.getC21OrderDocument().getFilename()).isEqualTo("C21_1.pdf");
//        assertThat(appendedC21.getC21OrderDocument().getFilename()).isEqualTo("C21_2.pdf");
//    }
//
//    @Test
//    void shouldFormatC21TemplateDataCorrectlyWhenOnlyC21MandatoryDataIsIncluded() {
//        CaseData caseData = buildCaseData(false);
//
//        Map<String, Object> templateData = service.getC21OrderTemplateData(caseData);
//
//        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
//        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
//        assertThat(templateData.get("orderTitle")).isEqualTo("Order");
//        assertThat(templateData.get("orderDetails")).isEqualTo("Example order details");
//        assertThat(templateData.get("todaysDate")).isEqualTo(
//            dateFormatterService.formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG));
//        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("");
//        assertThat(templateData.get("legalAdvisorName")).isEqualTo("");
//        assertThat(templateData.get("children")).isEqualTo(ImmutableList.of());
//    }
//
//    @Test
//    void shouldFormatC21TemplateDataCorrectlyWhenC21TemplateDataIsPopulated() {
//        CaseData caseData = buildCaseData(true);
//
//        Map<String, Object> templateData = service.getC21OrderTemplateData(caseData);
//
//        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
//        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
//        assertThat(templateData.get("orderTitle")).isEqualTo("Example order title");
//        assertThat(templateData.get("orderDetails")).isEqualTo("Example order details");
//        assertThat(templateData.get("todaysDate")).isEqualTo(
//            dateFormatterService.formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG));
//        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("His Honour Judge Johnson");
//        assertThat(templateData.get("legalAdvisorName")).isEqualTo("John Clarke");
//        assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
//    }
//
//    private CaseData buildCaseData(boolean addAllFields) {
//        C21Order.C21OrderBuilder c21OrderBuilder = C21Order.builder()
//            .orderDetails("Example order details");
//
//        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
//            .caseLocalAuthority("example")
//            .familyManCaseNumber("123")
//            .c21Order(c21OrderBuilder.build());
//
//        if (addAllFields) {
//            caseDataBuilder
//                .c21Order(c21OrderBuilder
//                    .orderTitle("Example order title")
//                    .build())
//                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
//                    .judgeTitle(HIS_HONOUR_JUDGE)
//                    .judgeLastName("Johnson")
//                    .legalAdvisorName("John Clarke")
//                    .build())
//                .children1(createPopulatedChildren())
//                .build();
//        }
//        return caseDataBuilder.build();
//    }
//
//    private List<Map<String, String>> getExpectedChildren() {
//        return ImmutableList.of(
//            ImmutableMap.of(
//                "name", "Bran Stark",
//                "gender", "Male",
//                "dateOfBirth", dateFormatterService.formatLocalDateToString(time.now().toLocalDate(),
//                    FormatStyle.LONG)),
//            ImmutableMap.of(
//                "name", "Sansa Stark",
//                "gender", "",
//                "dateOfBirth", ""),
//            ImmutableMap.of(
//                "name", "Jon Snow",
//                "gender", "",
//                "dateOfBirth", ""));
//    }
//
//    private CaseData addC21OrderAndBundleToCaseData(List<Element<C21Orders>> c21Orders, String fileName) {
//        return CaseData.builder()
//            .c21Orders(c21Orders)
//            .c21Order(C21Order.builder()
//                .orderTitle("Example order title")
//                .orderDetails("Example order details")
//                .document(DocumentReference.builder()
//                    .filename(fileName)
//                    .build())
//                .build())
//            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
//                .judgeTitle(HIS_HONOUR_JUDGE)
//                .judgeLastName("Johnson")
//                .legalAdvisorName("John Clarke")
//                .build())
//            .build();
//    }
//
//    private List<Element<C21Orders>> getExistingC21Orders() {
//        List<Element<C21Orders>> c21EmptyOrderBundle = new ArrayList<>();
//        CaseData caseData = addC21OrderAndBundleToCaseData(c21EmptyOrderBundle, "C21_1.pdf");
//        return service.addToC21Orders(
//            caseData.getC21Order(), caseData.getJudgeAndLegalAdvisor(), caseData.getC21Orders());
//    }
//}
