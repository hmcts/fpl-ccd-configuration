package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.NewHearingsAdded;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.FIRST_HEARING_FLAG;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListValueCode;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/manage-hearings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageHearingsController extends CallbackController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final ManageHearingsService manageHearingsService;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getAllocatedJudge() != null) {
            caseDetails.getData().put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
        }

        List<Element<HearingBooking>> hearings = defaultIfNull(caseData.getHearingDetails(), List.of());

        if (hearings.isEmpty()) {
            caseDetails.getData().put(FIRST_HEARING_FLAG, "Yes");
        } else {
            caseDetails.getData().put(HEARING_DATE_LIST,
                asDynamicList(caseData.getFutureHearings(), hearing -> hearing.toLabel(DATE)));
            caseDetails.getData().put("hasExistingHearings", YES.getValue());
        }

        return respond(caseDetails);
    }

    @PostMapping("/edit-hearing/mid-event")
    public CallbackResponse populateExistingDraftHearing(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        //If new hearing, populate previous venue - if editing existing hearing, populate page with existing hearing
        if (NEW_HEARING == caseData.getHearingOption()) {
            caseDetails.getData().putAll(manageHearingsService.populatePreviousVenueFields(caseData));
        } else if (EDIT_HEARING == caseData.getHearingOption()) {
            UUID hearingBookingId = getDynamicListValueCode(caseData.getHearingDateList(), mapper);

            List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();

            caseDetails.getData().put(HEARING_DATE_LIST,
                asDynamicList(futureHearings, hearingBookingId, hearing -> hearing.toLabel(DATE)));

            HearingBooking hearingBooking = manageHearingsService.findHearingBooking(
                hearingBookingId, caseData.getHearingDetails());

            caseDetails.getData().putAll(manageHearingsService.populateHearingCaseFields(
                hearingBooking, caseData.getAllocatedJudge()));

            if (hearingBookingId.equals(caseData.getHearingDetails().get(0).getId())
                || hearingBooking.getPreviousHearingVenue() == null
                || hearingBooking.getPreviousHearingVenue().getPreviousVenue() == null) {
                caseDetails.getData().put(FIRST_HEARING_FLAG, "Yes");
            }
        }

        return respond(caseDetails);
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public CallbackResponse validateHearingDatesMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validateGroupService.validateGroup(caseData, HearingDatesGroup.class);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        manageHearingsService.findAndSetPreviousVenueId(caseData);

        HearingBooking hearingBooking = manageHearingsService.buildHearingBooking(caseData);

        if ("Yes".equals(caseData.getSendNoticeOfHearing())) {
            manageHearingsService.addNoticeOfHearing(caseData, hearingBooking);
        }

        List<Element<HearingBooking>> hearingBookingElements;

        // Editing previous hearing
        if (EDIT_HEARING == caseData.getHearingOption()) {
            UUID editedHearingId = getDynamicListValueCode(caseData.getHearingDateList(), mapper);

            hearingBookingElements = manageHearingsService.updateEditedHearingEntry(
                hearingBooking, editedHearingId, caseData.getHearingDetails());
        } else {
            List<Element<HearingBooking>> currentHearingBookings = defaultIfNull(
                caseData.getHearingDetails(), new ArrayList<>()
            );
            currentHearingBookings.add(element(hearingBooking));
            hearingBookingElements = currentHearingBookings;
        }

        caseDetails.getData().put(HEARING_DETAILS_KEY, hearingBookingElements);
        caseDetails.getData().put(FIRST_HEARING_FLAG, "No");

        caseDetails.getData().keySet().removeAll(manageHearingsService.caseFieldsToBeRemoved());

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        if (isInGatekeepingState(callbackRequest.getCaseDetails())
            && standardDirectionsService.hasEmptyDates(caseData)) {
            publishEvent(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
        }

        //TODO Refactor during removal of legacy code (now only ever one hearing sent, list not needed) (FPLA-2280)
        //Also rename NewHearingsAdded event as this can be triggered for edited hearings too
        List<Element<HearingBooking>> hearingsToBeSent = caseData.getHearingDetails();
        if (caseDataBefore.getHearingDetails() != null) {
            hearingsToBeSent.removeAll(caseDataBefore.getHearingDetails());
        }
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
}
