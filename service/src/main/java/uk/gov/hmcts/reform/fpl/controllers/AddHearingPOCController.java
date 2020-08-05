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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
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
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
            data.put("hearingDateList", buildHearingDateList(caseData.getHearingDetails()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/populate-existing-hearing/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateExistingHearing(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        UUID hearingBookingId = mapper.convertValue(caseDetails.getData().get("hearingDateList"), UUID.class);

        caseDetails.getData().put("hearingDateList",
            ElementUtils.asDynamicList(caseData.getHearingDetails(),
                hearingBookingId, hearingBooking -> hearingBooking.toLabel(DATE)));

        HearingBooking hearingBooking = findHearingBooking(hearingBookingId, caseData.getHearingDetails());

        caseDetails.getData().put("hearingType", hearingBooking.getType());
        caseDetails.getData().put("hearingVenue", hearingBooking.getVenue());
        caseDetails.getData().put("hearingVenueCustom", hearingBooking.getVenueCustomAddress());
        caseDetails.getData().put("hearingNeedsBooked", hearingBooking.getHearingNeedsBooked());
        caseDetails.getData().put("hearingNeedsDetails", hearingBooking.getHearingNeedsDetails());
        caseDetails.getData().put("hearingStartDate", hearingBooking.getStartDate());
        caseDetails.getData().put("hearingEndDate", hearingBooking.getEndDate());
        caseDetails.getData().put("judgeAndLegalAdvisor", hearingBooking.getJudgeAndLegalAdvisor());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/validate-hearing-dates/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateHearingDatesMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = mapper.convertValue(callbackRequest.getCaseDetails(), CaseDetails.class);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getHearingPreferences() != null) {
            caseDetails.getData().put("hearingNeedsLabel", buildHearingPreferencesLabel(caseData.getHearingPreferences()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, HearingDatesGroup.class))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = mapper.convertValue(callbackRequest.getCaseDetails(), CaseDetails.class);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        HearingBooking hearingBooking = buildHearingBooking(caseData);

        if (isSendingNoticeOfHearing(caseData.getSendNoticeOfHearing())) {
            DocmosisNoticeOfHearing dnof = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
            DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(dnof,
                NOTICE_OF_HEARING);
            Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
                NOTICE_OF_HEARING.getDocumentTitle(now()));

            hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
        }

        List<Element<HearingBooking>> hearingBookingElements = List.of();

        // Editing previous hearing
        if (caseData.getUseExistingHearing() != null && caseData.getUseExistingHearing().equals(YES.getValue())) {
            DynamicList hearingList =
                mapper.convertValue(caseDetails.getData().get("hearingDateList"), DynamicList.class);

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

        caseDetails.getData().put(HEARING_DETAILS_KEY, hearingBookingElements);

        caseDetails.getData().remove("hearingType");
        caseDetails.getData().remove("hearingVenue");
        caseDetails.getData().remove("hearingVenueCustom");
        caseDetails.getData().remove("hearingNeedsBooked");
        caseDetails.getData().remove("hearingNeedsDetails");
        caseDetails.getData().remove("hearingStartDate");
        caseDetails.getData().remove("hearingEndDate");
        caseDetails.getData().remove("sendNoticeOfHearing");
        caseDetails.getData().remove("judgeAndLegalAdvisor");
        caseDetails.getData().remove("hasExistingHearings");
        caseDetails.getData().remove("hearingDateList");
        caseDetails.getData().remove("useExistingHearing");

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

    private DynamicList buildHearingDateList(List<Element<HearingBooking>> hearingBookings) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();

        for (int i = 0; i < hearingBookings.size(); i++) {
            HearingBooking hearingBooking = hearingBookings.get(i).getValue();

            if(isNull(hearingBooking.getIsAdjourned())) {

                DynamicListElement dynamicListElement = DynamicListElement.builder()
                    .label(hearingBooking.toLabel(DATE))
                    .code(hearingBookings.get(i).getId())
                    .build();

                dynamicListElements.add(dynamicListElement);
            }
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

    private HearingBooking buildHearingBooking(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .hearingNeedsBooked(caseData.getHearingNeedsBooked())
            .hearingNeedsDetails(caseData.getHearingNeedsDetails())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
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

    private String buildHearingPreferencesLabel(HearingPreferences hearingPreferences) {
        StringBuilder stringBuilder = new StringBuilder();

        if (hearingPreferences.getInterpreter() == null && hearingPreferences.getWelsh() == null
            && hearingPreferences.getIntermediary() == null && hearingPreferences.getDisabilityAssistance() == null
            && hearingPreferences.getExtraSecurityMeasures() == null) {
            return stringBuilder.toString();
        } else {
            stringBuilder.append("Court services already requested").append("\n").append("\n");
        }

        if (hearingPreferences.getInterpreter() != null) {
            stringBuilder.append("• Interpreter").append("\n");
        }

        if (hearingPreferences.getWelsh() != null) {
            stringBuilder.append("• Spoken or written welsh").append("\n");
        }

        if (hearingPreferences.getIntermediary() != null) {
            stringBuilder.append("• Intermediary").append("\n");
        }

        if (hearingPreferences.getDisabilityAssistance() != null) {
            stringBuilder.append("• Facilities or assistance for a disability").append("\n");
        }

        if (hearingPreferences.getExtraSecurityMeasures() != null) {
            stringBuilder.append("• Separate waiting room or other security measures").append("\n");
        }

        return stringBuilder.toString();
    }
}
