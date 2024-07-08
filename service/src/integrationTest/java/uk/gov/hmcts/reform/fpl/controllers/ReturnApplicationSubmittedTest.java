package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.service.notify.NotificationClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.*;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_RETURNED_TO_THE_LA;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@WebMvcTest(ReturnApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
class ReturnApplicationSubmittedTest extends AbstractCallbackTest {
    private static final String NOTIFICATION_REFERENCE = "localhost/" + 12345;

    @MockBean
    private NotificationClient notificationClient;

    ReturnApplicationSubmittedTest() {
        super("return-application");
    }

    @Test
    void shouldNotifyTheLocalAuthorityWhenCaseReturned() throws Exception {
        CaseData caseData = CaseData.builder()
            .returnApplication(ReturnApplication.builder()
                .reason(List.of(ReturnedApplicationReasons.INCOMPLETE))
                .note("some note")
                .build())
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Dave").lastName("Davidson").build())
                .build()))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()))
            .id(12345L)
            .build();

        postSubmittedEvent(caseData);

        verify(notificationClient).sendEmail(
            eq(APPLICATION_RETURNED_TO_THE_LA), eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(), eq(NOTIFICATION_REFERENCE));
    }
}
