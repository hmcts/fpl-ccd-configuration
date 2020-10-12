package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageHearingsService {
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final Time time;

    public static final String FIRST_HEARING_FLAG = "firstHearingFlag";
    public static final String HEARING_DATE_LIST = "hearingDateList";

    public HearingVenue getPreviousHearingVenue(CaseData caseData) {
        List<HearingBooking> hearingsList = unwrapElements(caseData.getHearingDetails());

        HearingBooking previousHearingBooking;
        if (hearingsList.stream().anyMatch(hearing -> hearing.getStartDate().isBefore(time.now()))) {
            previousHearingBooking = hearingsList.stream()
                .filter(hearing -> hearing.getStartDate().isBefore(time.now()))
                .max(comparing(HearingBooking::getStartDate))
                .orElseThrow(NoHearingBookingException::new);
        } else {
            previousHearingBooking = caseData.getMostUrgentHearingBookingAfter(time.now());
        }

        return hearingVenueLookUpService.getHearingVenue(previousHearingBooking);
    }

    public Map<String, Object> populatePreviousVenueFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        HearingVenue previousHearingVenue = getPreviousHearingVenue(caseData);

        Address customAddress = "OTHER".equals(previousHearingVenue.getHearingVenueId())
            ? previousHearingVenue.getAddress() : null;

        data.put("previousHearingVenue",
            PreviousHearingVenue.builder()
                .previousVenue(hearingVenueLookUpService.buildHearingVenue(previousHearingVenue))
                .newVenueCustomAddress(customAddress)
                .build());

        return data;
    }

    public HearingBooking findHearingBooking(UUID id, List<Element<HearingBooking>> hearingBookings) {
        Optional<Element<HearingBooking>> hearingBookingElement = findElement(id, hearingBookings);

        if (hearingBookingElement.isPresent()) {
            return hearingBookingElement.get().getValue();
        }

        return HearingBooking.builder().build();
    }

    public Map<String, Object> populateHearingCaseFields(HearingBooking hearingBooking, Judge allocatedJudge) {
        Map<String, Object> caseFields = new HashMap<>();

        //Reconstruct judge fields for editing
        JudgeAndLegalAdvisor judgeAndLegalAdvisor;
        if (allocatedJudge != null && allocatedJudge.getJudgeLastName() != null) {
            judgeAndLegalAdvisor = prepareJudgeFields(hearingBooking.getJudgeAndLegalAdvisor(), allocatedJudge);
            judgeAndLegalAdvisor.setAllocatedJudgeLabel(buildAllocatedJudgeLabel(allocatedJudge));
        } else {
            judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();
        }

        if (OTHER.equals(hearingBooking.getType())) {
            caseFields.put("hearingTypeDetails", hearingBooking.getTypeDetails());
        }

        caseFields.put("hearingType", hearingBooking.getType());
        caseFields.put("hearingStartDate", hearingBooking.getStartDate());
        caseFields.put("hearingEndDate", hearingBooking.getEndDate());
        caseFields.put("judgeAndLegalAdvisor", judgeAndLegalAdvisor);

        if (hearingBooking.getPreviousHearingVenue() == null
            || hearingBooking.getPreviousHearingVenue().getPreviousVenue() == null) {
            caseFields.put("hearingVenue", hearingBooking.getVenue());
            caseFields.put("hearingVenueCustom", hearingBooking.getVenueCustomAddress());
        } else {
            caseFields.put("previousHearingVenue", hearingBooking.getPreviousHearingVenue());
        }

        return caseFields;
    }

    public void findAndSetPreviousVenueId(CaseData caseData) {
        if (caseData.getHearingDetails() != null && caseData.getPreviousHearingVenue() != null
            && caseData.getPreviousHearingVenue().getUsePreviousVenue().equals("Yes")) {

            PreviousHearingVenue previousVenueForEditedHearing = caseData.getPreviousHearingVenue();

            caseData.setPreviousVenueId(hearingVenueLookUpService.getVenueId(
                previousVenueForEditedHearing.getPreviousVenue()));
        }
    }

    public HearingBooking buildHearingBooking(CaseData caseData) {
        if (caseData.getPreviousHearingVenue() == null
            || caseData.getPreviousHearingVenue().getUsePreviousVenue() == null) {
            return buildFirstHearing(caseData);
        } else {
            return buildFollowingHearings(caseData);
        }
    }

    public void addNoticeOfHearing(CaseData caseData, HearingBooking hearingBooking) {
        DocmosisNoticeOfHearing notice = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(notice,
            NOTICE_OF_HEARING);
        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));

        hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
    }

    public List<Element<HearingBooking>> updateEditedHearingEntry(HearingBooking hearingBooking,
                                                                  UUID hearingId,
                                                                  List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .map(hearingBookingElement -> {
                if (hearingBookingElement.getId().equals(hearingId)) {
                    hearingBookingElement = element(hearingBookingElement.getId(), hearingBooking);
                }
                return hearingBookingElement;
            }).collect(Collectors.toList());
    }

    public Set<String> caseFieldsToBeRemoved() {
        return Set.of(
            "hearingType",
            "hearingTypeDetails",
            "hearingVenue",
            "hearingVenueCustom",
            "hearingStartDate",
            "hearingEndDate",
            "sendNoticeOfHearing",
            "judgeAndLegalAdvisor",
            "hasExistingHearings",
            "hearingDateList",
            "hearingOption",
            "noticeOfHearingNotes",
            "previousHearingVenue");
    }

    private HearingBooking buildFirstHearing(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .typeDetails(caseData.getHearingTypeDetails())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .additionalNotes(caseData.getNoticeOfHearingNotes())
            .build();
    }

    private HearingBooking buildFollowingHearings(CaseData caseData) {
        String customPreviousVenue = null;
        String venue;

        String usePreviousVenue = caseData.getPreviousHearingVenue().getUsePreviousVenue();

        //Set venue fields based on what the user chose on the venue screen
        if ("Yes".equals(usePreviousVenue)) {
            venue = caseData.getPreviousVenueId();
            if ("OTHER".equals(venue)) {
                customPreviousVenue = caseData.getPreviousHearingVenue().getPreviousVenue();
            }
        } else {
            venue = caseData.getPreviousHearingVenue().getNewVenue();
        }

        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .typeDetails(caseData.getHearingTypeDetails())
            .venue(venue)
            .venueCustomAddress(caseData.getPreviousHearingVenue().getNewVenueCustomAddress())
            .customPreviousVenue(customPreviousVenue)
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .previousHearingVenue(caseData.getPreviousHearingVenue())
            .additionalNotes(caseData.getNoticeOfHearingNotes())
            .build();
    }
}
