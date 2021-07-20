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
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.events.TemporaryHearingJudgeAllocationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.PastHearingDatesValidatorService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.hearing.ManageHearingsOthersGenerator;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingEndDateGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.FUTURE_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.TO_RE_LIST_HEARING_LIST;
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
    private static final String SELECTED_HEARING_ID = "selectedHearingId";
    private static final String PRE_ATTENDANCE = "preHearingAttendanceDetails";
    private static final String CANCELLED_HEARING_DETAILS_KEY = "cancelledHearingDetails";
    private static final String HEARING_DOCUMENT_BUNDLE_KEY = "hearingFurtherEvidenceDocuments";
    private static final String HAS_SESSION_KEY = "hasSession";
    private static final String HEARING_ORDERS_BUNDLES_DRAFTS = "hearingOrdersBundlesDrafts";
    private static final String DRAFT_UPLOADED_CMOS = "draftUploadedCMOs";

    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final ManageHearingsService hearingsService;
    private final PastHearingDatesValidatorService pastHearingDatesValidatorService;
    private final ValidateEmailService validateEmailService;
    private final ManageHearingsOthersGenerator othersGenerator;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().remove(SELECTED_HEARING_ID);

        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getAllocatedJudge() != null) {
            caseDetails.getData().put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
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

        if (NEW_HEARING == caseData.getHearingOption()) {
            caseDetails.getData().putAll(hearingsService.initiateNewHearing(caseData));
            caseDetails.getData()
                .putAll(othersGenerator.generate(caseData, HearingBooking.builder().build()));
        } else if (EDIT_HEARING == caseData.getHearingOption()) {
            final UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);
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

            caseDetails.getData().putAll(othersGenerator.generate(caseData, hearingBooking));
        } else if (ADJOURN_HEARING == caseData.getHearingOption()) {
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

            caseDetails.getData().put(PAST_HEARING_LIST,
                hearingsService.asDynamicList(caseData.getPastAndTodayHearings(), hearingBookingId));

            HearingBooking hearingBooking = hearingsService
                .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                .orElse(HearingBooking.builder().build());

            caseDetails.getData().putAll(othersGenerator.generate(caseData, hearingBooking));
        } else if (VACATE_HEARING == caseData.getHearingOption()) {
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

            caseDetails.getData().put(FUTURE_HEARING_LIST,
                hearingsService.asDynamicList(caseData.getFutureAndTodayHearings(), hearingBookingId));

            HearingBooking hearingBooking = hearingsService
                .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                .orElse(HearingBooking.builder().build());

            caseDetails.getData().putAll(othersGenerator.generate(caseData, hearingBooking));
        } else if (RE_LIST_HEARING == caseData.getHearingOption()) {
            if (isEmpty(caseData.getToBeReListedHearings())) {
                return respond(caseDetails, List.of("There are no adjourned or vacated hearings to re-list"));
            }
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

            HearingBooking cancelledHearing = hearingsService
                .getHearingBooking(hearingBookingId, caseData.getCancelledHearingDetails());

            HearingBooking reListedHearingBooking = cancelledHearing.toBuilder()
                .previousHearingVenue(null)
                .startDate(null)
                .endDate(null)
                .build();

            caseDetails.getData().putAll(hearingsService.populateHearingCaseFields(
                reListedHearingBooking, caseData.getAllocatedJudge()));

            caseDetails.getData().put(FIRST_HEARING_FLAG, YES.getValue());
            caseDetails.getData().put(TO_RE_LIST_HEARING_LIST,
                hearingsService.asDynamicList(caseData.getToBeReListedHearings(), hearingBookingId));
        }

        if (NEW_HEARING != caseData.getHearingOption()) {
            UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);

            HearingBooking hearingBooking = hearingsService
                .findHearingBooking(hearingBookingId, caseData.getHearingDetails())
                .orElse(HearingBooking.builder().build());

            caseDetails.getData().putAll(othersGenerator.generate(caseData, hearingBooking));
        }

        return respond(caseDetails);
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

        if (isAddingNewHearing(caseData)) {
            errors = pastHearingDatesValidatorService.validateHearingDates(caseData.getHearingStartDate(),
                caseData.getHearingEndDate());
        } else {
            errors = validateGroupService.validateGroup(caseData, HearingDatesGroup.class);
            if (errors.isEmpty()) {
                errors = validateGroupService.validateGroup(caseData, HearingEndDateGroup.class);
            }
        }

        caseDetails.getData().putAll(hearingsService.populateFieldsWhenPastHearingDateAdded(
            caseData.getHearingStartDate(), caseData.getHearingEndDate()));

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
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        JudgeAndLegalAdvisor tempJudge = caseData.getJudgeAndLegalAdvisor();

        if (caseData.hasSelectedTemporaryJudge(tempJudge)) {
            Optional<String> error = validateEmailService.validate(tempJudge.getJudgeEmailAddress());

            if (error.isPresent()) {
                return respond(caseDetails, List.of(error.get()));
            }
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
            final UUID hearingBookingId = hearingsService.getSelectedHearingId(caseData);
            final HearingBooking hearingBooking = hearingsService.getCurrentHearingBooking(caseData);
            final Element<HearingBooking> hearingBookingElement = element(hearingBookingId, hearingBooking);

            hearingsService.addOrUpdate(hearingBookingElement, caseData);
            hearingsService.sendNoticeOfHearing(caseData, hearingBooking);

            data.put(SELECTED_HEARING_ID, hearingBookingId);
        } else if (ADJOURN_HEARING == caseData.getHearingOption()) {
            UUID adjournedHearingId = hearingsService.getSelectedHearingId(caseData);

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
            UUID vacatedHearingId = hearingsService.getSelectedHearingId(caseData);

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
        } else if (RE_LIST_HEARING == caseData.getHearingOption()) {
            final UUID cancelledHearingId = hearingsService.getSelectedHearingId(caseData);

            final HearingBooking reListedHearing = hearingsService.getCurrentHearingBooking(caseData);
            final UUID reListedHearingId = hearingsService.reListHearing(caseData, cancelledHearingId, reListedHearing);

            hearingsService.sendNoticeOfHearing(caseData, reListedHearing);

            data.put(SELECTED_HEARING_ID, reListedHearingId);
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

        if (isNotEmpty(caseData.getSelectedHearingId())) {
            if (isInGatekeepingState(callbackRequest.getCaseDetails())
                && standardDirectionsService.hasEmptyDates(caseData)) {
                publishEvent(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
            }

            hearingsService.findHearingBooking(caseData.getSelectedHearingId(), caseData.getHearingDetails())
                .ifPresent(hearingBooking -> {
                    if (isNotEmpty(hearingBooking.getNoticeOfHearing())) {
                        publishEvent(new SendNoticeOfHearing(caseData, hearingBooking));
                    }

                    if (isNewOrReListedHearing(caseData) && isTemporaryHearingJudge(hearingBooking)) {
                        publishEvent(new TemporaryHearingJudgeAllocationEvent(caseData, hearingBooking));
                    }
                });
        }

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

    private boolean isNewOrReListedHearing(CaseData caseData) {
        return caseData.getHearingOption() == null
            || NEW_HEARING.equals(caseData.getHearingOption()) || RE_LIST_NOW.equals(caseData.getHearingReListOption());
    }

    private boolean isTemporaryHearingJudge(HearingBooking hearingBooking) {
        return (hearingBooking.getHearingJudgeLabel() != null);
    }
}
