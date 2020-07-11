package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.NewHearingsAddedEvent;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingValidatorService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.NEW_HEARING_LABEL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.NEW_HEARING_SELECTOR;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/add-hearing-bookings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingBookingDetailsController {
    private final HearingBookingService service;
    private final HearingBookingValidatorService validationService;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final StandardDirectionsService standardDirectionsService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> errors = validationService.validateHasAllocatedJudge(caseData);

        Judge allocatedJudge = caseData.getAllocatedJudge();

        if (errors.isEmpty()) {
            List<Element<HearingBooking>> hearingDetails = service.expandHearingBookingCollection(caseData);

            hearingDetails = service.resetHearingJudge(hearingDetails, allocatedJudge);

            service.removePastHearings(hearingDetails);

            caseDetails.getData().put(HEARING_DETAILS_KEY, hearingDetails);
            caseDetails.getData().put("allocatedJudgeLabel", buildAllocatedJudgeLabel(caseData.getAllocatedJudge()));
        }

        caseDetails.getData().remove(NEW_HEARING_LABEL.getKey());
        caseDetails.getData().remove(NEW_HEARING_SELECTOR.getKey());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackrequest.getCaseDetailsBefore();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        List<Element<HearingBooking>> hearings = caseData.getHearingDetails();
        List<Element<HearingBooking>> hearingsBefore = defaultIfNull(caseDataBefore.getHearingDetails(), emptyList());

        service.removePastHearings(hearingsBefore);

        if (!service.getNewHearings(hearings, hearingsBefore).isEmpty()) {
            caseDetails.getData().putAll(service.getHearingNoticeCaseFields(hearings, hearingsBefore));
        } else {
            caseDetails.getData().put(NEW_HEARING_LABEL.getKey(), "");
            caseDetails.getData().put(NEW_HEARING_SELECTOR.getKey(), null);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationService.validateHearingBookings(unwrapElements(caseData.getHearingDetails())))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        List<Element<HearingBooking>> hearingDetails = service.getHearingDetails(caseDetails, caseData, caseDataBefore);

        caseDetails.getData().put(HEARING_DETAILS_KEY, hearingDetails);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        if (isInGatekeepingState(callbackRequest.getCaseDetails())
            && standardDirectionsService.hasEmptyDates(caseData)) {
            applicationEventPublisher.publishEvent(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
        }

        applicationEventPublisher.publishEvent(new NewHearingsAddedEvent(callbackRequest));

    }
}
