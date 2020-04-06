package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/add-hearing-bookings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingBookingDetailsController {
    private final HearingBookingService service;
    private final ValidateGroupService validateGroupService;
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<HearingBooking>> hearingDetails = service.expandHearingBookingCollection(caseData);

        List<Element<HearingBooking>> pastHearings = service.getPastHearings(hearingDetails);

        hearingDetails.removeAll(pastHearings);

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            String assignedJudgeLabel = buildAllocatedJudgeLabel(caseData.getAllocatedJudge());

            hearingDetails = hearingDetails.stream()
                .map(element -> {
                    HearingBooking.HearingBookingBuilder hearingBookingBuilder = HearingBooking.builder();
                    HearingBooking hearingBooking = element.getValue();

                    JudgeAndLegalAdvisor.JudgeAndLegalAdvisorBuilder judgeAndLegalAdvisorBuilder
                        = JudgeAndLegalAdvisor.builder();

                    judgeAndLegalAdvisorBuilder.allocatedJudgeLabel(assignedJudgeLabel);

                    hearingBookingBuilder.type(hearingBooking.getType())
                        .typeDetails(hearingBooking.getTypeDetails())
                        .venue(hearingBooking.getVenue())
                        .startDate(hearingBooking.getStartDate())
                        .endDate(hearingBooking.getEndDate())
                        .hearingNeedsBooked(hearingBooking.getHearingNeedsBooked())
                        .hearingNeedsDetails(hearingBooking.getHearingNeedsDetails())
                        .judgeAndLegalAdvisor(judgeAndLegalAdvisorBuilder.build());

                    return Element.<HearingBooking>builder()
                        .id(element.getId())
                        .value(hearingBookingBuilder.build())
                        .build();
                }).collect(toList());
        }

        caseDetails.getData().put(HEARING_DETAILS_KEY, hearingDetails);

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
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        List<Element<HearingBooking>> hearingDetailsBefore = service.expandHearingBookingCollection(caseDataBefore);
        List<Element<HearingBooking>> pastHearings = service.getPastHearings(hearingDetailsBefore);

        List<Element<HearingBooking>> combinedHearingDetails =
            service.combineHearingDetails(caseData.getHearingDetails(), pastHearings);

        caseDetails.getData().put(HEARING_DETAILS_KEY, combinedHearingDetails);

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
