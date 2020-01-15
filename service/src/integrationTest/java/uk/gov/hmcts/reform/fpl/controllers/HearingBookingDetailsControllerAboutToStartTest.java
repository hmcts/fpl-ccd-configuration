package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerAboutToStartTest extends AbstractControllerTest {
    private static final LocalDateTime TODAY = LocalDateTime.now();

    @Autowired
    private ObjectMapper mapper;

    HearingBookingDetailsControllerAboutToStartTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldReturnPopulatedHearingWhenNoOtherHearingsExist() {
        AboutToStartOrSubmitCallbackResponse response =
            postAboutToStartEvent(callbackRequestWithHearingDetails(new HashMap<>()));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).hasSize(1);
    }

    @Test
    void shouldOnlyReturnHearingsWithFutureStartDateWhenHearingsInThePastExist() {
        List<Element<HearingBooking>> hearingDetails = newArrayList(
            bookingWithStartDate(TODAY.plusDays(5)), bookingWithStartDate(TODAY.minusDays(5)));

        AboutToStartOrSubmitCallbackResponse response =
            postAboutToStartEvent(callbackRequestWithHearingDetails(Map.of(HEARING_DETAILS.getKey(), hearingDetails)));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).hasSize(1);
        assertThat(caseData.getHearingDetails().get(0).getValue().getStartDate()).isAfter(TODAY);
        assertThat(caseData.getPastHearingDetails()).hasSize(1);
        assertThat(caseData.getPastHearingDetails().get(0).getValue().getStartDate()).isBefore(TODAY);
    }

    private CallbackRequest callbackRequestWithHearingDetails(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
    }

    private Element<HearingBooking> bookingWithStartDate(LocalDateTime date) {
        return element(HearingBooking.builder().startDate(date).build());
    }
}
