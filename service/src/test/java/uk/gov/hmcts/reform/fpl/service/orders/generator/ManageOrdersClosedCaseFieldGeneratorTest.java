package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.updaters.ChildrenSmartFinalOrderUpdater;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C26_SECURE_ACCOMMODATION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32A_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32B_DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
public class ManageOrdersClosedCaseFieldGeneratorTest {
    private static final LocalDate APPROVAL_DATE = LocalDate.of(2010, 01, 01);
    private static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2010, 01, 01, 10, 00, 00);
    private static final LocalDate TODAY = LocalDate.of(2012, 12, 22);
    private static final LocalDateTime NOW = TODAY.atStartOfDay();

    @Mock
    private Time time;

    @Mock
    private ChildrenSmartFinalOrderUpdater childrenSmartFinalOrderUpdater;

    @InjectMocks
    private ManageOrdersClosedCaseFieldGenerator underTest;

    @Test
    void shouldCloseCaseWhenFinalOrder() {
        CaseData caseData = buildCaseData("Yes", "Yes", C32A_CARE_ORDER, APPROVAL_DATE, null);

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
            "state", CLOSED,
            "closeCaseTabField", CloseCase.builder().date(APPROVAL_DATE).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldNotCloseCaseAndReturnEmptyMapWhenNotFinalOrder() {
        CaseData caseData = buildCaseData("Yes", "No", C32B_DISCHARGE_OF_CARE_ORDER, APPROVAL_DATE, null);

        Map<String, Object> generatedData = underTest.generate(caseData);

        assertThat(generatedData).isEmpty();
    }

    @Test
    void shouldUpdateChildrenAndNotCloseCase() {

        CaseData caseData = buildCaseData("No", "No", C32A_CARE_ORDER, APPROVAL_DATE, null);
        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());

        Map<String, Object> generatedData = underTest.generate(caseData);

        assertThat(generatedData).doesNotContainEntry("state", "CLOSED");
        assertThat(generatedData).doesNotContainKey("closeCaseTabField");
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldNotUpdateChildrenWhenNotFinalOrder() {
        CaseData caseData = buildCaseData("No", "No", C21_BLANK_ORDER, APPROVAL_DATE, null);

        Map<String, Object> generatedData = underTest.generate(caseData);

        assertThat(generatedData).isEqualTo(Collections.emptyMap());
    }

    @Test
    void shouldUpdateChildrenWhenUserHasSelectedFinalOrder() {
        CaseData caseData = buildCaseData("No", "Yes", C21_BLANK_ORDER, APPROVAL_DATE, null);

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());

        Map<String, Object> generatedData = underTest.generate(caseData);

        assertThat(generatedData).containsKey("children1");
    }

    private CaseData buildCaseData(String closeCase, String isFinalOrder, Order order, LocalDate approvalDate,
                                   LocalDateTime approvalDateTime) {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersCloseCase(closeCase)
            .manageOrdersIsFinalOrder(isFinalOrder)
            .manageOrdersType(order)
            .manageOrdersApprovalDate(approvalDate)
            .manageOrdersApprovalDateTime(approvalDateTime)
            .build();

        return CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .build();
    }

    @ParameterizedTest
    @MethodSource("generateOrderStates")
    void shouldCloseCaseWhenOrderIsDefaultFinal(String isFinalOrder) {
        CaseData caseData = buildCaseData("Yes", isFinalOrder, C32A_CARE_ORDER, APPROVAL_DATE, null);

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
                .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
                "state", CLOSED,
                "closeCaseTabField", CloseCase.builder().date(APPROVAL_DATE).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldCloseCaseWhenOrderIsDefaultFinalWhenNoApprovalDate() {
        CaseData caseData = buildCaseData("Yes", "Yes", C32A_CARE_ORDER, null, null);

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
            "state", CLOSED,
            "closeCaseTabField", CloseCase.builder().date(time.now().toLocalDate()).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldCloseCaseWhenOrderIsDefaultFinalAndApprovalDateTime() {
        CaseData caseData = buildCaseData("Yes", "Yes", C26_SECURE_ACCOMMODATION_ORDER, null, APPROVAL_DATE_TIME);

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
            "state", CLOSED,
            "closeCaseTabField", CloseCase.builder().date(APPROVAL_DATE).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldCloseCaseWhenOrderIsDefaultFinalWhenNoApprovalDateForBlankOrder() {
        CaseData caseData = buildCaseData("Yes", "Yes", C21_BLANK_ORDER, null, null);

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
            "state", CLOSED,
            "closeCaseTabField", CloseCase.builder().date(time.now().toLocalDate()).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldCloseCaseWhenOrderIsDefaultFinalAndApprovalDateIsLaterThanExistingFinalOrder() {
        CaseData caseData = buildCaseData("Yes", "Yes", C26_SECURE_ACCOMMODATION_ORDER,
            APPROVAL_DATE.minusDays(100), null);
        caseData = caseData.toBuilder().orderCollection(wrapElements(GeneratedOrder.builder()
                .markedFinal(YesNo.YES.getValue())
                .dateTimeIssued(APPROVAL_DATE_TIME)
                .approvalDate(APPROVAL_DATE)
                .build()))
            .build();

        when(childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
            "state", CLOSED,
            "closeCaseTabField", CloseCase.builder().date(APPROVAL_DATE).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    private static Stream<Arguments> generateOrderStates() {
        String data = null;
        return Stream.of(
                Arguments.of(YesNo.NO.getValue()),
                Arguments.of(YesNo.YES.getValue()),
                Arguments.of(data)
        );
    }
}
