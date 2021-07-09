package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class AmendableOrderListBuilderTest {
    @Captor
    private ArgumentCaptor<Function<Element<? extends AmendableOrder>, String>> codeCaptor;
    @Captor
    private ArgumentCaptor<Function<Element<? extends AmendableOrder>, String>> labelCaptor;

    private final AmendableListItemProvider provider = mock(AmendableListItemProvider.class);
    private final DynamicListService listService = mock(DynamicListService.class);
    private final AmendableOrderListBuilder underTest = new AmendableOrderListBuilder(listService, List.of(provider));

    @Test
    void buildList() {
        UUID order1Id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID order2Id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID order3Id = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID order4Id = UUID.fromString("44444444-4444-4444-4444-444444444444");

        AmendableOrder order1 = mock(AmendableOrder.class);
        AmendableOrder order2 = mock(AmendableOrder.class);
        AmendableOrder order3 = mock(AmendableOrder.class);
        AmendableOrder order4 = mock(AmendableOrder.class);

        Element<AmendableOrder> order2Element = element(order2Id, order2);

        CaseData caseData = mock(CaseData.class);

        when(provider.provideListItems(caseData)).thenReturn(List.of(
            element(order1Id, order1), order2Element, element(order3Id, order3),
            element(order4Id, order4)
        ));

        // order1 and order4 label methods are not called as the comparator doesn't need to evaluate those methods
        String order2Label = "label with more alphabetical value";
        when(order2.asLabel()).thenReturn(order2Label);
        when(order3.asLabel()).thenReturn("label with less alphabetical value");

        LocalDate now = LocalDate.now();
        LocalDate past = now.minusDays(1);
        LocalDate distantPast = now.minusDays(15);

        when(order1.amendableSortDate()).thenReturn(now);
        when(order2.amendableSortDate()).thenReturn(past);
        when(order3.amendableSortDate()).thenReturn(past);
        when(order4.amendableSortDate()).thenReturn(distantPast);

        DynamicList amendableOrderList = mock(DynamicList.class);

        when(listService.asDynamicList(any(), any(), any())).thenReturn(amendableOrderList);

        assertThat(underTest.buildList(caseData)).isEqualTo(amendableOrderList);

        List<Element<? extends AmendableOrder>> sortedOrders = List.of(
            element(order1Id, order1), element(order3Id, order3), order2Element, element(order4Id, order4)
        );

        verify(listService).asDynamicList(eq(sortedOrders), codeCaptor.capture(), labelCaptor.capture());

        assertThat(codeCaptor.getValue().apply(order2Element)).isEqualTo(order2Id.toString());
        assertThat(labelCaptor.getValue().apply(order2Element)).isEqualTo(order2Label);
    }
}
