package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@ExtendWith(MockitoExtension.class)
class RemoveOrderServiceTest {

    private static final LocalDate NOW = LocalDate.now();
    private static final java.util.UUID REMOVED_UUID = java.util.UUID.randomUUID();
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private OrderRemovalActions orderRemovalActions;
    @Mock
    private OrderRemovalAction orderRemovalAction;
    @Mock
    private RemovableOrder removableOrder;
    @Mock
    private CaseDetailsMap data;
    @Mock
    private CaseData caseData;
    @Mock
    private CaseData caseDataBefore;

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

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .orderCollection(generatedOrders)
            .sealedCMOs(caseManagementOrders)
            .standardDirectionOrder(standardDirectionOrder)
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
                    formatLocalDateToString(NOW, "d MMMM yyyy"))),
                buildListElement(SDO_ID, format("Gatekeeping order - %s",
                    formatLocalDateToString(NOW, "d MMMM yyyy")))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldUseExpectedRemovalActionWhenRemovingAnOrder() {
        when(orderRemovalActions.getAction(removableOrder)).thenReturn(orderRemovalAction);

        underTest.removeOrderFromCase(caseData, data, REMOVED_UUID, removableOrder);

        verify(orderRemovalAction).remove(caseData, data, REMOVED_UUID, removableOrder);
    }

    @Test
    void shouldUseExpectedRemovalActionWhenPreparingCaseFields() {
        when(orderRemovalActions.getAction(removableOrder)).thenReturn(orderRemovalAction);

        underTest.populateSelectedOrderFields(caseData, data, REMOVED_UUID, removableOrder);

        verify(orderRemovalAction).populateCaseFields(caseData, data, REMOVED_UUID, removableOrder);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateGetRemovedSDOArgumentsSource")
    void shouldReturnRemovedSDO(
        String testName,
        List<Element<StandardDirectionOrder>> hiddenSDOs,
        List<Element<StandardDirectionOrder>> previousHiddenSDOs,
        Element<StandardDirectionOrder> expectedRemovedSDO
    ) {
        Optional<StandardDirectionOrder> removedOrder = underTest.getRemovedSDO(hiddenSDOs, previousHiddenSDOs);

        if (expectedRemovedSDO == null) {
            assertThat(removedOrder).isEmpty();
        } else {
            assertThat(removedOrder).isNotEmpty().containsSame(expectedRemovedSDO.getValue());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateGetRemovedCMOArgumentsSource")
    void shouldReturnRemovedCMO(
        String testName,
        List<Element<CaseManagementOrder>> hiddenCMOs,
        List<Element<CaseManagementOrder>> previousHiddenCMOs,
        Element<CaseManagementOrder> expectedRemovedCMO
    ) {
        Optional<CaseManagementOrder> removedOrder = underTest.getRemovedCMO(hiddenCMOs, previousHiddenCMOs);

        if (expectedRemovedCMO == null) {
            assertThat(removedOrder).isEmpty();
        } else {
            assertThat(removedOrder).isNotEmpty().containsSame(expectedRemovedCMO.getValue());
        }
    }

    private static Stream<Arguments> generateGetRemovedSDOArgumentsSource() {
        Element<StandardDirectionOrder> hiddenSDO1 = element(StandardDirectionOrder.builder().build());
        Element<StandardDirectionOrder> hiddenSDO2 = element(StandardDirectionOrder.builder().build());

        return Stream.of(
            Arguments.of("A SDO is removed", singletonList(hiddenSDO2), emptyList(), hiddenSDO2),
            Arguments.of(
                "A new SDO is removed", List.of(hiddenSDO1, hiddenSDO2), singletonList(hiddenSDO1), hiddenSDO2),
            Arguments.of("No SDOs removed", List.of(hiddenSDO2, hiddenSDO1), List.of(hiddenSDO2, hiddenSDO1), null),
            Arguments.of("No Hidden SDOs exist", emptyList(), emptyList(), null)
        );
    }

    private static Stream<Arguments> generateGetRemovedCMOArgumentsSource() {
        Element<CaseManagementOrder> hiddenCMO1 = element(CaseManagementOrder.builder().build());
        Element<CaseManagementOrder> hiddenCMO2 = element(CaseManagementOrder.builder().build());

        return Stream.of(
            Arguments.of("A CMO is removed", singletonList(hiddenCMO2), emptyList(), hiddenCMO2),
            Arguments.of(
                "A new CMO is removed", List.of(hiddenCMO1, hiddenCMO2), singletonList(hiddenCMO1), hiddenCMO2),
            Arguments.of("No CMOs removed", List.of(hiddenCMO2, hiddenCMO1), List.of(hiddenCMO2, hiddenCMO1), null),
            Arguments.of("No Hidden CMOs exist", emptyList(), emptyList(), null)
        );
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
