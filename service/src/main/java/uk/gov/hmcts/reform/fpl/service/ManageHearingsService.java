package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageHearingsService {
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public static final String FIRST_HEARING_FLAG = "firstHearingFlag";
    public static final String HEARING_DATE_LIST = "hearingDateList";

    public Map<String, Object> populateInitialFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();

        data.put(HEARING_DATE_LIST, asDynamicList(futureHearings, hearing -> hearing.toLabel(DATE)));
        data.put("hasExistingHearings", YES.getValue());

        HearingVenue previousHearingVenue = getPreviousHearingVenue(caseData);

        data.put("previousHearingVenue",
            PreviousHearingVenue.builder().previousVenue(
                hearingVenueLookUpService.buildHearingVenue(previousHearingVenue))
                .build());
        data.put("previousVenueId", previousHearingVenue.getHearingVenueId());

        return data;
    }

    public HearingVenue getPreviousHearingVenue(CaseData caseData) {
        List<HearingBooking> hearingsList = unwrapElements(caseData.getHearingDetails());

        HearingBooking previousHearingBooking;
        if (hearingsList.stream().anyMatch(hearing -> hearing.getStartDate().isBefore(LocalDateTime.now()))) {
            previousHearingBooking = hearingsList.stream()
                .filter(hearing -> hearing.getStartDate().isBefore(LocalDateTime.now()))
                .max(comparing(HearingBooking::getStartDate))
                .orElseThrow(NoHearingBookingException::new);
        } else {
            previousHearingBooking = caseData.getMostUrgentHearingBookingAfter(LocalDateTime.now());
        }

        return hearingVenueLookUpService.getHearingVenue(previousHearingBooking);
    }

    public HearingBooking findHearingBooking(UUID id, List<Element<HearingBooking>> hearingBookings) {
        Optional<Element<HearingBooking>> hearingBookingElement = ElementUtils.findElement(id, hearingBookings);

        if (hearingBookingElement.isPresent()) {
            return hearingBookingElement.get().getValue();
        }

        return HearingBooking.builder().build();
    }

    public Map<String, Object> populateHearingCaseFields(HearingBooking hearingBooking) {
        return Map.of(
            "hearingType", hearingBooking.getType(),
            "hearingVenue", hearingBooking.getVenue(),
            "hearingVenueCustom", hearingBooking.getVenueCustomAddress(),
            "hearingStartDate", hearingBooking.getStartDate(),
            "hearingEndDate", hearingBooking.getEndDate(),
            "judgeAndLegalAdvisor", hearingBooking.getJudgeAndLegalAdvisor(),
            "previousHearingVenue", hearingBooking.getPreviousHearingVenue());
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
            NOTICE_OF_HEARING.getDocumentTitle(now()));

        hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
    }

    public List<Element<HearingBooking>> updateEditedHearingEntry(HearingBooking hearingBooking,
                                                                  UUID hearingId,
                                                                  List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .map(hearingBookingElement -> {
                if (hearingBookingElement.getId().equals(hearingId)) {
                    element(hearingBookingElement.getId(), hearingBooking);
                }
                return hearingBookingElement;
            }).collect(Collectors.toList());
    }

    public List<Element<HearingBooking>> appendHearingBooking(List<Element<HearingBooking>> currentHearingBookings,
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

    public Set<String> caseFieldsToBeRemoved() {
        return Set.of(
            "hearingType",
            "hearingVenue",
            "hearingVenueCustom",
            "hearingStartDate",
            "hearingEndDate",
            "sendNoticeOfHearing",
            "judgeAndLegalAdvisor",
            "hasExistingHearings",
            "hearingDateList",
            "useExistingHearing",
            "noticeOfHearingNotes");
    }

    private HearingBooking buildFirstHearing(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
            .additionalNotes(caseData.getNoticeOfHearingNotes())
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
            .previousHearingVenue(caseData.getPreviousHearingVenue())
            .build();
    }
}
