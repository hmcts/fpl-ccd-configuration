package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.SendOrderReminderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.cmo.SendOrderReminderService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Slf4j
@RestController
@RequestMapping("/callback/send-order-reminder")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendOrderReminderController extends CallbackController {

    private final SendOrderReminderService sendOrderReminderService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<HearingBooking> hearingsWithoutCMOs = sendOrderReminderService.getPastHearingBookingsWithoutCMOs(caseData);

        boolean hasHearingsMissingOrders = !hearingsWithoutCMOs.isEmpty();

        // Clear this field so we don't accidentally send reminders when there are no orders.
        caseDetails.getData().remove("shouldSendOrderReminder");

        caseDetails.getData().put("hasHearingsMissingOrders", YesNo.from(hasHearingsMissingOrders));

        if (hasHearingsMissingOrders) {
            String hearingsWithinRange = "<ul>" + hearingsWithoutCMOs.stream()
                .map(booking -> "<li>" + booking.toLabel() + "</li>")
                .collect(Collectors.joining("")) + "</ul>";

            caseDetails.getData().put("listOfHearingsMissingOrders", hearingsWithinRange);
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        if (YES.equals(caseData.getShouldSendOrderReminder())) {
            publishEvent(new SendOrderReminderEvent(caseData));
        }
    }

}
