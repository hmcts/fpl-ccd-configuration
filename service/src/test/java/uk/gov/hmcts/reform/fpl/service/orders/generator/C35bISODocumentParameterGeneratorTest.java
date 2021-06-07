package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35bInterimSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeMessages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;

@ExtendWith({MockitoExtension.class})
class C35bISODocumentParameterGeneratorTest {

    private static final DocmosisTemplates TEMPLATE = DocmosisTemplates.ORDER;
    private static final Order ORDER = C35B_INTERIM_SUPERVISION_ORDER;
    private static final String FURTHER_DIRECTIONS = "FurtherDirections";
    private static final String ORDER_DETAILS = "OrderDetails";
    private static final ManageOrdersEndDateType ORDERS_END_DATE_TYPE = ManageOrdersEndDateType.END_OF_PROCEEDINGS;

    @Mock
    private OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator;

    @InjectMocks
    private C35bISODocumentParameterGenerator underTest;

    @Test
    void shouldReturnAcceptedOrder() {
        Order order = underTest.accept();

        assertThat(order).isEqualTo(ORDER);
    }

    @Test
    public void shouldReturnTemplate() {
        DocmosisTemplates returnedTemplate = underTest.template();

        assertThat(TEMPLATE).isEqualTo(returnedTemplate);
    }

    @Test
    public void shouldReturnDocmosisParameters() {

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersEndDateTypeWithEndOfProceedings(ORDERS_END_DATE_TYPE)
                .build())
            .build();

        when(orderDetailsWithEndTypeGenerator.orderDetails(ORDERS_END_DATE_TYPE, OrderDetailsWithEndTypeMessages
            .builder()
            .messageWithSpecifiedTime(
                "The Court orders ${localAuthorityName} supervises the ${childOrChildren} until ${endDate}.")
            .messageWithNumberOfMonths(
                "The Court orders ${localAuthorityName} supervises the ${childOrChildren} for ${numOfMonths} "
                    + "months from the date of "
                    + "this order until ${endDate}.")
            .messageWithEndOfProceedings(
                "The Court orders ${localAuthorityName} supervises the ${childOrChildren} until "
                    + "the end of the proceedings or until a further order is made.").build(),
            caseData)).thenReturn(ORDER_DETAILS);

        DocmosisParameters actual = underTest.generate(caseData);

        assertThat(actual).isEqualTo(C35bInterimSupervisionOrderDocmosisParameters.builder()
            .orderTitle("Interim supervision order")
            .orderType(GeneratedOrderType.SUPERVISION_ORDER)
            .furtherDirections(FURTHER_DIRECTIONS)
            .orderDetails(ORDER_DETAILS)
            .build());

    }

}
