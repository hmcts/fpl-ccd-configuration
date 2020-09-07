package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_CTSC_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;

@ActiveProfiles("integration-test")
@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class StandardDirectionsOrderControllerSubmittedTest extends AbstractControllerTest {
    private static final Long CASE_ID = 1L;
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final DocumentReference DOCUMENT_REFERENCE = TestDataHelper.testDocumentReference();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    StandardDirectionsOrderControllerSubmittedTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldNotTriggerSDOEventWhenDraft() {
        postSubmittedEvent(buildCallbackRequest(DRAFT));

        verify(applicationEventPublisher, never()).publishEvent(StandardDirectionsOrderIssuedEvent.class);
    }

    @Test
    void shouldNotTriggerSendDocumentEventWhenDraft() {
        postSubmittedEvent(buildCallbackRequest(DRAFT));

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), eq(SEND_DOCUMENT_EVENT), any());
    }

    @Test
    void shouldTriggerSDOEventWhenSubmitted() throws Exception {
        postSubmittedEvent(buildCallbackRequest(SEALED));

        verify(notificationClient).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            "cafcass@cafcass.com",
            cafcassParameters(),
            NOTIFICATION_REFERENCE
        );

        verify(notificationClient).sendEmail(
            eq(STANDARD_DIRECTION_ORDER_ISSUED_CTSC_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );
    }

    @Test
    void shouldTriggerSendDocumentEventWhenSubmitted() {
        postSubmittedEvent(buildCallbackRequest(SEALED));

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", DOCUMENT_REFERENCE));
    }

    private Map<String, Object> cafcassParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", "cafcass")
            .put("familyManCaseNumber", "")
            .put("leadRespondentsName", "Moley")
            .put("hearingDate", "20 October 2020")
            .put("reference", String.valueOf(CASE_ID))
            .put("caseUrl", String.format("http://fake-url/cases/case-details/%s", CASE_ID))
            .build();
    }

    private CallbackRequest buildCallbackRequest(OrderStatus status) {
        StandardDirectionOrder order = StandardDirectionOrder.builder()
            .orderStatus(status)
            .orderDoc(DOCUMENT_REFERENCE)
            .build();

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of(
                    HEARING_DETAILS_KEY, List.of(
                        Element.builder()
                            .value(HearingBooking.builder()
                                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                                .endDate(LocalDateTime.of(2020, 11, 20, 11, 11, 11))
                                .build())
                            .build()),
                    "respondents1", List.of(
                        Map.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(dateNow().plusDays(1))
                                    .lastName("Moley")
                                    .relationshipToChild("Uncle")
                                    .build())
                                .build()
                        )
                    ),
                    "standardDirectionOrder", order,
                    "caseLocalAuthority", "example"))
                .build())
            .build();
    }
}
