package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableSet;
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
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookupService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Api
@RestController
@RequestMapping("/callback/add-hearing-bookings")
public class HearingBookingDetailsController {
    private final MapperService mapperService;
    private final HearingBookingService hearingBookingService;
    private final HearingVenueLookupService lookupService;

    @Autowired
    public HearingBookingDetailsController(MapperService mapperService, HearingBookingService hearingBookingService,
                                           HearingVenueLookupService lookupService) {
        this.mapperService = mapperService;
        this.hearingBookingService = hearingBookingService;
        this.lookupService = lookupService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackrequest) throws IOException {

        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        List<HearingVenue> hearingVenues = lookupService.getHearingVenues();
        DynamicList hearingVenuesDynamic = DynamicList.toDynamicList(hearingVenues);

        caseDetails.getData()
            .put("hearingDetails",
                hearingBookingService.expandHearingBookingCollection(caseData, hearingVenuesDynamic));

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
        ImmutableSet.Builder<String> errors = ImmutableSet.builder();

        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        for (Element<HearingBooking> hearingBookingElement : caseData.getHearingDetails()) {
            HearingBooking value = hearingBookingElement.getValue();
            DynamicListElement selectedVenue = value.getVenueList().getValue();
            LocalDate hearingDate = value.getDate();

            if (hearingDate != null && !hearingDate.isAfter(LocalDate.now())) {
                errors.add("Enter a future date");
            }

            if (selectedVenue != null && selectedVenue.getCode().equals(DynamicListElement.DEFAULT_CODE)) {
                errors.add("Select a hearing venue");
            }
        }

        return errors.build().asList();
    }
}
