package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerOrganisationMidEventTest extends AbstractCallbackTest {

    ApplicantLocalAuthorityControllerOrganisationMidEventTest() {
        super("enter-local-authority");
    }

    private final DynamicList pbaNumberDynamicList = DynamicList.builder()
        .value(DynamicListElement.builder()
            .code("1234567")
            .build())
        .build();

    @Test
    void shouldValidateEmailAndPbaNumberIfPresent() {

        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .email("test")
                .pbaNumberDynamicList(pbaNumberDynamicList)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .isCTSCUser(YesNo.NO)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "organisation");

        assertThat(response.getErrors()).containsExactly(
            "Payment by account (PBA) number must include 7 numbers",
            "Enter an email address in the correct format, for example name@example.com"
        );
    }

    @Test
    void shouldValidateManualPbaNumberForCTSCUser() {
        final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .email("test")
                .pbaNumber("1234567")
                .pbaNumberDynamicList(null)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorityEventData(eventData)
            .isCTSCUser(YesNo.YES)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "organisation");

        assertThat(response.getErrors()).containsExactly(
            "Payment by account (PBA) number must include 7 numbers",
            "Enter an email address in the correct format, for example name@example.com"
        );
    }
}
