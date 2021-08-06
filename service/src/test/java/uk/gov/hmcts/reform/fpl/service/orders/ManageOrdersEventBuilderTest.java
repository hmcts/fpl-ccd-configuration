package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

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

    private final DocumentReference document = mock(DocumentReference.class);
    private final GeneratedOrder order = mock(GeneratedOrder.class);
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final List<Element<GeneratedOrder>> orders = wrapElements(order);

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
    }

    @Test
    void buildAmended() {
        List<Element<Other>> selectedOthers = List.of(element(testOther("Other 1")));
        DocumentReference expectedDocument = testDocumentReference();

        when(caseData.getOrderCollection()).thenReturn(NO_ORDERS);
        when(caseDataBefore.getOrderCollection()).thenReturn(NO_ORDERS);
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
    void buildNonAmended() {
        when(caseData.getOrderCollection()).thenReturn(orders);
        when(caseDataBefore.getOrderCollection()).thenReturn(NO_ORDERS);
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(order);
        when(order.getDocument()).thenReturn(document);
        when(order.asLabel()).thenReturn(ORDER_TITLE);
        when(order.getTranslationRequirements()).thenReturn(TRANSLATION_REQUIREMENT);
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(GeneratedOrder.builder()
            .document(document)
            .build());

        assertThat(underTest.build(caseData, caseDataBefore)).isEqualTo(new GeneratedOrderEvent(caseData,
            document,
            TRANSLATION_REQUIREMENT,
            ORDER_TITLE));
    }
}
