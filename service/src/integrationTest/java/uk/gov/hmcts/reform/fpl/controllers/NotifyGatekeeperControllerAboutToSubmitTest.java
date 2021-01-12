package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.controllers.NotifyGatekeeperController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperControllerAboutToSubmitTest extends AbstractControllerTest {

    @SpyBean
    private StandardDirectionsService standardDirectionsService;

    NotifyGatekeeperControllerAboutToSubmitTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldPopulateStandardDirectionsWhenCaseInSubmittedState() {

        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .hearingDetails(wrapElements(testHearing()))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

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

    @Test
    void shouldNotPopulateStandardDirectionsWhenCaseNotInSubmittedState() {

        CaseData caseData = CaseData.builder()
            .state(State.GATEKEEPING)
            .cafcassDirections(wrapElements(testDirection()))
            .courtDirections(wrapElements(testDirection()))
            .respondentDirections(wrapElements(testDirection()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getAllParties())
            .isEqualTo(caseData.getAllParties());
        assertThat(updatedCaseData.getAllPartiesCustom())
            .isEqualTo(caseData.getAllPartiesCustom());
        assertThat(updatedCaseData.getLocalAuthorityDirections())
            .isEqualTo(caseData.getLocalAuthorityDirections());
        assertThat(updatedCaseData.getLocalAuthorityDirectionsCustom())
            .isEqualTo(caseData.getLocalAuthorityDirectionsCustom());
        assertThat(updatedCaseData.getCourtDirections())
            .isEqualTo(caseData.getCourtDirections());
        assertThat(updatedCaseData.getCourtDirectionsCustom())
            .isEqualTo(caseData.getCourtDirectionsCustom());
        assertThat(updatedCaseData.getCafcassDirections())
            .isEqualTo(caseData.getCafcassDirections());
        assertThat(updatedCaseData.getCafcassDirectionsCustom())
            .isEqualTo(caseData.getCafcassDirectionsCustom());
        assertThat(updatedCaseData.getOtherPartiesDirections())
            .isEqualTo(caseData.getOtherPartiesDirections());
        assertThat(updatedCaseData.getOtherPartiesDirectionsCustom())
            .isEqualTo(caseData.getOtherPartiesDirectionsCustom());
        assertThat(updatedCaseData.getRespondentDirections())
            .isEqualTo(caseData.getRespondentDirections());
        assertThat(updatedCaseData.getRespondentDirectionsCustom())
            .isEqualTo(caseData.getRespondentDirectionsCustom());

        verifyNoInteractions(standardDirectionsService);
    }

    private static Direction testDirection() {
        return Direction.builder()
            .directionType(RandomStringUtils.randomAlphanumeric(5))
            .directionText(RandomStringUtils.randomAlphanumeric(5))
            .build();
    }

    private static HearingBooking testHearing() {
        return HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(RandomUtils.nextInt(0, 100)))
            .build();
    }

}
