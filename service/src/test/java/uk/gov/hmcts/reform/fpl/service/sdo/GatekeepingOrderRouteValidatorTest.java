package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatekeepingOrderRouteValidatorTest {
    private static final String URGENT_ROUTE_VALIDATION_MESSAGE = "An urgent hearing order has already been added to"
        + " this case. You can still add a gatekeeping order, if needed.";
    private static final String EVENT_ACCESS_VALIDATION_MESSAGE = "There is already a gatekeeping order for this case";

    private final GatekeepingOrderRouteValidator underTest = new GatekeepingOrderRouteValidator();

    @Test
    void allowAccessToEventShouldReturnErrorWhenSDOIsSealed() {
        StandardDirectionOrder sdo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder().standardDirectionOrder(sdo).build();
        when(sdo.isSealed()).thenReturn(true);

        assertThat(underTest.allowAccessToEvent(caseData)).isEqualTo(List.of(EVENT_ACCESS_VALIDATION_MESSAGE));
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenSDOIsNotSealed() {
        StandardDirectionOrder sdo = mock(StandardDirectionOrder.class);
        CaseData caseData = CaseData.builder().standardDirectionOrder(sdo).build();
        when(sdo.isSealed()).thenReturn(false);

        assertThat(underTest.allowAccessToEvent(caseData)).isEmpty();
    }

    @Test
    void allowAccessToEventShouldReturnNoErrorWhenSDOIsNotPresent() {
        CaseData caseData = CaseData.builder().standardDirectionOrder(null).build();

        assertThat(underTest.allowAccessToEvent(caseData)).isEmpty();
    }

    @Test
    void allowAccessToUrgentHearingRouteShouldReturnErrorWhenStateIsCaseManagement() {
        CaseData caseData = CaseData.builder().state(State.CASE_MANAGEMENT).build();

        assertThat(underTest.allowAccessToUrgentHearingRoute(caseData)).isEqualTo(List.of(
            URGENT_ROUTE_VALIDATION_MESSAGE
        ));
    }

    @Test
    void allowAccessToUrgentHearingRouteShouldNotReturnErrorWhenStateIsGatekeeping() {
        CaseData caseData = CaseData.builder().state(State.GATEKEEPING).build();

        assertThat(underTest.allowAccessToUrgentHearingRoute(caseData)).isEmpty();
    }
}
