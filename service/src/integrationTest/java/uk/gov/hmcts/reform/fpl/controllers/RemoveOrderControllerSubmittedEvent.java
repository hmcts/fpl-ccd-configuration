package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerSubmittedEvent extends AbstractControllerTest {
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String CASE_ID = "12345";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    RemoveOrderControllerSubmittedEvent() {
        super("remove-order");
    }

    @Test
    void shouldPublishPopulateStandardDirectionsEventIfNewSDOHasBeenRemoved() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = new ArrayList<>();
        hiddenSDOs.add(element(previousSDO));

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(
            Map.of("hiddenStandardDirectionOrders", hiddenSDOs)).build();

        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(12345L),
            eq("populateSDO"),
            anyMap());
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
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
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

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(12345L),
            eq("populateSDO"),
            anyMap());
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

        verifyNoMoreInteractions(coreCaseDataService);
    }
}
