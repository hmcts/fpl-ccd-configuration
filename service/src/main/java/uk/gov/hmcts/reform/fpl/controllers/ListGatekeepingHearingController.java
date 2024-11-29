package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.events.judicial.NewAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.events.judicial.NewHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.PastHearingDatesValidatorService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.hearing.ManageHearingsOthersGenerator;
import uk.gov.hmcts.reform.fpl.service.sdo.ListGatekeepingHearingDecider;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.JUDGE_AND_LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PREVIOUS_HEARING_VENUE_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@RestController
@RequestMapping("/callback/list-gatekeeping-hearing")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListGatekeepingHearingController extends CallbackController {

    private static final String ALLOCATED_JUDGE = "allocatedJudge";
    private static final String FIRST_HEARING_FLAG = "firstHearingFlag";
    private static final String SELECTED_HEARING_ID = "selectedHearingId";
    private static final String PRE_ATTENDANCE = "preHearingAttendanceDetails";
    private static final String CANCELLED_HEARING_DETAILS_KEY = "cancelledHearingDetails";
    private static final String HEARING_ORDERS_BUNDLES_DRAFTS = "hearingOrdersBundlesDrafts";
    private static final String DRAFT_UPLOADED_CMOS = "draftUploadedCMOs";
    private static final String HAS_PREVIOUS_VENUE_HEARING = "hasPreviousHearingVenue";
    private final ManageHearingsService hearingsService;
    private final PastHearingDatesValidatorService pastHearingDatesValidatorService;
    private final ValidateEmailService validateEmailService;
    private final ManageHearingsOthersGenerator othersGenerator;
    private final GatekeepingOrderService orderService;
    private final NoticeOfProceedingsService nopService;
    private final ListGatekeepingHearingDecider listGatekeepingHearingDecider;
    private final CoreCaseDataService coreCaseDataService;
    private final JudicialService judicialService;
    private final SendDocumentService sendDocumentService;
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
        caseDetails.getData().put("enterManually", NO.getValue());
        caseDetails.getData().put("enterManuallyHearingJudge", NO.getValue());

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
            "customDirections",
            "judicialUser",
            "enterManually",
            "judicialUserHearingJudge",
            "enterManuallyHearingJudge",
            "hearingJudge",
            "allocatedJudgeLabel",
            "useAllocatedJudge"
        );

        caseData.keySet().removeAll(hearingsService.caseFieldsToBeRemoved());

        State endState = isNull(eventData.getGatekeepingOrderRouter())
            && eventData.isCareOrderCombinedWithUrgentDirections()  ? GATEKEEPING : CASE_MANAGEMENT;

        return respond(caseData, endState);
    }

    @PostMapping("/allocated-judge/mid-event")
    public AboutToStartOrSubmitCallbackResponse allocatedJudgeMidEvent(@RequestBody CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        Optional<String> error = judicialService.validateTempAllocatedJudge(caseData);

        if (error.isPresent()) {
            return respond(caseDetails, List.of(error.get()));
        }

        Judge allocatedJudge;
        if (caseData.getEnterManually().equals(YesNo.NO)
            && !ObjectUtils.isEmpty(caseData.getJudicialUser())
            && !ObjectUtils.isEmpty(caseData.getJudicialUser().getPersonalCode())) {

            Optional<JudicialUserProfile> jup = judicialService.getJudge(caseData.getJudicialUser().getPersonalCode());
            if (jup.isPresent()) {
                allocatedJudge = Judge.fromJudicialUserProfile(jup.get());
                caseDetails.getData().put(ALLOCATED_JUDGE, allocatedJudge);
            } else {
                return respond(caseDetails,
                    List.of("No Judge could be found, please retry your search or enter their details manually."));
            }
        } else {
            // lookup the manual entry to see if we can do a mapping anyway
            allocatedJudge = caseData.getTempAllocatedJudge();
            Optional<String> possibleId = judicialService
                .getJudgeUserIdFromEmail(allocatedJudge.getJudgeEmailAddress());
            if (possibleId.isPresent()) {
                // if they are in our maps - add their UUID extra info to the case
                caseDetails.getData().put(ALLOCATED_JUDGE,
                    allocatedJudge.toBuilder()
                        .judgeJudicialUser(JudicialUser.builder()
                            .idamId(possibleId.get())
                            .build())
                        .build());
            } else {
                // put the temporary manual entry into the proper field
                caseDetails.getData().put(ALLOCATED_JUDGE, caseData.getTempAllocatedJudge());
            }
        }

        // todo - test removal of this, as we use the manual label field now on the hearing judge page
        caseDetails.getData().put(
            JUDGE_AND_LEGAL_ADVISOR,
            JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel(buildAllocatedJudgeLabel(allocatedJudge))
                .build());

        caseDetails.getData().put("allocatedJudgeLabel", buildAllocatedJudgeLabel(allocatedJudge));

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

    @PostMapping("validate-judge-email/mid-event")
    public CallbackResponse validateJudgeEmailMidEvent(@RequestBody CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (NO.equals(caseData.getUseAllocatedJudge()) || isEmpty(caseData.getUseAllocatedJudge())) {
            // validate + add to caseDetails the judgeAndLegalAdvisor field
            List<String> possibleErrors = judicialService.validateHearingJudgeEmail(caseDetails, caseData);
            if (possibleErrors.size() > 0) {
                return respond(caseDetails, possibleErrors);
            }
        } else {
            // hearing judge == allocated judge
            JudgeAndLegalAdvisor hearingJudge = JudgeAndLegalAdvisor.from(caseData.getAllocatedJudge()).toBuilder()
                .legalAdvisorName(caseData.getLegalAdvisorName())
                .build();
            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR, hearingJudge);
        }


        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        final CaseData caseData = getCaseData(request);

        publishEvent(new NewAllocatedJudgeEvent(caseData.getAllocatedJudge(), caseData.getId()));
        triggerPostSubmissionHearingEvents(request);
        triggerPostSealingEvents(request);
        publishEvent(new AfterSubmissionCaseDataUpdated(caseData, getCaseDataBefore(request)));
    }


    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handlePostSubmittedEvent(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        removeTemporaryFields(caseDetails,
            "gatekeepingOrderSealDecision",
            "urgentDirectionsRouter");

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
        hearingsService.buildNoticeOfHearing(eventData, hearingBooking);

        caseData.put(SELECTED_HEARING_ID, hearingBookingElement.getId());

        caseData.putIfNotEmpty(CANCELLED_HEARING_DETAILS_KEY, eventData.getCancelledHearingDetails());
        caseData.putIfNotEmpty(HEARING_DETAILS_KEY, eventData.getHearingDetails());
        caseData.put(HEARING_ORDERS_BUNDLES_DRAFTS, eventData.getHearingOrdersBundlesDrafts());
        caseData.put(DRAFT_UPLOADED_CMOS, eventData.getDraftUploadedCMOs());
    }

    private void createGatekeepingOrder(final CallbackRequest callbackRequest,
                                        CaseData eventData,
                                        final CaseDetailsMap caseData) {

        eventData = mergeEventAndCaseData(eventData, caseData);
        caseData.put("gatekeepingOrderSealDecision", orderService.buildSealedDecision(eventData));
        eventData = mergeEventAndCaseData(eventData, caseData);

        final GatekeepingOrderRoute sdoRouter;
        final String orderType;
        if (nonNull(eventData.getGatekeepingOrderRouter())) {
            sdoRouter = eventData.getGatekeepingOrderRouter();
            orderType = "standardDirectionOrder";
        } else {
            sdoRouter = eventData.getUrgentDirectionsRouter();
            orderType = "urgentDirectionsOrder";
        }

        if (UPLOAD == sdoRouter) {
            caseData.put(orderType, orderService.buildOrderFromUploadedFile(eventData));
        } else if (SERVICE == sdoRouter) {
            caseData.put(orderType, orderService.buildOrderFromGeneratedFile(eventData));
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

    private void triggerPostSubmissionHearingEvents(CallbackRequest request) {
        final CaseData caseData = getCaseData(request);

        if (isNotEmpty(caseData.getSelectedHearingId())) {
            hearingsService.findHearingBooking(caseData.getSelectedHearingId(), caseData.getHearingDetails())
                .ifPresent(hearingBooking -> {
                    publishEvent(new NewHearingJudgeEvent(hearingBooking, caseData, Optional.empty()));

                    if (isNotEmpty(hearingBooking.getNoticeOfHearing())) {
                        publishEvent(new SendNoticeOfHearing(caseData, hearingBooking, true));
                    }
                });
        }
    }

    private void triggerPostSealingEvents(final CallbackRequest request) {
        final CaseDetails oldCaseDetails = request.getCaseDetails();

        CaseDetails caseDetails = coreCaseDataService.performPostSubmitCallback(oldCaseDetails.getId(),
            "internal-change-add-gatekeeping",
            details -> {
                CaseData caseData = getCaseData(details);
                Map<String, Object> updates = new HashMap<>();

                final GatekeepingOrderRoute sdoRouter;
                final String orderType;
                if (nonNull(caseData.getGatekeepingOrderRouter())) {
                    sdoRouter = caseData.getGatekeepingOrderRouter();
                    orderType = "standardDirectionOrder";
                } else {
                    sdoRouter = caseData.getUrgentDirectionsRouter();
                    orderType = "urgentDirectionsOrder";
                }

                if (sdoRouter == UPLOAD) {
                    updates.put(orderType, orderService.sealDocumentAfterEventSubmitted(caseData));
                }
                return updates;
            });

        if (isEmpty(caseDetails)) {
            // if our callback has failed 3 times, all we have is the prior caseData to send notifications based on
            caseDetails = oldCaseDetails;
        }

        final CaseData caseData = getCaseData(caseDetails);

        listGatekeepingHearingDecider.buildEventToPublish(caseData)
            .ifPresent(eventToPublish -> {
                sendDocumentService.sendDocuments(caseData, List.of(eventToPublish.getOrder()),
                    sendDocumentService.getRepresentativesServedByPost(caseData));
                publishEvent(eventToPublish);
            });
    }

}
