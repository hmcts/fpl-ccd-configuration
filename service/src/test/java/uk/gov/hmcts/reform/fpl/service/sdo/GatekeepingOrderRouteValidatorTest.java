package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ADD_URGENT_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.enums.Event.JUDICIAL_GATEKEEPNIG;
import static uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderRouteValidator.URGENT_DIRECTIONS_VALIDATION_MESSAGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class GatekeepingOrderRouteValidatorTest {
    private static final String URGENT_ROUTE_VALIDATION_MESSAGE = "An urgent hearing order has already been added to"
        + " this case. You can still add a gatekeeping "
        + "order, if needed.";
    private static final String EVENT_ACCESS_VALIDATION_MESSAGE = "There is already a gatekeeping order for this case";
    private static final String HEARING_DETAILS_REQUIRED = "You need to add hearing details for the notice of "
        + "proceedings";
    private static final String URGENT_DIRECTIONS_NOT_ALLOWED_MESSAGE =
        "There is a standard direction order on this case and the urgent direction event can not be used.";
    private static final List<Element<HearingBooking>> HEARINGS = List.of(element(mock(HearingBooking.class)));

    private final GatekeepingOrderRouteValidator underTest = new GatekeepingOrderRouteValidator();

    @Test
    void allowAccessToEventShouldReturnErrorWhenSDOIsSealed() {
        StandardDirectionOrder sdo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.UPLOAD)
            .standardDirectionOrder(sdo)
            .build();
        when(sdo.isSealed()).thenReturn(true);

        assertThat(underTest.allowAccessToEvent(caseData, JUDICIAL_GATEKEEPNIG.getId()))
            .isEqualTo(List.of(EVENT_ACCESS_VALIDATION_MESSAGE));
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenSDOIsNotSealed() {
        StandardDirectionOrder sdo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.UPLOAD)
            .standardDirectionOrder(sdo)
            .build();
        when(sdo.isSealed()).thenReturn(false);

        assertThat(underTest.allowAccessToEvent(caseData, JUDICIAL_GATEKEEPNIG.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenSDOIsNotPresent() {
        CaseData caseData = CaseData.builder().standardDirectionOrder(null).build();

        assertThat(underTest.allowAccessToEvent(caseData)).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnErrorWhenUDOIsSealed() {
        StandardDirectionOrder udo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .urgentDirectionsOrder(udo)
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER, OrderType.INTERIM_CARE_ORDER)).build())
            .build();
        when(udo.isSealed()).thenReturn(true);

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId()))
            .isEqualTo(List.of(URGENT_DIRECTIONS_VALIDATION_MESSAGE));
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenUDOIsNotSealed() {
        StandardDirectionOrder udo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .urgentDirectionsOrder(udo)
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER, OrderType.INTERIM_CARE_ORDER)).build())
            .build();
        when(udo.isSealed()).thenReturn(false);

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnErrorWhenNotCombinedOrEPO() {
        StandardDirectionOrder udo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .urgentDirectionsOrder(udo)
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId()))
            .isEqualTo(List.of("An urgent directions order is not required for this case."));
    }

    @Test
    void allowAccessToEventShouldNotReturnErrorWhenStandaloneEPOForJudicialGatekeeping() {
        StandardDirectionOrder udo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .urgentDirectionsOrder(udo)
            .orders(Orders.builder().orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, JUDICIAL_GATEKEEPNIG.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldNoReturnErrorWhenUDOIsNotSealedAndCombinedOrderRequested() {
        StandardDirectionOrder udo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .urgentDirectionsOrder(udo)
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER, OrderType.INTERIM_CARE_ORDER)).build())
            .build();
        when(udo.isSealed()).thenReturn(false);

        assertThat(underTest.allowAccessToEvent(caseData, JUDICIAL_GATEKEEPNIG.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenUDOIsSealedAndCombinedOrderRequested() {
        StandardDirectionOrder udo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .urgentDirectionsOrder(udo)
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER, OrderType.INTERIM_CARE_ORDER)).build())
            .build();
        when(udo.isSealed()).thenReturn(true);

        assertThat(underTest.allowAccessToEvent(caseData, JUDICIAL_GATEKEEPNIG.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenUDOIsNotPresent() {
        CaseData caseData = CaseData.builder()
            .urgentDirectionsOrder(null)
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER, OrderType.INTERIM_CARE_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenInterimCareOrderAlone() {
        CaseData caseData = CaseData.builder()
            .urgentDirectionsOrder(null)
            .orders(Orders.builder().orderType(List.of(OrderType.INTERIM_CARE_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenSecureAccommodationOrderAlone() {
        CaseData caseData = CaseData.builder()
            .urgentDirectionsOrder(null)
            .orders(Orders.builder().orderType(List.of(OrderType.SECURE_ACCOMMODATION_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenChildRecoveryOrderAlone() {
        CaseData caseData = CaseData.builder()
            .urgentDirectionsOrder(null)
            .orders(Orders.builder().orderType(List.of(OrderType.CHILD_RECOVERY_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenCombinedEPOAndICO() {
        CaseData caseData = CaseData.builder()
            .urgentDirectionsOrder(null)
            .orders(Orders.builder().orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER,
                OrderType.INTERIM_CARE_ORDER)).build())
            .build();

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId())).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnErrorWhenAddingUrgentDirectionButSDOIsSealed() {
        StandardDirectionOrder sdo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(GatekeepingOrderRoute.UPLOAD)
            .standardDirectionOrder(sdo)
            .orders(Orders.builder().orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER)).build())
            .build();
        when(sdo.isSealed()).thenReturn(true);

        assertThat(underTest.allowAccessToEvent(caseData, ADD_URGENT_DIRECTIONS.getId()))
            .isEqualTo(List.of(URGENT_DIRECTIONS_NOT_ALLOWED_MESSAGE));
    }

    @Test
    void allowAccessToUrgentHearingRouteShouldReturnErrorWhenStateIsCaseManagement() {
        CaseData caseData = CaseData.builder().hearingDetails(HEARINGS).state(State.CASE_MANAGEMENT).build();

        assertThat(underTest.allowAccessToUrgentHearingRoute(caseData)).isEqualTo(List.of(
            URGENT_ROUTE_VALIDATION_MESSAGE
        ));
    }

    @Test
    void allowAccessToUrgentHearingRouteShouldReturnErrorWhenStateIsGatekeepingAndNoHearingPresent() {
        CaseData caseData = CaseData.builder().hearingDetails(null).state(State.GATEKEEPING).build();

        assertThat(underTest.allowAccessToUrgentHearingRoute(caseData)).isEqualTo(List.of(
            HEARING_DETAILS_REQUIRED
        ));
    }

    @Test
    void allowAccessToUrgentHearingRouteShouldNotReturnErrorWhenStateIsGatekeepingAndHearingPresent() {
        CaseData caseData = CaseData.builder().hearingDetails(HEARINGS).state(State.GATEKEEPING).build();

        assertThat(underTest.allowAccessToUrgentHearingRoute(caseData)).isEmpty();
    }
}
