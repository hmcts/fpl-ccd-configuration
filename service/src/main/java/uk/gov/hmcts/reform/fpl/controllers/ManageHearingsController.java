package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.PastHearingDatesGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/manage-hearings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageHearingsController extends CallbackController {

    private static final String FIRST_HEARING_FLAG = "firstHearingFlag";
    private static final String HEARING_DATE_LIST = "hearingDateList";
    private static final String PAST_HEARING_LIST = "pastAndTodayHearingDateList";
    private static final String FUTURE_HEARING_LIST = "futureAndTodayHearingDateList";
    private static final String SELECTED_HEARING_ID = "selectedHearingId";
    private static final String CANCELLED_HEARING_DETAILS_KEY = "cancelledHearingDetails";
    private static final String HEARING_DOCUMENT_BUNDLE_KEY = "hearingFurtherEvidenceDocuments";

    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final ManageHearingsService hearingsService;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().remove(SELECTED_HEARING_ID);

        if (caseData.getAllocatedJudge() != null) {
            caseDetails.getData().put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
        }

        caseDetails.getData().put(FIRST_HEARING_FLAG, (isEmpty(caseData.getHearingDetails()) ? YES : NO).getValue());

        if (isNotEmpty(caseData.getHearingDetails())) {
            caseDetails.getData().put(HEARING_DATE_LIST, hearingsService.asDynamicList(caseData.getFutureHearings()));

            caseDetails.getData()
                .put(PAST_HEARING_LIST, hearingsService.asDynamicList(caseData.getPastAndTodayHearings()));

            caseDetails.getData()
                .put(FUTURE_HEARING_LIST, hearingsService.asDynamicList(caseData.getFutureAndTodayHearings()));

            caseDetails.getData().put("hasExistingHearings", YES.getValue());
        }

        return respond(caseDetails);
    }

    @PostMapping("/edit-hearing/mid-event")
    public CallbackResponse populateExistingDraftHearing(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (NEW_HEARING == caseData.getHearingOption()) {
            caseDetails.getData().putAll(hearingsService.populatePreviousVenueFields(caseData));
        } else if (EDIT_HEARING == caseData.getHearingOption()) {
            final UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData.getHearingDateList());
            final List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();

            caseDetails.getData()
                .put(HEARING_DATE_LIST, hearingsService.asDynamicList(futureHearings, hearingBookingId));

            HearingBooking hearingBooking = hearingsService
                .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                .orElse(HearingBooking.builder().build());

            caseDetails.getData().putAll(hearingsService.populateHearingCaseFields(
                hearingBooking, caseData.getAllocatedJudge()));

            if (hearingBookingId.equals(caseData.getHearingDetails().get(0).getId())
                || hearingBooking.getPreviousHearingVenue() == null
                || hearingBooking.getPreviousHearingVenue().getPreviousVenue() == null) {
                caseDetails.getData().put(FIRST_HEARING_FLAG, "Yes");
            }
        } else if (ADJOURN_HEARING == caseData.getHearingOption()) {
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData.getPastAndTodayHearingDateList());

            caseDetails.getData().put(PAST_HEARING_LIST,
                hearingsService.asDynamicList(caseData.getPastAndTodayHearings(), hearingBookingId));
        } else if (VACATE_HEARING == caseData.getHearingOption()) {
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData.getFutureAndTodayHearingDateList());

            caseDetails.getData().put(FUTURE_HEARING_LIST,
                hearingsService.asDynamicList(caseData.getFutureAndTodayHearings(), hearingBookingId));
        }

        return respond(caseDetails);
    }

    @PostMapping("/re-list/mid-event")
    public CallbackResponse reListHearing(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Object dynamicHearingListType = hearingsService.getSelectedDynamicListType(caseData);
        UUID hearingBookingId = hearingsService.getSelectedHearingId(dynamicHearingListType);

        HearingBooking adjournedHearingBooking = hearingsService
            .getHearingBooking(hearingBookingId, caseData.getHearingDetails());

        HearingBooking reListedHearingBooking = adjournedHearingBooking.toBuilder()
            .previousHearingVenue(null)
            .startDate(null)
            .endDate(null)
            .build();

        caseDetails.getData().putAll(hearingsService.populateHearingCaseFields(
            reListedHearingBooking, caseData.getAllocatedJudge()));

        caseDetails.getData().put(FIRST_HEARING_FLAG, YES.getValue());

        return respond(caseDetails);
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public CallbackResponse validateHearingDatesMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors;

        //have done like this as not null / empty wasn't working
        if (caseData.getHearingOption() == EDIT_HEARING || caseData.getHearingOption() == ADJOURN_HEARING
            || caseData.getHearingOption() == VACATE_HEARING) {
            errors = validateGroupService.validateGroup(caseData, HearingDatesGroup.class);
            return respond(caseDetails, errors);
        } else {
            errors = validateGroupService.validateGroup(caseData, PastHearingDatesGroup.class);
            if (caseData.getHearingEndDate().isBefore(LocalDateTime.now()) || caseData.getHearingStartDate().isBefore(LocalDateTime.now()))
            {
                caseDetails.getData().put("pageShow", "YES");
                caseDetails.getData().putAll(hearingsService.populateFieldsWhenPastDateAdded(caseData));
            }

            return respond(caseDetails, errors);
        }
    }

    @PostMapping("/hearing-in-past/mid-event")
    public CallbackResponse populateHearingDateIfIncorrect(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        //does not need toggled as will not be triggered
        if (caseDetails.getData().get("hearingDateConfirmation").equals("NO")) {
            caseDetails.getData().putAll(hearingsService.changeHearingDateToDateAddedOnConfirmationPage(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final CaseDetailsMap data = caseDetailsMap(caseDetails);

        hearingsService.findAndSetPreviousVenueId(caseData);

        if (EDIT_HEARING == caseData.getHearingOption()) {
            final UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData.getHearingDateList());
            final HearingBooking hearingBooking = hearingsService.getCurrentHearingBooking(caseData);
            final Element<HearingBooking> hearingBookingElement = element(hearingBookingId, hearingBooking);

            hearingsService.addOrUpdate(hearingBookingElement, caseData);
            hearingsService.sendNoticeOfHearing(caseData, hearingBooking);

            data.put(SELECTED_HEARING_ID, hearingBookingId);
        } else if (ADJOURN_HEARING == caseData.getHearingOption()) {
            UUID adjournedHearingId = hearingsService.getSelectedHearingId(caseData.getPastAndTodayHearingDateList());

            if (caseData.getHearingReListOption() == RE_LIST_NOW) {
                final HearingBooking reListedHearing = hearingsService.getCurrentHearingBooking(caseData);
                final UUID reListedHearingId = hearingsService
                    .adjournAndReListHearing(caseData, adjournedHearingId, reListedHearing);
                hearingsService.sendNoticeOfHearing(caseData, reListedHearing);

                data.put(SELECTED_HEARING_ID, reListedHearingId);
            } else {
                hearingsService.adjournHearing(caseData, adjournedHearingId);
                data.remove(SELECTED_HEARING_ID);
            }
        } else if (VACATE_HEARING == caseData.getHearingOption()) {
            UUID vacatedHearingId = hearingsService.getSelectedHearingId(caseData.getFutureAndTodayHearingDateList());

            if (caseData.getHearingReListOption() == RE_LIST_NOW) {
                final HearingBooking reListedHearing = hearingsService.getCurrentHearingBooking(caseData);
                final UUID reListedHearingId = hearingsService
                    .vacateAndReListHearing(caseData, vacatedHearingId, reListedHearing);

                hearingsService.sendNoticeOfHearing(caseData, reListedHearing);

                data.put(SELECTED_HEARING_ID, reListedHearingId);
            } else {
                hearingsService.vacateHearing(caseData, vacatedHearingId);
                data.remove(SELECTED_HEARING_ID);
            }
        } else {
            final HearingBooking hearingBooking = hearingsService.getCurrentHearingBooking(caseData);
            final Element<HearingBooking> hearingBookingElement = element(hearingBooking);

            hearingsService.addOrUpdate(hearingBookingElement, caseData);
            hearingsService.sendNoticeOfHearing(caseData, hearingBooking);

            data.put(SELECTED_HEARING_ID, hearingBookingElement.getId());
        }

        data.putIfNotEmpty(CANCELLED_HEARING_DETAILS_KEY, caseData.getCancelledHearingDetails());
        data.putIfNotEmpty(HEARING_DOCUMENT_BUNDLE_KEY, caseData.getHearingFurtherEvidenceDocuments());
        data.putIfNotEmpty(HEARING_DETAILS_KEY, caseData.getHearingDetails());

        data.keySet().removeAll(hearingsService.caseFieldsToBeRemoved());

        data.remove("hearingStartDateLabel");
        data.remove("pageShow");
        data.remove("hearingEndDateLabel");
        data.remove("hearingDateConfirmation");
        data.remove("hearingStartDateConfirmation");
        data.remove("hearingEndDateConfirmation");
        data.remove("showStartDateLabel");
        data.remove("showEndDateLabel");

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        if (isNotEmpty(caseData.getSelectedHearingId())) {
            if (isInGatekeepingState(callbackRequest.getCaseDetails())
                && standardDirectionsService.hasEmptyDates(caseData)) {
                publishEvent(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
            }

            hearingsService.findHearingBooking(caseData.getSelectedHearingId(), caseData.getHearingDetails())
                .filter(hearing -> isNotEmpty(hearing.getNoticeOfHearing()))
                .ifPresent(hearing -> publishEvent(new SendNoticeOfHearing(caseData, hearing)));
        }
    }

    private static JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }
}
