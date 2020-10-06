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
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.MultiPageHearingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptionsPOCType.EDIT_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
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
            caseDetails.getData().put("hearingDateList", buildDraftHearingDateList(futureHearings));
            caseDetails.getData().put("hasExistingHearings", YES.getValue());
        }

        return respond(caseDetails);
    }

    @PostMapping("/populate-existing-hearings/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateExistingDraftHearing
        (@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        UUID hearingBookingId = mapper.convertValue(caseDetails.getData().get("hearingDateList"), UUID.class);

        caseDetails.getData().put("hearingDateList", caseData.buildDynamicHearingList(hearingBookingId));

        HearingBooking hearingBooking = findHearingBooking(hearingBookingId, caseData.getHearingDetails());

        populateHearingBooking(caseDetails, hearingBooking);

        if (hearingBookingId.equals(caseData.getHearingDetails().get(0).getId())) {
            caseDetails.getData().put("firstHearingFlag", "Yes");
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

            DynamicList hearingList;

            hearingList = mapper.convertValue(caseDetails.getData().get("hearingDateList"), DynamicList.class);

            UUID editedHearingId = hearingList.getValueCode();
            caseDetails.getData().put("selectedHearingIds", List.of(editedHearingId));

            hearingBookingElements = caseData.getHearingDetails().stream()
                .map(hearingBookingElement -> {
                    if (hearingBookingElement.getId().equals(editedHearingId)) {
                        hearingBookingElement = Element.<HearingBooking>builder()
                            .id(hearingBookingElement.getId())
                            .value(hearingBooking)
                            .build();
                    }
                    return hearingBookingElement;
                }).collect(Collectors.toList());
        } else {
            hearingBookingElements = appendHearingBooking(defaultIfNull(caseData.getHearingDetails(), List.of()),
                hearingBooking);
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

    private HearingBooking findHearingBooking(UUID id, List<Element<HearingBooking>> hearingBookings) {
        Optional<Element<HearingBooking>> hearingBookingElement = ElementUtils.findElement(id, hearingBookings);

        if (hearingBookingElement.isPresent()) {
            return hearingBookingElement.get().getValue();
        }

        return HearingBooking.builder().build();
    }

    private DynamicList buildDraftHearingDateList(List<Element<HearingBooking>> hearingBookings) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();

        for (Element<HearingBooking> booking : hearingBookings) {
            HearingBooking hearingBooking = booking.getValue();

            DynamicListElement dynamicListElement = DynamicListElement.builder()
                .label(hearingBooking.toLabel(DATE))
                .code(booking.getId())
                .build();

            dynamicListElements.add(dynamicListElement);
        }

        if (dynamicListElements.isEmpty()) {
            return null;
        }

        return DynamicList.builder()
            .listItems(dynamicListElements)
            .value(dynamicListElements.get(0))
            .build();
    }

    private List<Element<HearingBooking>> appendHearingBooking(List<Element<HearingBooking>> currentHearingBookings,
                                                               HearingBooking hearingBooking) {
        Element<HearingBooking> hearingBookingElement = Element.<HearingBooking>builder()
            .id(UUID.randomUUID())
            .value(hearingBooking)
            .build();

        if (currentHearingBookings.isEmpty()) {
            return List.of(hearingBookingElement);
        }

        currentHearingBookings.add(hearingBookingElement);
        return currentHearingBookings;
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
        caseDetails.getData().put("previousHearingVenue", hearingBooking.getPreviousHearingVenue());
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
