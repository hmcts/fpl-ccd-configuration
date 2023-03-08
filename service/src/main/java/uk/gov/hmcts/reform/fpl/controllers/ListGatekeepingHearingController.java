package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.events.TemporaryHearingJudgeAllocationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.PastHearingDatesValidatorService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.hearing.ManageHearingsOthersGenerator;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderEventNotificationDecider;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PREVIOUS_HEARING_VENUE_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingListingState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/list-gatekeeping-hearing")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
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
    private final CoreCaseDataService coreCaseDataService;
    private final GatekeepingOrderEventNotificationDecider notificationDecider;

    private final CaseConverter converter;

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
        caseDetails.getData().put("sendNoticeOfHearing", YES.getValue());

        setNewHearing(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData eventData = orderService.updateStandardDirections(callbackRequest.getCaseDetails());
        final CaseDetailsMap caseData = caseDetailsMap(caseDetails);

        hearingsService.findAndSetPreviousVenueId(eventData);

        createHearing(eventData, caseData);
        createGatekeepingOrder(callbackRequest, eventData, caseData);

        removeTemporaryFields(caseData,
            "urgentHearingOrderDocument",
            "urgentHearingAllocation",
            "showUrgentHearingAllocation",
            "currentSDO",
            "preparedSDO",
            "replacementSDO",
            "useServiceRoute",
            "useUploadRoute",
            "judgeAndLegalAdvisor",
            "gatekeepingOrderHearingDate1",
            "gatekeepingOrderHearingDate2",
            "gatekeepingOrderHasHearing1",
            "gatekeepingOrderHasHearing2",
            "gatekeepingOrderIssuingJudge",
            "customDirections"
        );

        caseData.keySet().removeAll(hearingsService.caseFieldsToBeRemoved());

        return respond(caseData);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = getCaseData(callbackRequest);
        publishEvent(new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest)));
        if (isNotEmpty(caseData.getSelectedHearingId())) {
            if (isInGatekeepingListingState(callbackRequest.getCaseDetails())
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

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();

        Map<String, Object> updates = new HashMap<>();

        updates.put("standardDirectionOrder", orderService.sealDocumentAfterEventSubmitted(caseData));

        final CaseData caseDataAfterSealing;
        if (updates.isEmpty()) {
            caseDataAfterSealing = caseData;
        } else {
            data.putAll(updates);
            caseDataAfterSealing = getCaseData(caseDetails);
        }

        coreCaseDataService.triggerEvent(caseDataAfterSealing.getId(),
            "internal-change-add-gatekeeping",
            updates);

        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        notificationDecider.buildEventToPublish(caseDataAfterSealing, caseDataBefore.getState())
            .ifPresent(eventToPublish -> {
                coreCaseDataService.triggerEvent(
                    JURISDICTION,
                    CASE_TYPE,
                    caseDataAfterSealing.getId(),
                    "internal-change-SEND_DOCUMENT",
                    Map.of("documentToBeSent", eventToPublish.getOrder()));

                publishEvent(eventToPublish);
            });
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
        final List<String> integerErrors = pastHearingDatesValidatorService.validateHearingIntegers(caseDetails);

        if (!integerErrors.isEmpty()) {
            return respond(caseDetails, integerErrors);
        }

        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = pastHearingDatesValidatorService.validateHearingDates(
            caseData.getHearingStartDate(),
            caseData.getHearingEndDateTime());
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

    private void createHearing(final CaseData eventData, final CaseDetailsMap caseData) {

        final HearingBooking hearingBooking =
            hearingsService.getCurrentHearingBooking(converter.convert(caseData, CaseData.class));
        final Element<HearingBooking> hearingBookingElement = element(hearingBooking);

        hearingsService.addOrUpdate(hearingBookingElement, eventData);
        hearingsService.sendNoticeOfHearing(eventData, hearingBooking);

        caseData.put(SELECTED_HEARING_ID, hearingBookingElement.getId());

        caseData.putIfNotEmpty(CANCELLED_HEARING_DETAILS_KEY, eventData.getCancelledHearingDetails());
        caseData.putIfNotEmpty(HEARING_DOCUMENT_BUNDLE_KEY, eventData.getHearingFurtherEvidenceDocuments());
        caseData.putIfNotEmpty(HEARING_DETAILS_KEY, eventData.getHearingDetails());
        caseData.put(HEARING_ORDERS_BUNDLES_DRAFTS, eventData.getHearingOrdersBundlesDrafts());
        caseData.put(DRAFT_UPLOADED_CMOS, eventData.getDraftUploadedCMOs());
    }

    private void createGatekeepingOrder(final CallbackRequest callbackRequest,
                                        CaseData eventData,
                                        final CaseDetailsMap caseData) {

        final GatekeepingOrderRoute sdoRouter = eventData.getGatekeepingOrderRouter();

        eventData = mergeEventAndCaseData(eventData, caseData);
        caseData.put("gatekeepingOrderSealDecision", orderService.buildSealedDecision(eventData));
        eventData = mergeEventAndCaseData(eventData, caseData);

        switch (sdoRouter) {
            case UPLOAD:
                caseData.put("standardDirectionOrder", orderService.buildOrderFromUploadedFile(eventData));
                break;
            case SERVICE:
                caseData.put("standardDirectionOrder", orderService.buildOrderFromGeneratedFile(eventData));
                break;
        }

        callbackRequest.getCaseDetails().setData(caseData);
        final List<DocmosisTemplates> nopTemplates = orderService.getNoticeOfProceedingsTemplates(eventData);
        caseData.put("noticeOfProceedingsBundle",
            nopService.uploadNoticesOfProceedings(getCaseData(callbackRequest.getCaseDetails()), nopTemplates));
    }

    private CaseData mergeEventAndCaseData(final CaseData eventData, final CaseDetailsMap caseData) {
        var eventDataMap = converter.toMap(eventData);
        eventDataMap.putAll(caseData);
        return converter.convert(eventDataMap, CaseData.class);
    }

    private boolean isNewOrReListedHearing(CaseData caseData) {
        return caseData.getHearingOption() == null
            || NEW_HEARING.equals(caseData.getHearingOption()) || RE_LIST_NOW.equals(caseData.getHearingReListOption());
    }

    private boolean isTemporaryHearingJudge(HearingBooking hearingBooking) {
        return (hearingBooking.getHearingJudgeLabel() != null);
    }
}
