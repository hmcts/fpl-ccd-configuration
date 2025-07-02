package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerColleaguesMidEventTest extends AbstractCallbackTest {

    ApplicantLocalAuthorityControllerColleaguesMidEventTest() {
        super("enter-local-authority");
    }

    @Test
    void shouldValidateColleaguesEmails() {
        final Colleague colleague1 = Colleague.builder()
            .email("test")
            .build();

        final Colleague colleague2 = Colleague.builder()
            .email("test@test.com")
            .build();

        final Colleague colleague3 = Colleague.builder()
            .email("test@test")
            .build();

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .applicantContact(colleague1)
            .applicantContactOthers(wrapElements(colleague2, colleague3))
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "colleagues");

        assertThat(response.getErrors()).containsExactly(
            "Main contact 1: Enter an email address in the correct format, for example name@example.com",
            "Other contact 2: Enter an email address in the correct format, for example name@example.com");
    }
}
