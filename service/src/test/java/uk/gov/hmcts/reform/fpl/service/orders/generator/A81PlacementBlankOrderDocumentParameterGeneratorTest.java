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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
            .orderDetails("THE COURT ORDERS THAT:\n\nparagraphs is here\n\ncost orders is here");
    }

    private A81PlacementBlankOrderDocmosisParameters.A81PlacementBlankOrderDocmosisParametersBuilder<?,?>
        expectedCommonParametersWithoutCostCode() {
        return A81PlacementBlankOrderDocmosisParameters.builder()
            .orderTitle(TITLE)
            .orderDetails(DIRECTIONS)
            .localAuthorityName(LA_NAME)
            .orderType(TYPE)
            .recitalsOrPreamble("Preambles Text is here")
            .orderDetails("THE COURT ORDERS THAT:\n\nparagraphs is here");
    }
}
