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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptionsPOCType.EDIT_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Api
@RestController
@RequestMapping("/callback/add-hearing-poc")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddHearingPOCController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = mapper.convertValue(callbackRequest.getCaseDetails(), CaseDetails.class);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        Map<String, Object> data = caseDetails.getData();

        if (caseData.getAllocatedJudge() != null) {
            data.put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
        }

        if (hasExistingHearingBookings(caseData.getHearingDetails())) {
            data.put("hasExistingHearings", YES.getValue());
            data.put("hearingDateList", buildDraftHearingDateList(caseData.getHearingDetails()));
        }

        if (caseData.getHearingDetails() == null) {
            data.put("isFirstHearing", YES.getValue());
            System.out.println("This is the first hearing");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/populate-existing-hearings/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateExistingDraftHearing
        (@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        UUID hearingBookingId = mapper.convertValue(caseDetails.getData().get("hearingDateList"), UUID.class);

        caseDetails.getData().put("hearingDateList", ElementUtils.asDynamicList(caseData.getHearingDetails(),
            hearingBookingId, hearingBooking -> hearingBooking.toLabel(DATE)));

        HearingBooking hearingBooking = findHearingBooking(hearingBookingId, caseData.getHearingDetails());

        populateHearingBooking(caseDetails, hearingBooking);

        if ("Yes".equals(hearingBooking.getIsFirstHearing())) {
            caseDetails.getData().put("isFirstHearing", "Yes");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateHearingDatesMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = mapper.convertValue(callbackRequest.getCaseDetails(), CaseDetails.class);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, HearingDatesGroup.class))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = mapper.convertValue(callbackRequest.getCaseDetails(), CaseDetails.class);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        HearingBooking hearingBooking;
        if (caseData.getPreviousHearingVenue() == null
            || caseData.getPreviousHearingVenue().getUsePreviousVenue() == null) {
            hearingBooking = buildFirstHearing(caseData);
        } else {
            hearingBooking = buildFollowingHearings(caseData);
        }

        if (caseData.getSendNoticeOfHearing() != null
            && isSendingNoticeOfHearing(caseData.getSendNoticeOfHearing())) {
            DocmosisNoticeOfHearing dnof = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
            DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(dnof,
                NOTICE_OF_HEARING);
            Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
                NOTICE_OF_HEARING.getDocumentTitle(now()));

            hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
        }

        List<Element<HearingBooking>> hearingBookingElements;

        // Editing previous hearing
        if ((caseData.getUseExistingHearing() != null) && EDIT_DRAFT.equals(caseData.getUseExistingHearing())) {

            DynamicList hearingList;

            hearingList = mapper.convertValue(caseDetails.getData().get("hearingDateList"), DynamicList.class);

            UUID editedHearingId = hearingList.getValueCode();

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
            hearingBookingElements = appendHearingBooking(caseData.getHearingDetails(), hearingBooking);
        }

        HearingBooking mostRecentHearingBooking = unwrapElements(hearingBookingElements).stream().min(
            comparing(HearingBooking::getStartDate)).orElseThrow(NoHearingBookingException::new);

        HearingVenue mostRecentVenue = hearingVenueLookUpService.getHearingVenue(mostRecentHearingBooking);

        caseDetails.getData().put(HEARING_DETAILS_KEY, hearingBookingElements);
        caseDetails.getData().put("isFirstHearing", "No");

        //Set previousHearingVenue to be the venue of the most recent hearing
        caseDetails.getData().put("previousHearingVenue",
            PreviousHearingVenue.builder()
                .previousVenue(hearingVenueLookUpService.buildHearingVenue(mostRecentVenue))
                .build());
        caseDetails.getData().put("previousVenueId", mostRecentVenue.getHearingVenueId());

        removeHearingProperties(caseDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
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

        if (!hasExistingHearingBookings(currentHearingBookings)) {
            return List.of(hearingBookingElement);
        }

        currentHearingBookings.add(hearingBookingElement);
        return currentHearingBookings;
    }

    private HearingBooking buildFirstHearing(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
            .isFirstHearing("Yes")
            .build();
    }

    private HearingBooking buildFollowingHearings(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .venue(caseData.getPreviousHearingVenue().getUsePreviousVenue().equals("Yes")
                ? caseData.getPreviousVenueId() : caseData.getPreviousHearingVenue().getNewVenue())
            .venueCustomAddress(caseData.getPreviousHearingVenue().getVenueCustomAddress())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
            .isFirstHearing(caseData.getIsFirstHearing())
            .previousHearingVenue(caseData.getPreviousHearingVenue())
            .build();
    }

    private JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }

    private boolean isSendingNoticeOfHearing(String sendNoticeOfHearing) {
        return sendNoticeOfHearing.equals(YES.getValue());
    }

    private boolean hasExistingHearingBookings(List<Element<HearingBooking>> hearingBookings) {
        return isNotEmpty(hearingBookings);
    }

    private void populateHearingBooking(CaseDetails caseDetails, HearingBooking hearingBooking) {
        caseDetails.getData().put("hearingType", hearingBooking.getType());
        caseDetails.getData().put("hearingVenue", hearingBooking.getVenue());
        caseDetails.getData().put("hearingVenueCustom", hearingBooking.getVenueCustomAddress());
        caseDetails.getData().put("hearingStartDate", hearingBooking.getStartDate());
        caseDetails.getData().put("hearingEndDate", hearingBooking.getEndDate());
        caseDetails.getData().put("judgeAndLegalAdvisor", hearingBooking.getJudgeAndLegalAdvisor());
        caseDetails.getData().put("isFirstHearing", hearingBooking.getIsFirstHearing());
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
    }
}
