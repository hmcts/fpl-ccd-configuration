package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_JUDICIAL_MESSAGE_ADDED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerSubmittedTest extends AbstractControllerTest {
    private static final String JUDICIAL_MESSAGE_RECIPIENT = "recipient@test.com";
    private static final Long CASE_REFERENCE = 12345L;

    @MockBean
    private NotificationClient notificationClient;

    MessageJudgeControllerSubmittedTest() {
        super("message-judge");
    }

    @Test
    void shouldNotifyJudicialMessageRecipientWhenNewJudicialMessageAdded() throws NotificationClientException {
        JudicialMessage latestJudicialMessage = JudicialMessage.builder()
            .recipient(JUDICIAL_MESSAGE_RECIPIENT)
            .sender("sender@fpla.com")
            .urgency("High")
            .latestMessage("Some note")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(Map.of(
                "respondents1", List.of(
                    element(Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName("Davidson")
                            .build())
                        .build())),
                    "judicialMessages", List.of(
                        element(latestJudicialMessage),
                        element(JudicialMessage.builder()
                            .recipient("do_not_send@fpla.com")
                            .sender("someOthersender@fpla.com")
                            .urgency("High")
                            .build()))
            )).build();

        postSubmittedEvent(caseDetails);

        Map<String, Object> expectedData = Map.of(
            "respondentLastName", "Davidson",
            "caseUrl", "http://fake-url/cases/case-details/12345#JudicialMessagesTab",
            "callout", "^Davidson",
            "sender", "sender@fpla.com",
            "urgency", "High",
            "hasUrgency", "Yes",
            "note", "Some note"
        );

        verify(notificationClient).sendEmail(
            NEW_JUDICIAL_MESSAGE_ADDED_TEMPLATE, JUDICIAL_MESSAGE_RECIPIENT, expectedData, "localhost/12345");
    }
}
