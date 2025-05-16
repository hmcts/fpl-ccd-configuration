package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.events.judicial.HandleHearingModificationRolesEvent;
import uk.gov.hmcts.reform.fpl.events.judicial.NewHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.PastHearingDatesValidatorService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.hearing.ManageHearingsOthersGenerator;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingEndDateGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_FUTURE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_PAST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.FUTURE_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_AND_TODAY_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PREVIOUS_HEARING_VENUE_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.TO_RE_LIST_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.VACATE_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.utils.BooleanHelper.booleanToYesNo;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@RestController
@RequestMapping("/callback/manage-hearings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageHearingsController extends CallbackController {

    private static final String FIRST_HEARING_FLAG = "firstHearingFlag";
    private static final String SELECTED_HEARING_ID = "selectedHearingId";
    private static final String CANCELLED_HEARING_ID = "cancelledHearingId";
    private static final String PRE_ATTENDANCE = "preHearingAttendanceDetails";
    private static final String CANCELLED_HEARING_DETAILS_KEY = "cancelledHearingDetails";
    private static final String HAS_SESSION_KEY = "hasSession";
    private static final String HEARING_ORDERS_BUNDLES_DRAFTS = "hearingOrdersBundlesDrafts";
    private static final String DRAFT_UPLOADED_CMOS = "draftUploadedCMOs";
    private static final String HAS_PREVIOUS_VENUE_HEARING = "hasPreviousHearingVenue";

    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final ManageHearingsService hearingsService;
    private final PastHearingDatesValidatorService pastHearingDatesValidatorService;
    private final ManageHearingsOthersGenerator othersGenerator;
    private final JudicialService judicialService;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().remove(SELECTED_HEARING_ID);

        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getAllocatedJudge() != null) {
            caseDetails.getData().put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
            caseDetails.getData().put("allocatedJudgeLabel", buildAllocatedJudgeLabel(caseData.getAllocatedJudge()));
        }

        boolean isFirstHearing = isEmpty(caseData.getAllHearings());

        if (isFirstHearing) {
            caseDetails.getData().put(FIRST_HEARING_FLAG, YES.getValue());
            caseDetails.getData().put(PRE_ATTENDANCE, DEFAULT_PRE_ATTENDANCE);
            caseDetails.getData().putAll(othersGenerator.generate(caseData, HearingBooking.builder().build()));
        } else {
            caseDetails.getData().put(FIRST_HEARING_FLAG, NO.getValue());
        }

        caseDetails.getData().putAll(hearingsService.populateHearingLists(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/edit-hearing/mid-event")
    public CallbackResponse populateExistingDraftHearing(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validateGroupService.validateGroup(caseData, HearingBookingGroup.class);

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().putAll(hearingsService.clearPopulatedHearingFields());

        switch (caseData.getHearingOption()) {
            case NEW_HEARING:
                caseDetails.getData().putAll(hearingsService.initiateNewHearing(caseData));

                PreviousHearingVenue previousHearingVenue = (PreviousHearingVenue) caseDetails.getData()
                    .get(PREVIOUS_HEARING_VENUE_KEY);

                boolean hasPreviousHearingVenue = previousHearingVenue != null
                    && !StringUtils.isEmpty(previousHearingVenue.getPreviousVenue());

                caseDetails.getData().put(HAS_PREVIOUS_VENUE_HEARING, hasPreviousHearingVenue
                    ? YES.getValue() : NO.getValue());
                caseDetails.getData()
                    .putAll(othersGenerator.generate(caseData, HearingBooking.builder().build()));
                break;
            case EDIT_PAST_HEARING:
                UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

                caseDetails.getData().put(PAST_HEARING_DATE_LIST,
                    hearingsService.asDynamicList(caseData.getPastHearings(), hearingBookingId));

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
                break;
            case EDIT_FUTURE_HEARING:
                hearingBookingId = hearingsService.getSelectedHearingId(caseData);

                caseDetails.getData().put(FUTURE_HEARING_LIST,
                    hearingsService.asDynamicList(caseData.getFutureHearings(), hearingBookingId));

                hearingBooking = hearingsService
                    .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                    .orElse(HearingBooking.builder().build());

                caseDetails.getData().putAll(hearingsService.populateHearingCaseFields(
                    hearingBooking, caseData.getAllocatedJudge()));

                if (hearingBookingId.equals(caseData.getHearingDetails().get(0).getId())
                    || hearingBooking.getPreviousHearingVenue() == null
                    || hearingBooking.getPreviousHearingVenue().getPreviousVenue() == null) {
                    caseDetails.getData().put(FIRST_HEARING_FLAG, "Yes");
                }
                break;
            case ADJOURN_HEARING:
                hearingBookingId = hearingsService.getSelectedHearingId(caseData);

                caseDetails.getData().put(PAST_AND_TODAY_HEARING_DATE_LIST,
                    hearingsService.asDynamicList(caseData.getPastAndTodayHearings(), hearingBookingId));
                break;
            case VACATE_HEARING:
                hearingBookingId = hearingsService.getSelectedHearingId(caseData);

                List<Element<HearingBooking>> nonCancelledHearings = caseData.getAllNonCancelledHearings()
                    .stream().sorted(Comparator.comparing(booking -> booking.getValue().getStartDate()))
                    .collect(toList());

                Collections.reverse(nonCancelledHearings);

                caseDetails.getData().put(VACATE_HEARING_LIST,
                    hearingsService.asDynamicList(nonCancelledHearings, hearingBookingId));

                hearingBooking = hearingsService
                    .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                    .orElse(HearingBooking.builder().build());

                caseDetails.getData().put("showVacatePastHearingWarning",
                    booleanToYesNo(hearingBooking.getEndDate().isBefore(now())));

                errors.addAll(pastHearingDatesValidatorService.validateVacatedDate(hearingBooking.getEndDate(),
                    caseData.getVacatedHearingDate()));
                break;
            case RE_LIST_HEARING:
                if (isEmpty(caseData.getToBeReListedHearings())) {
                    return respond(caseDetails, List.of("There are no adjourned or vacated hearings to re-list"));
                }
                hearingBookingId = hearingsService.getSelectedHearingId(caseData);

                HearingBooking cancelledHearing = hearingsService
                    .getHearingBooking(hearingBookingId, caseData.getCancelledHearingDetails());

                HearingBooking reListedHearingBooking = cancelledHearing.toBuilder()
                    .previousHearingVenue(null)
                    .startDate(null)
                    .endDate(null)
                    .endDateDerived(null)
                    .hearingDays(null)
                    .hearingHours(null)
                    .hearingMinutes(null)
                    .build();

                caseDetails.getData().putAll(hearingsService.populateHearingCaseFields(
                    reListedHearingBooking, caseData.getAllocatedJudge()));

                caseDetails.getData().put(FIRST_HEARING_FLAG, YES.getValue());
                caseDetails.getData().put(TO_RE_LIST_HEARING_LIST,
                    hearingsService.asDynamicList(caseData.getToBeReListedHearings(), hearingBookingId));
                break;
            default:
                return respond(caseDetails, List.of("Invalid hearing type selected"));
        }


        if (NEW_HEARING != caseData.getHearingOption()) {
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

            HearingBooking hearingBooking = hearingsService
                .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                .orElse(HearingBooking.builder().build());

            caseDetails.getData().putAll(othersGenerator.generate(caseData, hearingBooking));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/re-list/mid-event")
    public CallbackResponse reListHearing(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

        HearingBooking adjournedHearingBooking = hearingsService
            .getHearingBooking(hearingBookingId, caseData.getHearingDetails());

        HearingBooking reListedHearingBooking = adjournedHearingBooking.toBuilder()
            .previousHearingVenue(null)
            .startDate(null)
            .endDate(null)
            .endDateDerived(null)
            .hearingDays(null)
            .hearingHours(null)
            .hearingMinutes(null)
            .build();

        caseDetails.getData().putAll(hearingsService.populateHearingCaseFields(
            reListedHearingBooking, caseData.getAllocatedJudge()));

        caseDetails.getData().put(FIRST_HEARING_FLAG, YES.getValue());

        return respond(caseDetails);
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public CallbackResponse validateHearingDatesMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        List<String> errors;
        errors = pastHearingDatesValidatorService.validateHearingIntegers(caseDetails);
        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }
        CaseData caseData = getCaseData(caseDetails);

        if (isAddingNewHearing(caseData)) {
            errors = pastHearingDatesValidatorService.validateHearingDates(caseData.getHearingStartDate(),
                caseData.getHearingEndDateTime());
            errors.addAll(pastHearingDatesValidatorService.validateDays(caseData.getHearingDuration(),
                caseData.getHearingDays()));
            errors.addAll(pastHearingDatesValidatorService.validateHoursMinutes(caseData.getHearingDuration(),
                caseData.getHearingHours(), caseData.getHearingMinutes()));
        } else {
            errors = validateGroupService.validateGroup(caseData, HearingDatesGroup.class);
            if (errors.isEmpty()) {
                errors = validateGroupService.validateGroup(caseData, HearingEndDateGroup.class);
            }
        }

        caseDetails.getData().putAll(hearingsService.populateFieldsWhenPastHearingDateAdded(caseData));

        caseDetails.getData().put(HAS_SESSION_KEY, YES.getValue());

        return respond(caseDetails, errors);
    }

    @PostMapping("/hearing-in-past/mid-event")
    public CallbackResponse populateHearingDateIfIncorrect(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (NO.getValue().equals(caseDetails.getData().get("confirmHearingDate"))) {
            List<String> errors = pastHearingDatesValidatorService.validateHearingDates(caseData
                    .getHearingStartDateConfirmation(),
                caseData.getHearingEndDateConfirmation());
            caseDetails.getData().putAll(hearingsService.updateHearingDates(caseData));

            return respond(caseDetails, errors);
        }

        return respond(caseDetails);
    }

    @PostMapping("validate-judge-email/mid-event")
    public CallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getUseAllocatedJudge()) || caseData.getUseAllocatedJudge().equals(NO)) {
            // validate + add to caseDetails the judgeAndLegalAdvisor field
            List<String> possibleErrors = judicialService.validateHearingJudgeEmail(caseDetails, caseData);
            if (possibleErrors.size() > 0) {
                return respond(caseDetails, possibleErrors);
            }
        } else {
            // we want to use the existing allocated judge
            JudgeAndLegalAdvisor hearingJudge = JudgeAndLegalAdvisor.from(caseData.getAllocatedJudge()).toBuilder()
                .legalAdvisorName(caseData.getLegalAdvisorName())
                .build();
            caseDetails.getData().put("judgeAndLegalAdvisor", hearingJudge);
        }


        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final CaseDetailsMap data = caseDetailsMap(caseDetails);

        data.remove(CANCELLED_HEARING_ID);

        hearingsService.findAndSetPreviousVenueId(caseData);

        if (EDIT_PAST_HEARING == caseData.getHearingOption()) {
            final UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

            HearingBooking originalHearingBooking = hearingsService.findHearingBooking(hearingBookingId,
                caseData.getHearingDetails()).orElse(HearingBooking.builder().build());

            HearingBooking editedHearingBooking = hearingsService.getCurrentHearingBooking(caseData);

            editedHearingBooking = originalHearingBooking.toBuilder()
                .judgeAndLegalAdvisor(editedHearingBooking.getJudgeAndLegalAdvisor())
                .allocatedJudgeLabel(editedHearingBooking.getAllocatedJudgeLabel())
                .hearingJudgeLabel(editedHearingBooking.getHearingJudgeLabel())
                .legalAdvisorLabel(editedHearingBooking.getLegalAdvisorLabel())
                .build();

            final Element<HearingBooking> hearingBookingElement = element(hearingBookingId, editedHearingBooking);

            hearingsService.addOrUpdate(hearingBookingElement, caseData);

            data.put(SELECTED_HEARING_ID, hearingBookingId);

        } else if (EDIT_FUTURE_HEARING == caseData.getHearingOption()) {
            final UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);
            HearingBooking editedHearingBooking = hearingsService.getCurrentHearingBooking(caseData);

            final Element<HearingBooking> hearingBookingElement = element(hearingBookingId, editedHearingBooking);
            hearingsService.addOrUpdate(hearingBookingElement, caseData);
            hearingsService.buildNoticeOfHearingIfYes(caseData, editedHearingBooking);

            data.put(SELECTED_HEARING_ID, hearingBookingId);
        } else if (ADJOURN_HEARING == caseData.getHearingOption()) {
            UUID adjournedHearingId = hearingsService.getSelectedHearingId(caseData);

            if (caseData.getHearingReListOption() == RE_LIST_NOW) {
                final HearingBooking reListedHearing = hearingsService.getCurrentHearingBooking(caseData);
                final UUID reListedHearingId = hearingsService
                    .adjournAndReListHearing(caseData, adjournedHearingId, reListedHearing);
                hearingsService.buildNoticeOfHearingIfYes(caseData, reListedHearing);

                data.put(SELECTED_HEARING_ID, reListedHearingId);
            } else {
                hearingsService.adjournHearing(caseData, adjournedHearingId);
                data.remove(SELECTED_HEARING_ID);
            }
        } else if (VACATE_HEARING == caseData.getHearingOption()) {
            UUID vacatedHearingId = hearingsService.getSelectedHearingId(caseData);

            if (caseData.getHearingReListOption() == RE_LIST_NOW) {
                final HearingBooking reListedHearing = hearingsService.getCurrentHearingBooking(caseData);
                final UUID reListedHearingId = hearingsService
                    .vacateAndReListHearing(caseData, vacatedHearingId, reListedHearing);

                hearingsService.buildNoticeOfHearingIfYes(caseData, reListedHearing);

                data.put(SELECTED_HEARING_ID, reListedHearingId);
            } else {
                hearingsService.vacateHearing(caseData, vacatedHearingId);
                data.remove(SELECTED_HEARING_ID);
            }

            hearingsService.buildNoticeOfHearingVacatedIfYes(caseData,
                hearingsService.getHearingBooking(vacatedHearingId, caseData.getCancelledHearingDetails()));
            data.put(CANCELLED_HEARING_ID, vacatedHearingId);
        } else if (RE_LIST_HEARING == caseData.getHearingOption()) {
            final UUID cancelledHearingId = hearingsService.getSelectedHearingId(caseData);

            final HearingBooking reListedHearing = hearingsService.getCurrentHearingBooking(caseData);
            final UUID reListedHearingId = hearingsService.reListHearing(caseData, cancelledHearingId, reListedHearing);

            hearingsService.buildNoticeOfHearingIfYes(caseData, reListedHearing);

            data.put(SELECTED_HEARING_ID, reListedHearingId);
        } else {
            final HearingBooking hearingBooking = hearingsService.getCurrentHearingBooking(caseData);
            final Element<HearingBooking> hearingBookingElement = element(hearingBooking);

            hearingsService.addOrUpdate(hearingBookingElement, caseData);
            hearingsService.buildNoticeOfHearingIfYes(caseData, hearingBooking);

            data.put(SELECTED_HEARING_ID, hearingBookingElement.getId());
        }

        data.putIfNotEmpty(CANCELLED_HEARING_DETAILS_KEY, caseData.getCancelledHearingDetails());
        data.putIfNotEmpty(HEARING_DETAILS_KEY, caseData.getHearingDetails());
        data.put(HEARING_ORDERS_BUNDLES_DRAFTS, caseData.getHearingOrdersBundlesDrafts());
        data.put(DRAFT_UPLOADED_CMOS, caseData.getDraftUploadedCMOs());

        data.keySet().removeAll(hearingsService.caseFieldsToBeRemoved());

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        publishEvent(new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest)));

        publishEvent(new HandleHearingModificationRolesEvent(caseData, getCaseDataBefore(callbackRequest)));

        if (isNotEmpty(caseData.getSelectedHearingId())) {
            if (isInGatekeepingState(callbackRequest.getCaseDetails())
                && standardDirectionsService.hasEmptyDates(caseData)) {
                publishEvent(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
            }

            Optional<HearingBooking> oldHearing = hearingsService.findHearingBooking(caseData.getSelectedHearingId(),
                getCaseDataBefore(callbackRequest).getAllNonCancelledHearings());

            hearingsService.findHearingBooking(caseData.getSelectedHearingId(), caseData.getHearingDetails())
                .ifPresent(hearingBooking -> {
                    publishEvent(new NewHearingJudgeEvent(hearingBooking, caseData, oldHearing));

                    if (isNotEmpty(hearingBooking.getNoticeOfHearing())) {
                        publishEvent(new SendNoticeOfHearing(caseData, hearingBooking, false));
                    }
                });
        }

        hearingsService.findHearingBooking(caseData.getCancelledHearingId(), caseData.getCancelledHearingDetails())
            .ifPresent(cancelledHearing -> {
                if (!isEmpty(cancelledHearing.getCancellationReason())) {
                    publishEvent(new SendNoticeOfHearingVacated(caseData, cancelledHearing,
                        isNotEmpty(caseData.getSelectedHearingId())));
                }
            });
    }

    private static JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }

    private boolean isAddingNewHearing(CaseData caseData) {
        return isEmpty(caseData.getHearingOption()) || NEW_HEARING.equals(caseData.getHearingOption());
    }

}
