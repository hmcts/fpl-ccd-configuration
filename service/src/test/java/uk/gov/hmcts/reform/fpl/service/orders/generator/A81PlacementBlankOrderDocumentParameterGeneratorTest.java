package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A81PlacementBlankOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith({MockitoExtension.class})
class A81PlacementBlankOrderDocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final GeneratedOrderType TYPE = GeneratedOrderType.BLANK_ORDER;
    private static final String TITLE = "Placement order";
    private static final String DIRECTIONS = "Test directions";
    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LA_CODE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.A81_PLACEMENT_BLANK_ORDER)
            .manageOrdersTitle(TITLE)
            .manageOrdersDirections(DIRECTIONS)
            .manageOrdersPreamblesText("Preambles Text is here")
            .manageOrdersCostOrders("cost orders is here")
            .manageOrdersParagraphs("paragraphs is here")
            .build())
        .respondents1(List.of(element(Respondent.builder()
            .party(RespondentParty.builder().firstName("Test").lastName("Respondent").build())
            .build())))
        .build();
    private static final CaseData CASE_DATA_WITHOUT_COST_CODE = CaseData.builder()
        .caseLocalAuthority(LA_CODE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.A81_PLACEMENT_BLANK_ORDER)
            .manageOrdersTitle(TITLE)
            .manageOrdersDirections(DIRECTIONS)
            .manageOrdersPreamblesText("Preambles Text is here")
            .manageOrdersParagraphs("paragraphs is here")
            .build())
        .respondents1(List.of(element(Respondent.builder()
            .party(RespondentParty.builder().firstName("Test").lastName("Respondent").build())
            .build())))
        .build();

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private A81PlacementBlankOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.A81_PLACEMENT_BLANK_ORDER);
    }

    @Test
    void generate() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        assertThat(generatedParameters).isEqualTo(expectedCommonParameters().build());
    }

    @Test
    void generateWithoutCostCode() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA_WITHOUT_COST_CODE);
        assertThat(generatedParameters).isEqualTo(expectedCommonParametersWithoutCostCode().build());
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    private A81PlacementBlankOrderDocmosisParameters.A81PlacementBlankOrderDocmosisParametersBuilder<?,?>
        expectedCommonParameters() {
        return A81PlacementBlankOrderDocmosisParameters.builder()
            .orderTitle(TITLE)
            .orderDetails(DIRECTIONS)
            .localAuthorityName(LA_NAME)
            .orderType(TYPE)
            .recitalsOrPreamble("Preambles Text is here")
            .orderDetails("THE COURT ORDERS THAT:\n\nparagraphs is here\n\ncost orders is here")
            .respondentNames("Test Respondent is");
    }

    private A81PlacementBlankOrderDocmosisParameters.A81PlacementBlankOrderDocmosisParametersBuilder<?,?>
        expectedCommonParametersWithoutCostCode() {
        return A81PlacementBlankOrderDocmosisParameters.builder()
            .orderTitle(TITLE)
            .orderDetails(DIRECTIONS)
            .localAuthorityName(LA_NAME)
            .orderType(TYPE)
            .recitalsOrPreamble("Preambles Text is here")
            .orderDetails("THE COURT ORDERS THAT:\n\nparagraphs is here")
            .respondentNames("Test Respondent is");
    }

    @Test
    void shouldFormatRespondentNamesSingleRespondent() {
        RespondentParty respondentParty = RespondentParty.builder()
            .firstName("John")
            .lastName("Ross")
            .build();
        Respondent respondent = Respondent.builder().party(respondentParty).build();
        CaseData caseData = CASE_DATA.toBuilder()
            .respondents1(List.of(element(respondent)))
            .build();
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        A81PlacementBlankOrderDocmosisParameters params =
            (A81PlacementBlankOrderDocmosisParameters) underTest.generate(caseData);
        assertThat(params.getRespondentNames()).isEqualTo("John Ross is");
    }

    @Test
    void shouldFormatRespondentNamesTwoRespondents() {
        RespondentParty respondentParty1 = RespondentParty.builder()
            .firstName("John")
            .lastName("Ross")
            .build();
        RespondentParty respondentParty2 = RespondentParty.builder()
            .firstName("Julie")
            .lastName("Ross")
            .build();
        Respondent respondent1 = Respondent.builder().party(respondentParty1).build();
        Respondent respondent2 = Respondent.builder().party(respondentParty2).build();
        CaseData caseData = CASE_DATA.toBuilder()
            .respondents1(List.of(element(respondent1), element(respondent2)))
            .build();
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        A81PlacementBlankOrderDocmosisParameters params =
            (A81PlacementBlankOrderDocmosisParameters) underTest.generate(caseData);
        assertThat(params.getRespondentNames()).isEqualTo("John Ross and Julie Ross are");
    }

    @Test
    void shouldFormatRespondentNamesThreeRespondents() {
        RespondentParty respondentParty1 = RespondentParty.builder()
            .firstName("John")
            .lastName("Ross")
            .build();
        RespondentParty respondentParty2 = RespondentParty.builder()
            .firstName("Julie")
            .lastName("Ross")
            .build();
        RespondentParty respondentParty3 = RespondentParty.builder()
            .firstName("Karen")
            .lastName("Donalds")
            .build();
        Respondent respondent1 = Respondent.builder().party(respondentParty1).build();
        Respondent respondent2 = Respondent.builder().party(respondentParty2).build();
        Respondent respondent3 = Respondent.builder().party(respondentParty3).build();
        CaseData caseData = CASE_DATA.toBuilder()
            .respondents1(List.of(element(respondent1), element(respondent2), element(respondent3)))
            .build();
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        A81PlacementBlankOrderDocmosisParameters params =
            (A81PlacementBlankOrderDocmosisParameters) underTest.generate(caseData);
        assertThat(params.getRespondentNames()).isEqualTo("John Ross, Julie Ross and Karen Donalds are");
    }
}
