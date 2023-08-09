package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerOrganisationMidEventTest extends AbstractCallbackTest {

    ApplicantLocalAuthorityControllerOrganisationMidEventTest() {
        super("enter-local-authority");
    }

    @Test
    void shouldValidateEmailAndPbaNumberIfPresent() {

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .email("test")
                .pbaNumber("123")
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "organisation");

        assertThat(response.getErrors()).containsExactly(
            "Payment by account (PBA) number must include 7 numbers",
            "Enter an email address in the correct format, for example name@example.com"
        );
    }

    @Test
    void shouldNormaliseValidPbaNumber() {

        final String pbaNumber = "1234567";
        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .pbaNumber(pbaNumber)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "organisation"));

        final String expectedPbaNumber = "PBA" + pbaNumber;

        assertThat(updatedCaseData.getLocalAuthorityEventData().getLocalAuthority().getPbaNumber())
            .isEqualTo(expectedPbaNumber);
    }
}
