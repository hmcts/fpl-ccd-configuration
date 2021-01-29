package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class SendToGatekeeperControllerAboutToSubmitTest extends AbstractControllerTest {

    SendToGatekeeperControllerAboutToSubmitTest() {
        super("send-to-gatekeeper");
    }

    @Test
    void shouldPopulateStandardDirections() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(testHearing()))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(callbackRequest));

        assertThat(updatedCaseData.getAllParties()).hasSize(5);
        assertThat(updatedCaseData.getAllPartiesCustom()).isNull();
        assertThat(updatedCaseData.getLocalAuthorityDirections()).hasSize(7);
        assertThat(updatedCaseData.getLocalAuthorityDirectionsCustom()).isNull();
        assertThat(updatedCaseData.getCourtDirections()).hasSize(1);
        assertThat(updatedCaseData.getCourtDirectionsCustom()).isNull();
        assertThat(updatedCaseData.getCafcassDirections()).hasSize(3);
        assertThat(updatedCaseData.getCafcassDirectionsCustom()).isNull();
        assertThat(updatedCaseData.getOtherPartiesDirections()).hasSize(1);
        assertThat(updatedCaseData.getOtherPartiesDirectionsCustom()).isNull();
        assertThat(updatedCaseData.getRespondentDirections()).hasSize(1);
        assertThat(updatedCaseData.getRespondentDirectionsCustom()).isNull();
    }

    private static HearingBooking testHearing() {
        return HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(RandomUtils.nextInt(0, 100)))
            .build();
    }
}
