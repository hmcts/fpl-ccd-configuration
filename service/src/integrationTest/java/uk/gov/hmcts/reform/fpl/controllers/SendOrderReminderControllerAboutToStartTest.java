package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(SendOrderReminderController.class)
@OverrideAutoConfiguration(enabled = true)
public class SendOrderReminderControllerAboutToStartTest extends AbstractCallbackTest {

    public static final String HAS_MISSING_ORDERS_FLAG = "hasHearingsMissingOrders";

    SendOrderReminderControllerAboutToStartTest() {
        super("send-order-reminder");
    }

    @Test
    void shouldCheckForMissingCMOs() {
        LocalDateTime startDate = now().minusDays(5);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(startDate)
                    .endDate(startDate.plusHours(1))
                    .build())
            ))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(callbackRequest);

        assertThat(response.getData()).extracting(HAS_MISSING_ORDERS_FLAG).isEqualTo(YesNo.YES.toString());
        assertThat(response.getData()).extracting("listOfHearingsMissingOrders")
            .isEqualTo(String.format("<ul><li>Case management hearing, %s</li></ul>",
                formatLocalDateTimeBaseUsingFormat(startDate, DATE)));
    }

    @Test
    void shouldReturnNoHearingsIfAllHearingsHaveCMOs() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(now().minusDays(5))
                    .endDate(now().minusDays(5).plusHours(1))
                    .caseManagementOrderId(UUID.randomUUID())
                    .build())
            ))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(callbackRequest);

        assertThat(response.getData()).extracting(HAS_MISSING_ORDERS_FLAG).isEqualTo(YesNo.NO.toString());
    }

    @Test
    void shouldReturnNoHearingsIfNoHearingsOnCase() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(callbackRequest);

        assertThat(response.getData()).extracting(HAS_MISSING_ORDERS_FLAG).isEqualTo(YesNo.NO.toString());
    }

    @Test
    void shouldAlwaysClearShouldSendOrderReminderField() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .shouldSendOrderReminder(YesNo.YES)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        CaseData after = extractCaseData(postAboutToStartEvent(callbackRequest));

        assertThat(after.getShouldSendOrderReminder()).isNull();
    }

}
