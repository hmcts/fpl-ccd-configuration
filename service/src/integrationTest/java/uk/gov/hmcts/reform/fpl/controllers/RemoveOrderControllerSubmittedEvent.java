package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.handlers.PopulateStandardDirectionsHandler;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerSubmittedEvent extends AbstractControllerTest {
    @MockBean
    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    RemoveOrderControllerSubmittedEvent() {
        super("remove-order");
    }

    @Test
    void shouldPublishPopulateStandardDirectionsEventIfNewSDOHasBeenRemoved() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = new ArrayList<>();
        hiddenSDOs.add(element(previousSDO));

        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("hiddenStandardDirectionOrders", hiddenSDOs)).build();

        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(populateStandardDirectionsHandler).populateStandardDirections(any(
            PopulateStandardDirectionsEvent.class));
    }

    @Test
    void shouldPublishPopulateStandardDirectionsEventIfAnAdditionalSDOHasBeenRemoved() {
        Element<StandardDirectionOrder> newSDO = element(StandardDirectionOrder.builder().build());
        Element<StandardDirectionOrder> previousSDO = element(StandardDirectionOrder.builder().build());

        List<Element<StandardDirectionOrder>> previousHiddenSDOs = new ArrayList<>();
        previousHiddenSDOs.add(previousSDO);

        List<Element<StandardDirectionOrder>> hiddenSDOs = new ArrayList<>();
        hiddenSDOs.add(previousSDO);
        hiddenSDOs.add(newSDO);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "hiddenStandardDirectionOrders", hiddenSDOs
            ))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of(
                "hiddenStandardDirectionOrders", previousHiddenSDOs
            ))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(populateStandardDirectionsHandler).populateStandardDirections(any(
            PopulateStandardDirectionsEvent.class));
    }

    @Test
    void shouldNotPublishPopulateStandardDirectionsEventIfASDOHasNotBeenRemoved() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = new ArrayList<>();
        hiddenSDOs.add(element(previousSDO));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "hiddenStandardDirectionOrders", hiddenSDOs,
                "hiddenCaseManagementOrders", List.of(element(CaseManagementOrder.builder().build()))
            ))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of(
                "hiddenStandardDirectionOrders", hiddenSDOs
            ))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(populateStandardDirectionsHandler, never()).populateStandardDirections(any(
            PopulateStandardDirectionsEvent.class));
    }
}
