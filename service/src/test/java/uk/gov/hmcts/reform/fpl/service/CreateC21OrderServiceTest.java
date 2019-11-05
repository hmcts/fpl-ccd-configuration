package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C21OrderBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;

@ExtendWith(SpringExtension.class)
class CreateC21OrderServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);
    private static final LocalDate TODAYS_DATE = LocalDate.now();


    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);
    private CreateC21OrderService createC21OrderService = new CreateC21OrderService(dateFormatterService,
        hmctsCourtLookupConfiguration);

    @Test
    void shouldAppendNewC21OrderToEmptyC21OrderBundle() {
        List<Element<C21OrderBundle>> emptyC21OrderBundle = new ArrayList<>();
        CaseData caseData = addC21OrderAndBundleToCaseData(emptyC21OrderBundle, "C21_1.pdf");

        List<Element<C21OrderBundle>> c21OrderBundleWithOrder = createC21OrderService.appendToC21OrderBundle(
            caseData.getTemporaryC21Order(), caseData.getC21OrderBundle(), caseData.getJudgeAndLegalAdvisor());
        assertThat(c21OrderBundleWithOrder).size().isEqualTo(1);

        C21OrderBundle c21OrderBundle = c21OrderBundleWithOrder.get(0).getValue();

        assertThat(c21OrderBundle.getC21OrderDocument().getFilename()).isEqualTo("C21_1.pdf");
        assertThat(c21OrderBundle.getOrderTitle()).isEqualTo("Example order title");
        assertThat(c21OrderBundle.getJudgeTitleAndName()).isEqualTo("His Honour Judge Johnson");
    }

    @Test
    void shouldAppendNewC21OrderToExistingC21OrderBundle() {
        CaseData caseData = addC21OrderAndBundleToCaseData(getExistingC21OrderBundle(), "C21_2.pdf");

        List<Element<C21OrderBundle>> c21OrderBundleWithTwoOrders = createC21OrderService.appendToC21OrderBundle(
            caseData.getTemporaryC21Order(), caseData.getC21OrderBundle(), caseData.getJudgeAndLegalAdvisor());
        assertThat(c21OrderBundleWithTwoOrders).size().isEqualTo(2);

        C21OrderBundle previousC21 = c21OrderBundleWithTwoOrders.get(0).getValue();
        C21OrderBundle appendedC21 = c21OrderBundleWithTwoOrders.get(1).getValue();

        assertThat(previousC21.getC21OrderDocument().getFilename()).isEqualTo("C21_1.pdf");
        assertThat(appendedC21.getC21OrderDocument().getFilename()).isEqualTo("C21_2.pdf");
    }

    @Test
    void shouldFormatC21TemplateDataCorrectlyWhenOnlyC21MandatoryDataIsIncluded() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .temporaryC21Order(C21Order.builder()
                .orderDetails("Example order details")
                .build())
            .build();

        Map<String, Object> templateData = createC21OrderService.getC21OrderTemplateData(caseData);

        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("orderTitle")).isEqualTo("Order");
        assertThat(templateData.get("orderDetails")).isEqualTo("Example order details");
        assertThat(templateData.get("todaysDate")).isEqualTo(dateFormatterService.formatLocalDateToString(TODAYS_DATE,
            FormatStyle.LONG));
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("");
        assertThat(templateData.get("legalAdvisorName")).isEqualTo("");
        assertThat(templateData.get("children")).isEqualTo(ImmutableList.of());
    }

    @Test
    void shouldFormatC21TemplateDataCorrectlyWhenC21TemplateDataIsPopulated() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .temporaryC21Order(C21Order.builder()
                .orderTitle("Example order title")
                .orderDetails("Example order details")
                .build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Johnson")
                .legalAdvisorName("John Clarke")
                .build())
            .children1(createPopulatedChildren())
            .build();

        Map<String, Object> templateData = createC21OrderService.getC21OrderTemplateData(caseData);

        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("orderTitle")).isEqualTo("Example order title");
        assertThat(templateData.get("orderDetails")).isEqualTo("Example order details");
        assertThat(templateData.get("todaysDate")).isEqualTo(dateFormatterService.formatLocalDateToString(TODAYS_DATE,
            FormatStyle.LONG));
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("His Honour Judge Johnson");
        assertThat(templateData.get("legalAdvisorName")).isEqualTo("John Clarke");
        assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
    }

    private List<Map<String, String>> getExpectedChildren() {
        return ImmutableList.of(
            ImmutableMap.of(
                "name", "Bran Stark",
                "gender", "Male",
                "dateOfBirth", dateFormatterService.formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG)),
            ImmutableMap.of(
                "name", "Sansa Stark",
                "gender", "",
                "dateOfBirth", ""),
            ImmutableMap.of(
                "name", "Jon Snow",
                "gender", "",
                "dateOfBirth", ""));
    }


    private CaseData addC21OrderAndBundleToCaseData(List<Element<C21OrderBundle>> c21OrderBundle, String fileName) {
        return CaseData.builder()
            .c21OrderBundle(c21OrderBundle)
            .temporaryC21Order(C21Order.builder()
                .orderTitle("Example order title")
                .orderDetails("Example order details")
                .c21OrderDocument(DocumentReference.builder()
                    .filename(fileName)
                    .build())
                .build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Johnson")
                .legalAdvisorName("John Clarke")
                .build())
            .build();
    }

    private List<Element<C21OrderBundle>> getExistingC21OrderBundle() {
        List<Element<C21OrderBundle>> c21EmptyOrderBundle = new ArrayList<>();
        CaseData caseData = addC21OrderAndBundleToCaseData(c21EmptyOrderBundle, "C21_1.pdf");
        return createC21OrderService.appendToC21OrderBundle(
            caseData.getTemporaryC21Order(), caseData.getC21OrderBundle(), caseData.getJudgeAndLegalAdvisor());

    }

}
