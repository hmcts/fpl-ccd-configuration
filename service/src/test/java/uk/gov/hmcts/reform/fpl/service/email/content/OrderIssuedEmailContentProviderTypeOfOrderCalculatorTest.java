package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class OrderIssuedEmailContentProviderTypeOfOrderCalculatorTest {
    private final SealedOrderHistoryService sealedOrderHistoryService = mock(SealedOrderHistoryService.class);
    private final OrderIssuedEmailContentProviderTypeOfOrderCalculator underTest =
        new OrderIssuedEmailContentProviderTypeOfOrderCalculator(
            sealedOrderHistoryService
        );
    private final GeneratedOrder legacyOrder1 = mock(GeneratedOrder.class);
    private final GeneratedOrder legacyOrder2 = mock(GeneratedOrder.class);
    private final GeneratedOrder newGeneratedOrder1 = mock(GeneratedOrder.class);

    @BeforeEach
    void setUp() {
        when(legacyOrder1.isNewVersion()).thenReturn(false);
        when(legacyOrder1.getType()).thenReturn("legacyOrDer1");
        when(legacyOrder2.isNewVersion()).thenReturn(false);
        when(legacyOrder2.getType()).thenReturn("legacyOrDer2");
        when(newGeneratedOrder1.isNewVersion()).thenReturn(true);
        when(newGeneratedOrder1.getType()).thenReturn("newOrderTitle1");
    }

    @Test
    void typeOfOrderForLegacyGeneratedOrder() {

        CaseData caseData = CaseData.builder()
            .orderCollection(wrapElements(legacyOrder1, legacyOrder2))
            .build();
        when(sealedOrderHistoryService.lastGeneratedOrder(caseData)).thenReturn(legacyOrder1);

        String actual = underTest.getTypeOfOrder(caseData, IssuedOrderType.GENERATED_ORDER);

        assertThat(actual).isEqualTo("legacyorder2");
    }

    @Test
    void typeOfOrderForNewGeneratedOrder() {

        CaseData caseData = CaseData.builder()
            .orderCollection(wrapElements(newGeneratedOrder1, legacyOrder1))
            .build();
        when(sealedOrderHistoryService.lastGeneratedOrder(caseData)).thenReturn(newGeneratedOrder1);

        String actual = underTest.getTypeOfOrder(caseData, IssuedOrderType.GENERATED_ORDER);

        assertThat(actual).isEqualTo("newordertitle1");
    }

    @Test
    void typeOfOrderForOtherIssueTypeNoticeOfPlacement() {

        String actual = underTest.getTypeOfOrder(null, NOTICE_OF_PLACEMENT_ORDER);

        assertThat(actual).isEqualTo("notice of placement order");
    }

    @Test
    void typeOfOrderForOtherIssueTypeCMO() {

        String actual = underTest.getTypeOfOrder(null, CMO);

        assertThat(actual).isEqualTo("case management order");
    }
}
