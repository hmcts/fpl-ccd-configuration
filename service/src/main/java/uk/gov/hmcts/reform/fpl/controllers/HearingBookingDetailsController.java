package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.model.TestMulti;
import uk.gov.hmcts.reform.fpl.model.common.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookupService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Api
@RestController
@RequestMapping("/callback/add-hearing-bookings")
public class HearingBookingDetailsController {
    private final MapperService mapperService;
    private final HearingBookingService hearingBookingService;
    private final HearingVenueLookupService lookupService;

    @Autowired
    public HearingBookingDetailsController(MapperService mapperService, HearingBookingService hearingBookingService, HearingVenueLookupService lookupService) {
        this.mapperService = mapperService;
        this.hearingBookingService = hearingBookingService;
        this.lookupService = lookupService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        List<HearingVenue> hearingVenues = lookupService.getHearingVenues();
        DynamicList hearingVenuesDynamic = DynamicList.toDynamicList(hearingVenues);

        caseDetails.getData().put("hearingDetails", hearingBookingService.expandHearingBookingCollection(caseData,hearingVenuesDynamic));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validate(caseDetails))
            .build();
    }

    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        caseData.getHearingDetails().stream()
            .map(Element::getValue)
            .map(HearingBooking::getDate)
            .filter(Objects::nonNull)
            .filter(hearingDate -> !hearingDate.isAfter(LocalDate.now()))
            .findAny()
            .ifPresent(error -> errors.add("Enter a future date"));

        return errors.build();
    }
}
