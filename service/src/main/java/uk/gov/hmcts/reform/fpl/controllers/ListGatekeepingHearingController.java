package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.PastHearingDatesValidatorService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.hearing.ManageHearingsOthersGenerator;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PREVIOUS_HEARING_VENUE_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/list-gatekeeping-hearing")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListGatekeepingHearingController extends CallbackController {

    private static final String FIRST_HEARING_FLAG = "firstHearingFlag";
    private static final String SELECTED_HEARING_ID = "selectedHearingId";
    private static final String PRE_ATTENDANCE = "preHearingAttendanceDetails";
    private static final String CANCELLED_HEARING_DETAILS_KEY = "cancelledHearingDetails";
    private static final String HEARING_DOCUMENT_BUNDLE_KEY = "hearingFurtherEvidenceDocuments";
    private static final String HAS_SESSION_KEY = "hasSession";
    private static final String HEARING_ORDERS_BUNDLES_DRAFTS = "hearingOrdersBundlesDrafts";
    private static final String DRAFT_UPLOADED_CMOS = "draftUploadedCMOs";
    private static final String HAS_PREVIOUS_VENUE_HEARING = "hasPreviousHearingVenue";

    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final ManageHearingsService hearingsService;
    private final PastHearingDatesValidatorService pastHearingDatesValidatorService;
    private final ValidateEmailService validateEmailService;
    private final ManageHearingsOthersGenerator othersGenerator;
    private final GatekeepingOrderService orderService;
    private final NoticeOfProceedingsService nopService;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final boolean isFirstHearing = isEmpty(caseData.getAllHearings());

        caseDetails.getData().remove(SELECTED_HEARING_ID);

        if (isFirstHearing) {
            caseDetails.getData().put(FIRST_HEARING_FLAG, YES.getValue());
            caseDetails.getData().put(PRE_ATTENDANCE, DEFAULT_PRE_ATTENDANCE);
            caseDetails.getData().putAll(othersGenerator.generate(caseData, HearingBooking.builder().build()));
        } else {
            caseDetails.getData().put(FIRST_HEARING_FLAG, NO.getValue());
        }

        caseDetails.getData().putAll(hearingsService.populateHearingLists(caseData));

        setNewHearing(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final CaseDetailsMap data = caseDetailsMap(caseDetails);

        hearingsService.findAndSetPreviousVenueId(caseData);

        //Set hearing
        final HearingBooking hearingBooking = hearingsService.getCurrentHearingBooking(caseData);
        final Element<HearingBooking> hearingBookingElement = element(hearingBooking);

        hearingsService.addOrUpdate(hearingBookingElement, caseData);
        hearingsService.sendNoticeOfHearing(caseData, hearingBooking);

        data.put(SELECTED_HEARING_ID, hearingBookingElement.getId());

        data.putIfNotEmpty(CANCELLED_HEARING_DETAILS_KEY, caseData.getCancelledHearingDetails());
        data.putIfNotEmpty(HEARING_DOCUMENT_BUNDLE_KEY, caseData.getHearingFurtherEvidenceDocuments());
        data.putIfNotEmpty(HEARING_DETAILS_KEY, caseData.getHearingDetails());
        data.put(HEARING_ORDERS_BUNDLES_DRAFTS, caseData.getHearingOrdersBundlesDrafts());
        data.put(DRAFT_UPLOADED_CMOS, caseData.getDraftUploadedCMOs());

        data.keySet().removeAll(hearingsService.caseFieldsToBeRemoved());

        //Add gatekeeping order
        callbackRequest.getCaseDetails().setData(data);
        final List<DocmosisTemplates> nopTemplates = orderService.getNoticeOfProceedingsTemplates(caseData);
        data.put("noticeOfProceedingsBundle",
            nopService.uploadNoticesOfProceedings(getCaseData(callbackRequest.getCaseDetails()), nopTemplates));

        removeTemporaryFields(data, "gatekeepingOrderIssuingJudge", "customDirections");

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
    }

    @PostMapping("allocated-judge/mid-event")
    public AboutToStartOrSubmitCallbackResponse allocatedJudgeMidEvent(@RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final Judge allocatedJudge = caseData.getAllocatedJudge();

        final Optional<String> error = validateEmailService.validate(allocatedJudge.getJudgeEmailAddress());

        if (error.isPresent()) {
            return respond(caseDetails, List.of(error.get()));
        }

        caseDetails.getData().put(
            "judgeAndLegalAdvisor",
            JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel(buildAllocatedJudgeLabel(allocatedJudge))
                .build());

        return respond(caseDetails);
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public CallbackResponse validateHearingDatesMidEvent(@RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = pastHearingDatesValidatorService.validateHearingIntegers(caseDetails);
        errors.addAll(
            pastHearingDatesValidatorService.validateHearingDates(caseData.getHearingStartDate(),
                caseData.getHearingEndDateTime()));
        errors.addAll(
            pastHearingDatesValidatorService.validateDays(caseData.getHearingDuration(),
                caseData.getHearingDays()));
        errors.addAll(
            pastHearingDatesValidatorService.validateHoursMinutes(
                caseData.getHearingDuration(),
                caseData.getHearingHours(),
                caseData.getHearingMinutes()));

        caseDetails.getData().putAll(hearingsService.populateFieldsWhenPastHearingDateAdded(caseData));

        return respond(caseDetails, errors);
    }

    @PostMapping("/validate-judge-email/mid-event")
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

    private void setNewHearing(final CaseDetails caseDetails) {

        final CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().put("hearingOption", NEW_HEARING);
        caseDetails.getData().putAll(hearingsService.initiateNewHearing(caseData));

        final PreviousHearingVenue previousHearingVenue = (PreviousHearingVenue) caseDetails.getData()
            .get(PREVIOUS_HEARING_VENUE_KEY);

        final boolean hasPreviousHearingVenue = previousHearingVenue != null
            && !StringUtils.isEmpty(previousHearingVenue.getPreviousVenue());

        caseDetails.getData().put(HAS_PREVIOUS_VENUE_HEARING, hasPreviousHearingVenue
            ? YES.getValue() : NO.getValue());
        caseDetails.getData()
            .putAll(othersGenerator.generate(caseData, HearingBooking.builder().build()));
    }
}
