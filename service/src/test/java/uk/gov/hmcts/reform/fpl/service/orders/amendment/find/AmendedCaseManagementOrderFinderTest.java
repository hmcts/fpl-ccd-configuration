package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class AmendedCaseManagementOrderFinderTest {
    private static final HearingOrder ORIGINAL_CMO = mock(HearingOrder.class);
    private static final HearingOrder AMENDED_CMO = mock(HearingOrder.class);
    private static final Element<HearingOrder> ORIGINAL_ELEMENT = element(ORIGINAL_CMO);
    private static final Element<HearingOrder> AMENDED_ELEMENT = element(AMENDED_CMO);

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final AmendedCaseManagementOrderFinder underTest = new AmendedCaseManagementOrderFinder();

    @Test
    void findOrderIfPresentAmended() {
        when(caseData.getSealedCMOs()).thenReturn(List.of(AMENDED_ELEMENT));
        when(caseDataBefore.getSealedCMOs()).thenReturn(List.of(ORIGINAL_ELEMENT));

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).contains(AMENDED_CMO);
    }

    @Test
    void findOrderIfPresentNotAmended() {
        when(caseData.getSealedCMOs()).thenReturn(List.of(ORIGINAL_ELEMENT));
        when(caseDataBefore.getSealedCMOs()).thenReturn(List.of(ORIGINAL_ELEMENT));

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).isEmpty();
    }
}
