package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
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
    private final ObjectMapper mapper;
    private final IdentityService identityService;
    private final Time time;

    public static final String HEARING_DETAILS_KEY = "hearingDetails";

    public UUID getSelectedHearingId(Object dynamicList) {
        return getDynamicListSelectedValue(dynamicList, mapper);
    }

    public DynamicList asDynamicList(List<Element<HearingBooking>> hearingBooking) {
        return asDynamicList(hearingBooking, null);
    }

    public DynamicList asDynamicList(List<Element<HearingBooking>> hearingBooking, UUID selectedId) {
        return ElementUtils.asDynamicList(hearingBooking, selectedId, HearingBooking::toLabel);
    }

    public UUID adjournAndReListHearing(CaseData caseData, UUID hearingId, HearingBooking hearingToBeReListed) {
        Element<HearingBooking> adjournedBooking = adjourn(caseData, hearingId, ADJOURNED_AND_RE_LISTED);
        Element<HearingBooking> reListedBooking = reList(caseData, hearingToBeReListed);

        reassignDocumentsBundle(caseData, adjournedBooking, reListedBooking);
        return reListedBooking.getId();
    }

    public UUID vacateAndReListHearing(CaseData caseData, UUID hearingId, HearingBooking hearingToBeReListed) {
        Element<HearingBooking> vacatedBooking = vacate(caseData, hearingId, VACATED_AND_RE_LISTED);
        Element<HearingBooking> reListedBooking = reList(caseData, hearingToBeReListed);

        reassignDocumentsBundle(caseData, vacatedBooking, reListedBooking);
        return reListedBooking.getId();
    }

    public void adjournHearing(CaseData caseData, UUID hearingToBeAdjourned) {
        adjourn(caseData, hearingToBeAdjourned, ADJOURNED);
    }

    public void vacateHearing(CaseData caseData, UUID hearingToBeVacated) {
        vacate(caseData, hearingToBeVacated, VACATED);
    }

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

    public Optional<HearingBooking> findHearingBooking(UUID id, List<Element<HearingBooking>> hearingBookings) {
        return findElement(id, hearingBookings).map(Element::getValue);
    }

    public HearingBooking getHearingBooking(UUID id, List<Element<HearingBooking>> hearingBookings) {
        return findHearingBooking(id, hearingBookings).orElseThrow(() -> new NoHearingBookingException(id));
    }

    public Map<String, Object> populateHearingCaseFields(HearingBooking hearingBooking, Judge allocatedJudge) {
        Map<String, Object> caseFields = new HashMap<>();

        //Reconstruct judge fields for editing
        JudgeAndLegalAdvisor judgeAndLegalAdvisor;
        if (allocatedJudge != null) {
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
        if (isNotEmpty(caseData.getHearingDetails()) && caseData.getPreviousHearingVenue() != null
            && caseData.getPreviousHearingVenue().getUsePreviousVenue().equals(YES.getValue())) {

            PreviousHearingVenue previousVenueForEditedHearing = caseData.getPreviousHearingVenue();

            caseData.setPreviousVenueId(hearingVenueLookUpService.getVenueId(
                previousVenueForEditedHearing.getPreviousVenue()));
        }
    }

    public HearingBooking getCurrentHearingBooking(CaseData caseData) {
        if (caseData.getPreviousHearingVenue() == null
            || caseData.getPreviousHearingVenue().getUsePreviousVenue() == null) {
            return buildFirstHearing(caseData);
        } else {
            return buildFollowingHearings(caseData);
        }
    }


    public void sendNoticeOfHearing(CaseData caseData, HearingBooking hearingBooking) {
        if (YES.getValue().equals(caseData.getSendNoticeOfHearing())) {
            DocmosisNoticeOfHearing notice = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
            DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(notice,
                NOTICE_OF_HEARING);
            Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
                NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));

            hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
        }
    }

    public void addOrUpdate(Element<HearingBooking> hearingBooking, CaseData caseData) {
        List<Element<HearingBooking>> hearingBookings = defaultIfNull(caseData.getHearingDetails(), new ArrayList<>());
        boolean exists = isNotEmpty(hearingBooking.getId()) && hearingBookings.stream()
            .anyMatch(hearing -> hearingBooking.getId().equals(hearing.getId()));

        if (exists) {
            caseData.setHearingDetails(hearingBookings.stream()
                .map(hearing -> hearing.getId().equals(hearingBooking.getId()) ? hearingBooking : hearing)
                .collect(toList()));
        } else {
            caseData.addHearingBooking(hearingBooking);
        }
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
            "previousHearingVenue",
            "firstHearingFlag",
            "adjournmentReason",
            "vacatedReason",
            "hearingReListOption");
    }

    public Object getSelectedDynamicListType(CaseData caseData) {
        if (VACATE_HEARING == caseData.getHearingOption()) {
            return caseData.getFutureAndTodayHearingDateList();
        }

        return caseData.getPastAndTodayHearingDateList();
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

    private Element<HearingBooking> reList(CaseData caseData, HearingBooking hearingBooking) {
        Element<HearingBooking> reListedBooking = element(identityService.generateId(), hearingBooking);
        caseData.addHearingBooking(reListedBooking);
        return reListedBooking;
    }

    private Element<HearingBooking> adjourn(CaseData caseData, UUID adjournedHearingId, HearingStatus hearingStatus) {
        Element<HearingBooking> originalHearingBooking = findElement(adjournedHearingId, caseData.getHearingDetails())
            .orElseThrow(() -> new NoHearingBookingException(adjournedHearingId));

        Element<HearingBooking> adjournedBooking = element(adjournedHearingId, originalHearingBooking.getValue()
            .toBuilder()
            .status(hearingStatus)
            .cancellationReason(caseData.getAdjournmentReason().getReason())
            .build());

        caseData.addCancelledHearingBooking(adjournedBooking);
        caseData.removeHearingDetails(originalHearingBooking);

        return adjournedBooking;
    }

    private Element<HearingBooking> vacate(CaseData caseData, UUID vacatedHearingId, HearingStatus hearingStatus) {
        Element<HearingBooking> originalHearingBooking = findElement(vacatedHearingId, caseData.getHearingDetails())
            .orElseThrow(() -> new NoHearingBookingException(vacatedHearingId));

        Element<HearingBooking> vacatedBooking = element(vacatedHearingId, originalHearingBooking.getValue()
            .toBuilder()
            .status(hearingStatus)
            .cancellationReason(getVacatedReason(caseData))
            .build());

        caseData.addCancelledHearingBooking(vacatedBooking);
        caseData.removeHearingDetails(originalHearingBooking);

        return vacatedBooking;
    }

    private String getVacatedReason(CaseData caseData) {
        if (caseData.getVacatedReason() != null) {
            return caseData.getVacatedReason().getReason();
        }

        return null;
    }

    private void reassignDocumentsBundle(CaseData caseData,
                                         Element<HearingBooking> sourceHearing,
                                         Element<HearingBooking> targetHearing) {
        findElement(sourceHearing.getId(), caseData.getHearingFurtherEvidenceDocuments()).ifPresent(
            sourceHearingBundle -> {
                Element<HearingFurtherEvidenceBundle> targetHearingBundle = element(targetHearing.getId(),
                    sourceHearingBundle.getValue().toBuilder()
                        .hearingName(targetHearing.getValue().toLabel())
                        .build());

                caseData.getHearingFurtherEvidenceDocuments().remove(sourceHearingBundle);
                caseData.getHearingFurtherEvidenceDocuments().add(targetHearingBundle);
            });
    }
}
