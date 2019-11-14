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
import uk.gov.hmcts.reform.fpl.interfaces.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/callback/add-hearing-bookings")
public class HearingBookingDetailsController {
    private final MapperService mapperService;
    private final HearingBookingService hearingBookingService;
    private final ValidateGroupService validateGroupService;
    private final ObjectMapper mapper;

    @Autowired
    public HearingBookingDetailsController(MapperService mapperService,
                                           HearingBookingService hearingBookingService,
                                           ValidateGroupService validateGroupService,
                                           ObjectMapper mapper) {
        this.mapperService = mapperService;
        this.hearingBookingService = hearingBookingService;
        this.validateGroupService = validateGroupService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("hearingDetails", hearingBookingService.expandHearingBookingCollection(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        final List<String> errors = caseData.getHearingDetails()
            .stream()
            .flatMap(hearingDetail -> validateGroupService.validateGroup(
                hearingDetail, HearingBookingDetailsGroup.class).stream())
            .distinct()
            .collect(Collectors.toList());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }
}
