package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.PastHearingDatesValidatorService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.hearing.ManageHearingsOthersGenerator;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PREVIOUS_HEARING_VENUE_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

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

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
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
