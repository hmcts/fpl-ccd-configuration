package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateAllAvailableStatesSource")
    void shouldMakeDynamicListOfBlankOrdersInAllExpectedStates(State state) {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(BLANK_ORDER, "order 2", "16 July 2020"))
        );

        CaseData caseData = CaseData.builder()
            .orderCollection(generatedOrders)
            .state(state)
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateAllAvailableStatesSource")
    void shouldMakeDynamicListOfMixedNonSDOrderTypesInAllExpectedStates(State state) {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(CARE_ORDER, "order 2", "16 July 2020")),
            element(buildOrder(EMERGENCY_PROTECTION_ORDER, "order 3", "17 August 2020")),
            element(buildOrder(SUPERVISION_ORDER, "order 4", "18 September 2020"))
        );

        List<Element<HearingOrder>> sealedCaseManagementOrders = buildSealedCaseManagementOrders();

        Element<HearingOrder> draftCMOOne = element(buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> draftCMOTwo = element(buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> agreedCMO = element(buildPastHearingOrder(AGREED_CMO));
        Element<HearingOrder> draftOrder = element(
            HearingOrder.builder().type(C21)
                .title("test order")
                .order(DocumentReference.builder().filename("order.doc").build())
                .dateSent(NOW.minusDays(1)).build());

        CaseData caseData = CaseData.builder()
            .state(state)
            .orderCollection(generatedOrders)
            .sealedCMOs(sealedCaseManagementOrders)
            .hearingOrdersBundlesDrafts(List.of(
                element(HearingOrdersBundle.builder().orders(newArrayList(draftCMOOne)).build()),
                element(HearingOrdersBundle.builder().orders(
                    newArrayList(draftCMOTwo, draftOrder)).build()),
                element(HearingOrdersBundle.builder().orders(newArrayList(agreedCMO)).build())))
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(generatedOrders.get(1).getId(), "order 2 - 16 July 2020"),
                buildListElement(generatedOrders.get(2).getId(), "order 3 - 17 August 2020"),
                buildListElement(generatedOrders.get(3).getId(), "order 4 - 18 September 2020"),
                buildListElement(sealedCaseManagementOrders.get(0).getId(),
                    format("Sealed case management order issued on %s",
                        formatLocalDateToString(NOW, "d MMMM yyyy"))),
                buildListElement(draftCMOOne.getId(), format("Draft case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftCMOOne.getValue().getDocument().getFilename())),
                buildListElement(draftCMOTwo.getId(), format("Draft case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftCMOTwo.getValue().getDocument().getFilename())),
                buildListElement(draftOrder.getId(), format("Draft order sent on %s for %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftOrder.getValue().getTitle(),
                    draftOrder.getValue().getDocument().getFilename())),
                buildListElement(agreedCMO.getId(), format("Agreed case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    agreedCMO.getValue().getDocument().getFilename()))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateAllAvailableStatesSource")
    void shouldMakeDynamicListOfCMOsFromDraftUploadedCMOsAndHearingOrdersBundlesInAllExpectedStates(State state) {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020"))
        );

        List<Element<HearingOrder>> sealedCaseManagementOrders = buildSealedCaseManagementOrders();

        Element<HearingOrder> draftCMOOne = element(buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> draftCMOTwo = element(buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> legacyDraftCMO = element(HearingOrder.builder()
            .title("test order")
            .order(DocumentReference.builder().filename("order.doc").build())
            .dateSent(NOW.minusDays(1)).build());
        Element<HearingOrder> draftOrder =
            element(HearingOrder.builder().type(C21)
                .title("test order")
                .order(DocumentReference.builder().filename("order.doc").build())
                .dateSent(NOW.minusDays(1)).build());

        CaseData caseData = CaseData.builder()
            .state(state)
            .orderCollection(generatedOrders)
            .sealedCMOs(sealedCaseManagementOrders)
            .draftUploadedCMOs(List.of(draftCMOTwo, legacyDraftCMO))
            .hearingOrdersBundlesDrafts(List.of(
                element(HearingOrdersBundle.builder().orders(newArrayList(draftCMOOne)).build()),
                element(HearingOrdersBundle.builder().orders(newArrayList(draftCMOTwo, draftOrder)).build())))
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(sealedCaseManagementOrders.get(0).getId(),
                    format("Sealed case management order issued on %s",
                        formatLocalDateToString(NOW, "d MMMM yyyy"))),
                buildListElement(draftCMOOne.getId(), format("Draft case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftCMOOne.getValue().getDocument().getFilename())),
                buildListElement(draftCMOTwo.getId(), format("Draft case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftCMOTwo.getValue().getDocument().getFilename())),
                buildListElement(draftOrder.getId(), format("Draft order sent on %s for %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftOrder.getValue().getTitle(),
                    draftOrder.getValue().getDocument().getFilename())),
                buildListElement(legacyDraftCMO.getId(), format("Draft case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    legacyDraftCMO.getValue().getDocument().getFilename()))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateAllAvailableStatesSource")
    void shouldMakeDynamicListOfDraftHearingOrdersInAllExpectedStatesWhenNoHearingOrdersBundlesExist(State state) {
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020"))
        );

        List<Element<HearingOrder>> sealedCaseManagementOrders = buildSealedCaseManagementOrders();

        Element<HearingOrder> draftCMO = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
        Element<HearingOrder> agreedCMO = element(UUID.randomUUID(), buildPastHearingOrder(AGREED_CMO));
        Element<HearingOrder> draftOrder = element(UUID.randomUUID(), buildPastHearingOrder(C21));

        HearingOrdersBundle bundle1 = HearingOrdersBundle.builder()
            .orders(newArrayList(draftCMO, draftOrder)).build();

        HearingOrdersBundle bundle2 = HearingOrdersBundle.builder()
            .orders(newArrayList(agreedCMO)).build();

        CaseData caseData = CaseData.builder()
            .state(state)
            .orderCollection(generatedOrders)
            .sealedCMOs(sealedCaseManagementOrders)
            .draftUploadedCMOs(List.of(draftCMO, agreedCMO))
            .hearingOrdersBundlesDrafts(wrapElements(bundle1, bundle2))
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(sealedCaseManagementOrders.get(0).getId(),
                    format("Sealed case management order issued on %s",
                        formatLocalDateToString(NOW, "d MMMM yyyy"))),
                buildListElement(draftCMO.getId(),
                    format("Draft case management order sent on %s, %s",
                        formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                        draftCMO.getValue().getDocument().getFilename())),
                buildListElement(draftOrder.getId(), format("Draft order sent on %s for %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    draftOrder.getValue().getTitle(),
                    draftOrder.getValue().getDocument().getFilename())),
                buildListElement(agreedCMO.getId(), format("Agreed case management order sent on %s, %s",
                    formatLocalDateToString(NOW.minusDays(1), "d MMMM yyyy"),
                    agreedCMO.getValue().getDocument().getFilename()))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = State.class, names = {"SUBMITTED", "GATEKEEPING", "CASE_MANAGEMENT", "CLOSED"})
    void shouldMakeDynamicListOfSDOrderTypesInExpectedCaseStates(State state) {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .state(state)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(SDO_ID, format("Gatekeeping order - %s",
                    formatLocalDateToString(NOW, "d MMMM yyyy")))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = State.class, names = {"SUBMITTED", "GATEKEEPING", "CASE_MANAGEMENT", "CLOSED"})
    void shouldMakeDynamicListOfUDOrderTypesInExpectedCaseStates(State state) {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .state(state)
            .urgentDirectionsOrder(standardDirectionOrder)
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(SDO_ID, format("Gatekeeping order - %s",
                    formatLocalDateToString(NOW, "d MMMM yyyy")))))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldNotBuildDynamicListOfSDOrdersInFinalHearingState() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .state(FINAL_HEARING)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of())
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldNotBuildDynamicListOfUDOrdersInFinalHearingState() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .state(FINAL_HEARING)
            .urgentDirectionsOrder(standardDirectionOrder)
            .build();

        DynamicList listOfOrders = underTest.buildDynamicListOfOrders(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of())
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
        List<Element<HearingOrder>> hiddenCMOs,
        List<Element<HearingOrder>> previousHiddenCMOs,
        Element<HearingOrder> expectedRemovedCMO
    ) {
        Optional<HearingOrder> removedOrder = underTest.getRemovedCMO(hiddenCMOs, previousHiddenCMOs);

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
        Element<HearingOrder> hiddenCMO1 = element(HearingOrder.builder().build());
        Element<HearingOrder> hiddenCMO2 = element(HearingOrder.builder().build());

        return Stream.of(
            Arguments.of("A CMO is removed", singletonList(hiddenCMO2), emptyList(), hiddenCMO2),
            Arguments.of(
                "A new CMO is removed", List.of(hiddenCMO1, hiddenCMO2), singletonList(hiddenCMO1), hiddenCMO2),
            Arguments.of("No CMOs removed", List.of(hiddenCMO2, hiddenCMO1), List.of(hiddenCMO2, hiddenCMO1), null),
            Arguments.of("No Hidden CMOs exist", emptyList(), emptyList(), null)
        );
    }

    private static Stream<Arguments> generateAllAvailableStatesSource() {
        return Stream.of(
            Arguments.of(SUBMITTED),
            Arguments.of(GATEKEEPING),
            Arguments.of(CASE_MANAGEMENT),
            Arguments.of(FINAL_HEARING),
            Arguments.of(CLOSED));
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

    private List<Element<HearingOrder>> buildSealedCaseManagementOrders() {
        return List.of(
            element(HearingOrder.builder()
                .type(AGREED_CMO)
                .title("test order")
                .order(DocumentReference.builder().filename("order.doc").build())
                .status(APPROVED)
                .dateIssued(NOW)
                .build()));
    }

    private HearingOrder buildPastHearingOrder(HearingOrderType type) {
        return HearingOrder.builder()
            .type(type)
            .title("test order")
            .order(DocumentReference.builder().filename("order.doc").build())
            .status((type == AGREED_CMO || type == C21) ? SEND_TO_JUDGE : DRAFT)
            .dateIssued((type == AGREED_CMO || type == C21) ? NOW : null)
            .dateSent(NOW.minusDays(1))
            .build();
    }
}
