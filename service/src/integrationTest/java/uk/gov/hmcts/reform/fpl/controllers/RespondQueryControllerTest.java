package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.QUERY_RESPONDED;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(RaiseQueryController.class)
@OverrideAutoConfiguration(enabled = true)
public class RespondQueryControllerTest extends AbstractCallbackTest {

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationClient notificationClient;

    RespondQueryControllerTest() {
        super("respond-query");
    }

    @Test
    void shouldTriggerRespondQueryEventAndSendNotification() {
        when(userService.getUserDetailsById(any())).thenReturn(UserDetails.builder().email("test@test.com").build());
        Long caseId = 1L;
        String LaUserId = UUID.randomUUID().toString();
        String queryId = UUID.randomUUID().toString();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", LaUserId
                            )
                        )
                    )
                ))
            ))
            .build();

        CaseDetails caseDetailsAfter = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", LaUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "response",
                                "subject", "response",
                                "name", "CTSC Admin",
                                "createdOn", "2025-06-02T12:00:00.000Z",
                                "createdBy", UUID.randomUUID().toString(),
                                "parentId", queryId
                            )
                        )
                    )
                ))
            ))
            .build();

        postSubmittedEvent(toCallBackRequest(caseDetailsAfter, caseDetailsBefore));

        checkUntil(() -> verify(notificationClient).sendEmail(
                eq(QUERY_RESPONDED),
                eq("test@test.com"),
                any(),
                eq("localhost/" + caseId)
        ));
    }
}
