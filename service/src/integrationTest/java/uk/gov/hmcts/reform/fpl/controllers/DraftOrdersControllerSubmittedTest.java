package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
public class DraftOrdersControllerSubmittedTest extends AbstractControllerTest {
    private static final Long CASE_ID = 1L;
    private static final String PREPARE_FOR_HEARING_EVENT = "internal-changeState:Gatekeeping->PREPARE_FOR_HEARING";
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.builder().build();

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    DraftOrdersControllerSubmittedTest() {
        super("draft-standard-directions");
    }

    @Test
    void submittedCallbackShouldTriggerStateChangeWhenOrderIsMarkedAsFinal() {
        makeRequestWithOrderStatus(OrderStatus.SEALED);

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, PREPARE_FOR_HEARING_EVENT);
    }

    @Test
    void submittedCallbackShouldNotTriggerStateChangeWhenOrderIsStillInDraftState() {
        makeRequestWithOrderStatus(OrderStatus.DRAFT);

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), eq(PREPARE_FOR_HEARING_EVENT));
    }

    private void makeRequestWithOrderStatus(OrderStatus status) {
        Order order = Order.builder().orderStatus(status).orderDoc(DOCUMENT_REFERENCE).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of("standardDirectionOrder", order,
                    "caseLocalAuthority", "example",
                    "respondents1", List.of(
                        Map.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().plusDays(1))
                                    .lastName("Moley")
                                    .build())
                                .build()))))
                .build())
            .build();
        postSubmittedEvent(request);
    }
}
