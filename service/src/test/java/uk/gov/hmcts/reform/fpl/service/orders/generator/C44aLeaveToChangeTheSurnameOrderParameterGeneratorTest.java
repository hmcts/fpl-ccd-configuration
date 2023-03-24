package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C44aLeaveToChangeTheSurnameOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

public class C44aLeaveToChangeTheSurnameOrderParameterGeneratorTest {

    private final OrderMessageGenerator orderMessageGenerator = mock(OrderMessageGenerator.class);

    private C44aLeaveToChangeTheSurnameOrderParameterGenerator underTest =
        new C44aLeaveToChangeTheSurnameOrderParameterGenerator(
            orderMessageGenerator
        );

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C44A_LEAVE_TO_CHANGE_A_SURNAME);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @Test
    void shouldReturnTemplateData() {
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("By consent");
        when(orderMessageGenerator.formatOrderMessage(any(),
                eq("The Court grants leave to PartyOne to change the ${childOrChildren} surname to ABC")))
            .thenReturn("The Court grants leave to PartyOne to change the child surname to ABC");

        ManageOrdersEventData eventData = ManageOrdersEventData.builder()
            .manageOrdersIsByConsent(YES.getValue())
            .manageOrdersPartyGrantedLeave("PartyOne")
            .manageOrdersChildNewSurname("ABC")
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(eventData)
            .build();

        C44aLeaveToChangeTheSurnameOrderDocmosisParameters actualData =
            (C44aLeaveToChangeTheSurnameOrderDocmosisParameters) underTest.generate(caseData);

        C44aLeaveToChangeTheSurnameOrderDocmosisParameters expectedData =
            C44aLeaveToChangeTheSurnameOrderDocmosisParameters.builder()
                .orderTitle(Order.C44A_LEAVE_TO_CHANGE_A_SURNAME.getTitle())
                .orderByConsent("By consent")
                .orderDetails("The Court grants leave to PartyOne to change the child surname to ABC")
                .build();

        assertThat(actualData).isEqualTo(expectedData);
    }


}
