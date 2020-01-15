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

import static java.util.stream.Collectors.toList;

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

        List<Element<HearingBooking>> hearings = List.copyOf(caseData.getHearingDetails());

        List<Element<HearingBooking>> hearingDetails = service.expandHearingBookingCollection(caseData);
        service.filterHearingsInPast(hearingDetails);

        List<Element<HearingBooking>> pastHearings = hearings.stream()
            .filter(x -> !hearingDetails.contains(x))
            .collect(toList());

        caseDetails.getData().put("hearingDetails", hearingDetails);
        caseDetails.getData().put("pastHearingDetails", Map.of("hearingBookingCollection", pastHearings));

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

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();
        List<Element<HearingBooking>> pastHearingDetails = caseData.getPastHearingDetails();

        service.addHearingsInPastFromBeforeDataState(hearingDetails, pastHearingDetails);

        caseDetails.getData().put("hearingDetails", hearingDetails);
        caseDetails.getData().remove("hearingDetailsPast");

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
