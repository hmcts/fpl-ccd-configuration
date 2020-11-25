package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@ExtendWith(MockitoExtension.class)
class RemoveOrderServiceTest {

    private static final LocalDate NOW = LocalDate.now();
    private static final java.util.UUID REMOVED_UUID = java.util.UUID.randomUUID();

    @Mock
    private OrderRemovalActions orderRemovalActions;
    @Mock
    private GeneratedOrderRemovalAction generatedOrderRemovalAction;
    @Mock
    private CMOOrderRemovalAction cmoOrderRemovalAction;
    @Mock
    private RemovableOrder removableOrder;
    @Mock
    private CaseDetailsMap data;
    @Mock
    private CaseData caseData;

    @InjectMocks
    private RemoveOrderService underTest;

    @Test
    void shouldMakeDynamicListOfBlankOrders() {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(BLANK_ORDER, "order 2", "16 July 2020"))
        );

        CaseData caseData = CaseData.builder()
            .orderCollection(generatedOrders)
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(generatedOrders.get(1).getId(), "order 2 - 16 July 2020")
            ))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldMakeDynamicListOfMixedOrderTypes() {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(CARE_ORDER, "order 2", "16 July 2020")),
            element(buildOrder(EMERGENCY_PROTECTION_ORDER, "order 3", "17 August 2020")),
            element(buildOrder(SUPERVISION_ORDER, "order 4", "18 September 2020"))
        );

        List<Element<CaseManagementOrder>> caseManagementOrders = buildCaseManagementOrders();

        CaseData caseData = CaseData.builder()
            .orderCollection(generatedOrders)
            .sealedCMOs(caseManagementOrders)
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(generatedOrders.get(1).getId(), "order 2 - 16 July 2020"),
                buildListElement(generatedOrders.get(2).getId(), "order 3 - 17 August 2020"),
                buildListElement(generatedOrders.get(3).getId(), "order 4 - 18 September 2020"),
                buildListElement(caseManagementOrders.get(0).getId(), format("Case management order - %s",
                    formatLocalDateToString(NOW, "d MMMM yyyy")))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldUseGeneratedOrderRemovalActionWhenRemovingGeneratedOrder() {
        when(orderRemovalActions.getAction(REMOVED_UUID, removableOrder)).thenReturn(generatedOrderRemovalAction);

        underTest.removeOrderFromCase(caseData, data, REMOVED_UUID, removableOrder);

        verify(generatedOrderRemovalAction).remove(caseData, data, REMOVED_UUID, removableOrder);
        verifyNoMoreInteractions(generatedOrderRemovalAction);
        verifyNoInteractions(cmoOrderRemovalAction);
    }

    @Test
    void shouldUseCMORemovalActionWhenRemovingCMO() {
        when(orderRemovalActions.getAction(REMOVED_UUID, removableOrder)).thenReturn(cmoOrderRemovalAction);

        underTest.removeOrderFromCase(caseData, data, REMOVED_UUID, removableOrder);

        verify(cmoOrderRemovalAction).remove(caseData, data, REMOVED_UUID, removableOrder);
        verifyNoMoreInteractions(cmoOrderRemovalAction);
        verifyNoInteractions(generatedOrderRemovalAction);
    }

    @Test
    void shouldUseGeneratedOrderPopulateCaseFieldActionWhenPopulatingCaseFieldsForGeneratedOrder() {
        when(orderRemovalActions.getAction(REMOVED_UUID, removableOrder)).thenReturn(generatedOrderRemovalAction);

        underTest.populateSelectedOrderFields(caseData, data, REMOVED_UUID, removableOrder);

        verify(generatedOrderRemovalAction).populateCaseFields(caseData, data, REMOVED_UUID, removableOrder);
        verifyNoMoreInteractions(generatedOrderRemovalAction);
        verifyNoInteractions(cmoOrderRemovalAction);
    }

    @Test
    void shouldUseCMOPopulateCaseFieldActionWhenPopulatingCaseFieldsForCMO() {
        when(orderRemovalActions.getAction(REMOVED_UUID, removableOrder)).thenReturn(cmoOrderRemovalAction);

        underTest.populateSelectedOrderFields(caseData, data, REMOVED_UUID, removableOrder);

        verify(cmoOrderRemovalAction).populateCaseFields(caseData, data, REMOVED_UUID, removableOrder);
        verifyNoMoreInteractions(cmoOrderRemovalAction);
        verifyNoInteractions(generatedOrderRemovalAction);
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String dateOfIssue) {
        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .title(title)
            .dateOfIssue(dateOfIssue)
            .build();
    }

    private List<Element<CaseManagementOrder>> buildCaseManagementOrders() {
        return List.of(
            element(CaseManagementOrder.builder()
                .status(APPROVED)
                .dateIssued(NOW)
                .build()),
            element(CaseManagementOrder.builder()
                .status(DRAFT)
                .dateIssued(NOW)
                .build())
        );
    }
}
