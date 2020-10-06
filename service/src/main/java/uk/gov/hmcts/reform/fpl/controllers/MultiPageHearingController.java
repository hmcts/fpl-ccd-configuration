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
import uk.gov.hmcts.reform.fpl.events.NewHearingsAdded;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.MultiPageHearingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;

import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptionsPOCType.EDIT_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListValueCode;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/multi-page-hearing")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MultiPageHearingController extends CallbackController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final HearingBookingService hearingBookingService;
    private final StandardDirectionsService standardDirectionsService;
    private final MultiPageHearingService multiPageHearingService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getAllocatedJudge() != null) {
            caseDetails.getData().put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
        }

        List<Element<HearingBooking>> hearings = defaultIfNull(caseData.getHearingDetails(), List.of());
        List<Element<HearingBooking>> futureHearings = hearingBookingService.getFutureHearings(
            defaultIfNull(hearings, List.of()));

        if (hearings.isEmpty()) {
            caseDetails.getData().put("firstHearingFlag", "Yes");
        } else {
            caseDetails.getData().put("hearingDateList",
                asDynamicList(futureHearings, hearing -> hearing.toLabel(DATE)));
            caseDetails.getData().put("hasExistingHearings", YES.getValue());
        }

        return respond(caseDetails);
    }

    @PostMapping("/populate-existing-hearings/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateExistingDraftHearing(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        //Only triggered for edit journey
        if (EDIT_DRAFT.equals(caseData.getUseExistingHearing())) {
            UUID hearingBookingId = getDynamicListValueCode(caseData.getHearingDateList(), mapper);

            List<Element<HearingBooking>> futureHearings = hearingBookingService.getFutureHearings(
                caseData.getHearingDetails());

            caseDetails.getData().put("hearingDateList",
                asDynamicList(futureHearings, hearingBookingId, hearing -> hearing.toLabel(DATE)));

            HearingBooking hearingBooking = multiPageHearingService.findHearingBooking(
                hearingBookingId, caseData.getHearingDetails());

            populateHearingBooking(caseDetails, hearingBooking);

            if (hearingBookingId.equals(caseData.getHearingDetails().get(0).getId())) {
                caseDetails.getData().put("firstHearingFlag", "Yes");
            }
        }

        return respond(caseDetails);
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateHearingDatesMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validateGroupService.validateGroup(caseData, HearingDatesGroup.class);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        HearingBooking hearingBooking = multiPageHearingService.buildHearingBooking(caseData);

        if (caseData.getSendNoticeOfHearing() != null && caseData.getSendNoticeOfHearing().equals("Yes")) {
            multiPageHearingService.addNoticeOfHearing(caseData, hearingBooking);
        }

        List<Element<HearingBooking>> hearingBookingElements;

        // Editing previous hearing
        if ((caseData.getUseExistingHearing() != null) && EDIT_DRAFT.equals(caseData.getUseExistingHearing())) {
            UUID editedHearingId = getDynamicListValueCode(caseData.getHearingDateList(), mapper);

            caseDetails.getData().put("selectedHearingIds", List.of(editedHearingId));

            hearingBookingElements = multiPageHearingService.updateEditedHearingEntry(
                hearingBooking, editedHearingId, caseData.getHearingDetails());
        } else {
            hearingBookingElements = multiPageHearingService.appendHearingBooking(
                defaultIfNull(caseData.getHearingDetails(), List.of()), hearingBooking);
            caseDetails.getData().put("selectedHearingIds",
                List.of(hearingBookingElements.get(hearingBookingElements.size() - 1)));
        }

        HearingBooking mostRecentHearingBooking = unwrapElements(hearingBookingElements).stream().min(
            comparing(HearingBooking::getStartDate)).orElseThrow(NoHearingBookingException::new);

        HearingVenue mostRecentVenue = hearingVenueLookUpService.getHearingVenue(mostRecentHearingBooking);

        caseDetails.getData().put("selectedHearingIds", caseData.getSelectedHearingIds());
        caseDetails.getData().put(HEARING_DETAILS_KEY, hearingBookingElements);
        caseDetails.getData().put("firstHearingFlag", "No");

        //Set previousHearingVenue to be the venue of the most recent hearing
        //This won't be set for hearings in existing cases, only for newly added hearings
        caseDetails.getData().put("previousHearingVenue",
            PreviousHearingVenue.builder()
                .previousVenue(hearingVenueLookUpService.buildHearingVenue(mostRecentVenue))
                .build());
        caseDetails.getData().put("previousVenueId", mostRecentVenue.getHearingVenueId());

        removeHearingProperties(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        if (isInGatekeepingState(callbackRequest.getCaseDetails())
            && standardDirectionsService.hasEmptyDates(caseData)) {
            publishEvent(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
        }

        //TODO Refactor during removal of old HearingBookingDetails code
        List<Element<HearingBooking>> hearingsToBeSent = hearingBookingService.getSelectedHearings(
            unwrapElements(caseData.getSelectedHearingIds()), caseData.getHearingDetails());

        if (!hearingsToBeSent.isEmpty() && hearingsToBeSent.get(0).getValue().getNoticeOfHearing() != null) {
            publishEvent(new NewHearingsAdded(caseData, hearingsToBeSent));
        }
    }

    private JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }

    private void populateHearingBooking(CaseDetails caseDetails, HearingBooking hearingBooking) {
        caseDetails.getData().put("hearingType", hearingBooking.getType());
        caseDetails.getData().put("hearingVenue", hearingBooking.getVenue());
        caseDetails.getData().put("hearingVenueCustom", hearingBooking.getVenueCustomAddress());
        caseDetails.getData().put("hearingStartDate", hearingBooking.getStartDate());
        caseDetails.getData().put("hearingEndDate", hearingBooking.getEndDate());
        caseDetails.getData().put("judgeAndLegalAdvisor", hearingBooking.getJudgeAndLegalAdvisor());
    }

    private void removeHearingProperties(CaseDetails caseDetails) {
        caseDetails.getData().remove("hearingType");
        caseDetails.getData().remove("hearingVenue");
        caseDetails.getData().remove("hearingVenueCustom");
        caseDetails.getData().remove("hearingStartDate");
        caseDetails.getData().remove("hearingEndDate");
        caseDetails.getData().remove("sendNoticeOfHearing");
        caseDetails.getData().remove("judgeAndLegalAdvisor");
        caseDetails.getData().remove("hasExistingHearings");
        caseDetails.getData().remove("hearingDateList");
        caseDetails.getData().remove("useExistingHearing");
        caseDetails.getData().remove("noticeOfHearingNotes");
    }
}
