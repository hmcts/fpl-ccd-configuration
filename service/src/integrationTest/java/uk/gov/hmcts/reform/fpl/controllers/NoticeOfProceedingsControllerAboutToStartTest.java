package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.DATE;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToStartTest extends AbstractControllerTest {
    private static final LocalDateTime TODAY = LocalDateTime.now();

    NoticeOfProceedingsControllerAboutToStartTest() {
        super("notice-of-proceedings");
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of(HEARING_DETAILS_KEY, createHearingBookings()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter Familyman case number");
    }

    @Test
    void shouldUpdateProceedingLabelToIncludeHearingBookingDetailsDate() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of(
                HEARING_DETAILS_KEY, createHearingBookings(),
                "familyManCaseNumber", "123"
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        String proceedingLabel = callbackResponse.getData().get("proceedingLabel").toString();

        String expectedContent = String.format("The case management hearing will be on the %s.",
            formatLocalDateTimeBaseUsingFormat(TODAY, DATE));

        assertThat(proceedingLabel).isEqualTo(expectedContent);
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ElementUtils.wrapElements(
            createHearingBooking(TODAY.plusDays(5), TODAY.plusHours(6)),
            createHearingBooking(TODAY.plusDays(2), TODAY.plusMinutes(45)),
            createHearingBooking(TODAY, TODAY.plusHours(2)));
    }
}
