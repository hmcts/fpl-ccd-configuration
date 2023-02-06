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
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C21BlankOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class C21BlankOrderDocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final GeneratedOrderType TYPE = GeneratedOrderType.BLANK_ORDER;
    private static final String TITLE = "Test title";
    private static final String DIRECTIONS = "Test directions";
    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LA_CODE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.C21_BLANK_ORDER)
            .manageOrdersTitle(TITLE)
            .manageOrdersDirections(DIRECTIONS)
            .build())
        .build();

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C21BlankOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C21_BLANK_ORDER);
    }

    @Test
    void generate() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        assertThat(generatedParameters).isEqualTo(expectedCommonParameters().build());
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @Test
    void shouldReturnDefaultTitleWhenBlankOrderTitleNotProvided() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        CaseData caseData = CASE_DATA.toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C21_BLANK_ORDER)
                .manageOrdersDirections(DIRECTIONS)
                .build())
            .build();

        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderTitle("Order")
            .build();

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private C21BlankOrderDocmosisParameters.C21BlankOrderDocmosisParametersBuilder<?,?> expectedCommonParameters() {
        return C21BlankOrderDocmosisParameters.builder()
            .orderTitle(TITLE)
            .orderDetails(DIRECTIONS)
            .localAuthorityName(LA_NAME)
            .orderType(TYPE);
    }
}
