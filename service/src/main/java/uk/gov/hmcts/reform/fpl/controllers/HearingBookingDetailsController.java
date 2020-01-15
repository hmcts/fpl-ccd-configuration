package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.PAST_HEARING_DETAILS;

@Api
@RestController
@RequestMapping("/callback/add-hearing-bookings")
public class HearingBookingDetailsController {
    private final HearingBookingService service;
    private final ValidateGroupService validateGroupService;
    private final ObjectMapper mapper;

    @Autowired
    public HearingBookingDetailsController(HearingBookingService service,
                                           ValidateGroupService validateGroupService,
                                           ObjectMapper mapper) {
        this.service = service;
        this.validateGroupService = validateGroupService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingBooking>> hearingDetails = service.expandHearingBookingCollection(caseData);

        Map<String, List<Element<HearingBooking>>> splitHearingDetails =
            service.splitPastAndFutureHearings(hearingDetails);

        caseDetails.getData().putAll(splitHearingDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateHearingBookings(caseData.getHearingDetails()))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingBooking>> combinedHearingDetails =
            service.rebuildHearingDetailsObject(caseData.getHearingDetails(), caseData.getPastHearingDetails());

        caseDetails.getData().put(HEARING_DETAILS.getKey(), combinedHearingDetails);
        caseDetails.getData().remove(PAST_HEARING_DETAILS.getKey());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<String> validateHearingBookings(List<Element<HearingBooking>> hearingDetails) {
        final List<String> errors = new ArrayList<>();
        for (int i = 0; i < hearingDetails.size(); i++) {
            HearingBooking hearingDetail = hearingDetails.get(i).getValue();
            for (String message : validateGroupService.validateGroup(hearingDetail, HearingBookingDetailsGroup.class)) {
                String formattedMessage;
                // Format the message if there is more than one hearing
                if (hearingDetails.size() != 1) {
                    formattedMessage = String.format("%s for hearing %d", message, i + 1);
                } else {
                    formattedMessage = message;
                }
                errors.add(formattedMessage);
            }
        }
        return errors;
    }
}
