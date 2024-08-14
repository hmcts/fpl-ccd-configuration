package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedPlacementOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

@ExtendWith(MockitoExtension.class)
class ManageOrdersEventBuilderTest {

    private static final List<Element<GeneratedOrder>> NO_ORDERS = List.of();
    private static final String ORDER_TITLE = "orderTitle";
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENT =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;

    private final DocumentReference orderDocument = mock(DocumentReference.class);
    private final DocumentReference orderNotificationDocument = mock(DocumentReference.class);
    private final GeneratedOrder order = mock(GeneratedOrder.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    @Mock
    private SealedOrderHistoryService historyService;

    @Mock
    private AmendableOrder amendableOrder;

    @Mock
    private AmendedOrderFinder<AmendableOrder> finder;

    private ManageOrdersEventBuilder underTest;

    @BeforeEach
    void setUp() {
        underTest = new ManageOrdersEventBuilder(historyService, List.of(finder));
        when(caseDataBefore.getOrderCollection()).thenReturn(NO_ORDERS);
    }

    @Test
    void buildAmendedOrder() {
        List<Element<Other>> selectedOthers = List.of(element(testOther("Other 1")));
        DocumentReference expectedDocument = testDocumentReference();

        CaseData caseData = CaseData.builder().orderCollection(NO_ORDERS).build();
        when(amendableOrder.getDocument()).thenReturn(expectedDocument);
        when(amendableOrder.getModifiedItemType()).thenReturn("Care order");
        when(amendableOrder.getSelectedOthers()).thenReturn(selectedOthers);

        when(finder.findOrderIfPresent(caseData, caseDataBefore)).thenReturn(Optional.of(amendableOrder));

        assertThat(underTest.build(caseData, caseDataBefore)).isEqualTo(
            new AmendedOrderEvent(caseData, expectedDocument, "Care order", selectedOthers)
        );

        verifyNoInteractions(historyService);
    }

    @Test
    void buildGeneratedGeneralOrder() {
        CaseData caseData = CaseData.builder().orderCollection(wrapElements(order)).build();
        when(order.getApprovalDate()).thenReturn(LocalDate.now());
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(order);
        when(order.getDocument()).thenReturn(orderDocument);
        when(order.asLabel()).thenReturn(ORDER_TITLE);
        when(order.getTranslationRequirements()).thenReturn(TRANSLATION_REQUIREMENT);

        assertThat(underTest.build(caseData, caseDataBefore)).isEqualTo(new GeneratedOrderEvent(caseData,
            orderDocument,
            TRANSLATION_REQUIREMENT,
            ORDER_TITLE,
            LocalDate.now()));
    }

    @Test
    void buildGeneratedPlacementOrder() {
        CaseData caseData = CaseData.builder().orderCollection(wrapElements(order)).build();
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(order);
        when(order.getOrderType()).thenReturn(Order.A70_PLACEMENT_ORDER.name());
        when(order.getDocument()).thenReturn(orderDocument);
        when(order.getNotificationDocument()).thenReturn(orderNotificationDocument);
        when(order.asLabel()).thenReturn(ORDER_TITLE);
        when(order.getTranslationRequirements()).thenReturn(TRANSLATION_REQUIREMENT);

        assertThat(underTest.build(caseData, caseDataBefore))
            .isEqualTo(new GeneratedPlacementOrderEvent(caseData, orderDocument, orderNotificationDocument));
    }

}
