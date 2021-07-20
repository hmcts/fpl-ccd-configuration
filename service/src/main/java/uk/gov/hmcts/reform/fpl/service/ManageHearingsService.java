package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
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
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_LATER;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getHearingJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageHearingsService {

    public static final String HEARING_DETAILS_KEY = "hearingDetails";
    public static final String HAS_HEARINGS_TO_ADJOURN = "hasHearingsToAdjourn";
    public static final String HAS_HEARINGS_TO_VACATE = "hasHearingsToVacate";
    public static final String HAS_FUTURE_HEARING_FLAG = "hasFutureHearingDateFlag";
    public static final String HAS_HEARING_TO_RE_LIST = "hasHearingsToReList";
    public static final String HEARING_DATE_LIST = "hearingDateList";
    public static final String PAST_HEARING_LIST = "pastAndTodayHearingDateList";
    public static final String FUTURE_HEARING_LIST = "futureAndTodayHearingDateList";
    public static final String TO_RE_LIST_HEARING_LIST = "toReListHearingDateList";
    public static final String HAS_EXISTING_HEARINGS_FLAG = "hasExistingHearings";
    private static final String HEARING_START_DATE = "hearingStartDate";
    private static final String HEARING_END_DATE = "hearingEndDate";
    private static final String HEARING_START_DATE_LABEL = "hearingStartDateLabel";
    private static final String HEARING_END_DATE_LABEL = "hearingEndDateLabel";
    private static final String START_DATE_FLAG = "startDateFlag";
    private static final String END_DATE_FLAG = "endDateFlag";
    private static final String SHOW_PAST_HEARINGS_PAGE = "showConfirmPastHearingDatesPage";
    public static final String TO_RE_LIST_HEARING_LABEL = "toReListHearingsLabel";
    public static final String DEFAULT_PRE_ATTENDANCE = "1 hour before the hearing";

    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final OthersService othersService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final ObjectMapper mapper;
    private final IdentityService identityService;
    private final Time time;

    public Map<String, Object> populateHearingLists(CaseData caseData) {

        List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();
        List<Element<HearingBooking>> pastAndTodayHearings = caseData.getPastAndTodayHearings();
        List<Element<HearingBooking>> futureAndTodayHearing = caseData.getFutureAndTodayHearings();
        List<Element<HearingBooking>> toBeReListedHearings = caseData.getToBeReListedHearings();

        Map<String, Object> listAndLabel = new HashMap<>(Map.of(
            HEARING_DATE_LIST, asDynamicList(futureHearings),
            PAST_HEARING_LIST, asDynamicList(pastAndTodayHearings),
            FUTURE_HEARING_LIST, asDynamicList(futureAndTodayHearing),
            TO_RE_LIST_HEARING_LIST, asDynamicList(toBeReListedHearings)
        ));

        if (isNotEmpty(caseData.getHearingDetails()) || isNotEmpty(caseData.getToBeReListedHearings())) {
            listAndLabel.put(HAS_EXISTING_HEARINGS_FLAG, YES.getValue());
        }

        if (isNotEmpty(futureHearings)) {
            listAndLabel.put(HAS_FUTURE_HEARING_FLAG, YES.getValue());
        }

        if (isNotEmpty(pastAndTodayHearings)) {
            listAndLabel.put(HAS_HEARINGS_TO_ADJOURN, YES.getValue());
        }

        if (isNotEmpty(futureAndTodayHearing)) {
            listAndLabel.put(HAS_HEARINGS_TO_VACATE, YES.getValue());
        }

        if (isNotEmpty(toBeReListedHearings)) {
            listAndLabel.put(HAS_HEARING_TO_RE_LIST, YES.getValue());
            listAndLabel.put(TO_RE_LIST_HEARING_LABEL, hearingLabels(toBeReListedHearings));
        }

        return listAndLabel;
    }

    public DynamicList asDynamicList(List<Element<HearingBooking>> hearingBooking) {
        return asDynamicList(hearingBooking, null);
    }

    public DynamicList asDynamicList(List<Element<HearingBooking>> hearingBooking, UUID selectedId) {
        return ElementUtils.asDynamicList(hearingBooking, selectedId, HearingBooking::toLabel);
    }

    public UUID adjournAndReListHearing(CaseData caseData, UUID hearingId, HearingBooking hearingToBeReListed) {
        return cancelAndReListHearing(caseData, hearingId, hearingToBeReListed, ADJOURNED_AND_RE_LISTED);
    }

    public UUID vacateAndReListHearing(CaseData caseData, UUID hearingId, HearingBooking hearingToBeReListed) {
        return cancelAndReListHearing(caseData, hearingId, hearingToBeReListed, VACATED_AND_RE_LISTED);
    }

    public void adjournHearing(CaseData caseData, UUID hearingToBeAdjourned) {
        final HearingReListOption reListOption = caseData.getHearingReListOption();
        final HearingStatus hearingStatus = RE_LIST_LATER == reListOption ? ADJOURNED_TO_BE_RE_LISTED : ADJOURNED;

        cancelHearing(caseData, hearingToBeAdjourned, hearingStatus);
    }

    public void vacateHearing(CaseData caseData, UUID hearingToBeVacated) {
        final HearingReListOption reListOption = caseData.getHearingReListOption();
        final HearingStatus hearingStatus = RE_LIST_LATER == reListOption ? VACATED_TO_BE_RE_LISTED : VACATED;

        cancelHearing(caseData, hearingToBeVacated, hearingStatus);
    }

    public UUID reListHearing(CaseData caseData, UUID cancelledHearingId, HearingBooking newHearing) {
        Element<HearingBooking> cancelledHearing =
            findElement(cancelledHearingId, caseData.getCancelledHearingDetails())
                .orElseThrow(() -> new NoHearingBookingException(cancelledHearingId));

        HearingStatus newHearingStatus = cancelledHearing.getValue().isAdjourned()
            ? ADJOURNED_AND_RE_LISTED : VACATED_AND_RE_LISTED;

        cancelledHearing.getValue().setStatus(newHearingStatus);

        Element<HearingBooking> reListedBooking = reList(caseData, newHearing);

        reassignDocumentsBundle(caseData, cancelledHearing, reListedBooking);

        return reListedBooking.getId();
    }

    public Map<String, Object> initiateNewHearing(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (isEmpty(caseData.getHearingDetails())) {
            return data;
        }

        HearingVenue previousHearingVenue = getPreviousHearingVenue(caseData);

        Address customAddress = "OTHER".equals(previousHearingVenue.getHearingVenueId())
            ? previousHearingVenue.getAddress() : null;

        data.put("previousHearingVenue",
            PreviousHearingVenue.builder()
                .previousVenue(hearingVenueLookUpService.buildHearingVenue(previousHearingVenue))
                .newVenueCustomAddress(customAddress)
                .build());

        data.put("preHearingAttendanceDetails", DEFAULT_PRE_ATTENDANCE);

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
        caseFields.put(HEARING_START_DATE, hearingBooking.getStartDate());
        caseFields.put(HEARING_END_DATE, hearingBooking.getEndDate());
        caseFields.put("judgeAndLegalAdvisor", judgeAndLegalAdvisor);
        caseFields.put("hearingAttendance", hearingBooking.getAttendance());
        caseFields.put("hearingAttendanceDetails", hearingBooking.getAttendanceDetails());
        caseFields.put("preHearingAttendanceDetails", hearingBooking.getPreAttendanceDetails());

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
            && YES.getValue().equals(caseData.getPreviousHearingVenue().getUsePreviousVenue())) {

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
        DocmosisNoticeOfHearing notice = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(notice,
            NOTICE_OF_HEARING);
        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));

        hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));

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

    public Object getHearingsDynamicList(CaseData caseData) {
        switch (caseData.getHearingOption()) {
            case VACATE_HEARING:
                return caseData.getFutureAndTodayHearingDateList();
            case ADJOURN_HEARING:
                return caseData.getPastAndTodayHearingDateList();
            case RE_LIST_HEARING:
                return caseData.getToReListHearingDateList();
            case EDIT_HEARING:
                return caseData.getHearingDateList();
            default:
                return null;
        }
    }

    public UUID getSelectedHearingId(CaseData caseData) {
        return ofNullable(getHearingsDynamicList(caseData))
            .map(dynamicList -> getDynamicListSelectedValue(dynamicList, mapper))
            .orElse(null);
    }

    public Set<String> caseFieldsToBeRemoved() {
        return Set.of(
            "hearingType",
            "hearingTypeDetails",
            "hearingVenue",
            "hearingVenueCustom",
            HEARING_START_DATE,
            HEARING_END_DATE,
            "sendNoticeOfHearing",
            "judgeAndLegalAdvisor",
            "noticeOfHearingNotes",
            "previousHearingVenue",
            "firstHearingFlag",
            "adjournmentReason",
            "vacatedReason",
            HEARING_DATE_LIST,
            PAST_HEARING_LIST,
            FUTURE_HEARING_LIST,
            HAS_HEARINGS_TO_ADJOURN,
            HAS_HEARINGS_TO_VACATE,
            HAS_EXISTING_HEARINGS_FLAG,
            HAS_FUTURE_HEARING_FLAG,
            "hearingReListOption",
            HEARING_START_DATE_LABEL,
            "showConfirmPastHearingDatesPage",
            HEARING_END_DATE_LABEL,
            "confirmHearingDate",
            "hearingStartDateConfirmation",
            "hearingEndDateConfirmation",
            START_DATE_FLAG,
            END_DATE_FLAG,
            "hasSession",
            "hearingAttendance",
            "hearingAttendanceDetails",
            "preHearingAttendanceDetails",
            "hearingOption",
            "hasOthers",
            "sendOrderToAllOthers",
            "othersSelector",
            "others_label"
        );
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

    public Map<String, Object> populateFieldsWhenPastHearingDateAdded(LocalDateTime hearingStartDate,
                                                                      LocalDateTime hearingEndDate) {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime currentDateTime = LocalDateTime.now();

        data.put(SHOW_PAST_HEARINGS_PAGE, NO.getValue());

        if (hearingStartDate.isBefore(currentDateTime)) {
            data.put(HEARING_START_DATE_LABEL, formatLocalDateTimeBaseUsingFormat(hearingStartDate, DATE_TIME));
            data.put(START_DATE_FLAG, YES.getValue());
            data.put(SHOW_PAST_HEARINGS_PAGE, YES.getValue());
        }
        if (hearingEndDate.isBefore(currentDateTime)) {
            data.put(HEARING_END_DATE_LABEL, formatLocalDateTimeBaseUsingFormat(hearingEndDate, DATE_TIME));
            data.put(END_DATE_FLAG, YES.getValue());
            data.put(SHOW_PAST_HEARINGS_PAGE, YES.getValue());
        }

        return data;
    }

    public Map<String, Object> updateHearingDates(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (isNotEmpty(caseData.getHearingEndDateConfirmation()) && isNotEmpty(caseData
            .getHearingStartDateConfirmation())) {
            data.put(HEARING_START_DATE, caseData.getHearingStartDateConfirmation());
            data.put(HEARING_END_DATE, caseData.getHearingEndDateConfirmation());
        } else if (isNotEmpty(caseData.getHearingStartDateConfirmation())) {
            data.put(HEARING_START_DATE, caseData.getHearingStartDateConfirmation());
        } else if (isNotEmpty(caseData.getHearingEndDateConfirmation())) {
            data.put(HEARING_END_DATE, caseData.getHearingEndDateConfirmation());
        }

        return data;
    }

    private UUID cancelAndReListHearing(CaseData caseData,
                                        UUID hearingId,
                                        HearingBooking hearingToBeReListed,
                                        HearingStatus hearingStatus) {
        Element<HearingBooking> cancelledBooking = cancelHearing(caseData, hearingId, hearingStatus);
        Element<HearingBooking> reListedBooking = reList(caseData, hearingToBeReListed);

        reassignDocumentsBundle(caseData, cancelledBooking, reListedBooking);
        return reListedBooking.getId();
    }

    private HearingBooking buildFirstHearing(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .typeDetails(caseData.getHearingTypeDetails())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .attendance(caseData.getHearingAttendance())
            .attendanceDetails(caseData.getHearingAttendanceDetails())
            .preAttendanceDetails(caseData.getPreHearingAttendanceDetails())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .allocatedJudgeLabel(caseData.getAllocatedJudge() != null
                ? formatJudgeTitleAndName(caseData.getAllocatedJudge().toJudgeAndLegalAdvisor()) : null)
            .hearingJudgeLabel(getHearingJudge(caseData.getJudgeAndLegalAdvisor()))
            .legalAdvisorLabel(getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .others(othersService.getSelectedOthers(caseData))
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
            .attendance(caseData.getHearingAttendance())
            .attendanceDetails(caseData.getHearingAttendanceDetails())
            .preAttendanceDetails(caseData.getPreHearingAttendanceDetails())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .allocatedJudgeLabel(caseData.getAllocatedJudge() != null
                ? formatJudgeTitleAndName(caseData.getAllocatedJudge().toJudgeAndLegalAdvisor()) : null)
            .hearingJudgeLabel(getHearingJudge(caseData.getJudgeAndLegalAdvisor()))
            .legalAdvisorLabel(getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .others(othersService.getSelectedOthers(caseData))
            .previousHearingVenue(caseData.getPreviousHearingVenue())
            .additionalNotes(caseData.getNoticeOfHearingNotes())
            .build();
    }

    private Element<HearingBooking> reList(CaseData caseData, HearingBooking hearingBooking) {
        Element<HearingBooking> reListedBooking = element(identityService.generateId(), hearingBooking);
        caseData.addHearingBooking(reListedBooking);
        return reListedBooking;
    }

    private Element<HearingBooking> cancelHearing(CaseData caseData, UUID hearingId, HearingStatus hearingStatus) {
        Element<HearingBooking> originalHearingBooking = findElement(hearingId, caseData.getHearingDetails())
            .orElseThrow(() -> new NoHearingBookingException(hearingId));

        Element<HearingBooking> cancelledHearing = element(hearingId, originalHearingBooking.getValue()
            .toBuilder()
            .status(hearingStatus)
            .cancellationReason(getCancellationReason(caseData, hearingStatus))
            .build());

        caseData.addCancelledHearingBooking(cancelledHearing);
        caseData.removeHearingDetails(originalHearingBooking);

        updateDocumentsBundleName(caseData, cancelledHearing);

        if (isNotEmpty(cancelledHearing.getValue().getCaseManagementOrderId())) {
            updateHearingOrderBundles(caseData, cancelledHearing);
            updateDraftUploadedCaseManagementOrders(caseData, cancelledHearing);
        }

        return cancelledHearing;
    }

    private String getCancellationReason(CaseData caseData, HearingStatus hearingStatus) {
        if (caseData.getVacatedReason() != null
            && VACATED.equals(hearingStatus) || VACATED_AND_RE_LISTED.equals(hearingStatus)
            || VACATED_TO_BE_RE_LISTED.equals(hearingStatus)) {
            return caseData.getVacatedReason() != null ? caseData.getVacatedReason().getReason() : null;
        } else if (ADJOURNED.equals(hearingStatus) || ADJOURNED_AND_RE_LISTED.equals(hearingStatus)
            || ADJOURNED_TO_BE_RE_LISTED.equals(hearingStatus)) {
            return caseData.getAdjournmentReason().getReason();
        }

        return null;
    }

    private void updateDocumentsBundleName(CaseData caseData, Element<HearingBooking> hearing) {
        findElement(hearing.getId(), caseData.getHearingFurtherEvidenceDocuments())
            .map(Element::getValue)
            .ifPresent(bundle -> bundle.setHearingName(hearing.getValue().toLabel()));
    }

    private void updateDraftUploadedCaseManagementOrders(CaseData caseData, Element<HearingBooking> hearing) {
        UUID linkedCmoId = hearing.getValue().getCaseManagementOrderId();
        Optional<Element<HearingOrder>> draftUploadedOrderElement = caseData.getDraftUploadedCMOWithId(linkedCmoId);

        if (draftUploadedOrderElement.isPresent()) {
            List<Element<HearingOrder>> updatedDraftOrders = caseData.getDraftUploadedCMOs().stream()
                .map(hearingOrderElement -> {
                    if (hearingOrderElement.getId().equals(draftUploadedOrderElement.get().getId())) {
                        hearingOrderElement.getValue().setHearing(hearing.getValue().toLabel());
                    }
                    return hearingOrderElement;
                }).collect(toList());

            caseData.toBuilder().draftUploadedCMOs(updatedDraftOrders).build();
        }
    }

    private void updateHearingOrderBundles(CaseData caseData, Element<HearingBooking> hearing) {
        UUID linkedCmoId = hearing.getValue().getCaseManagementOrderId();

        Optional<Element<HearingOrdersBundle>> hearingBundleWithLinkedCMO =
            caseData.getHearingOrderBundleThatContainsOrder(linkedCmoId);

        hearingBundleWithLinkedCMO.ifPresent(hearingOrdersBundleElement ->
            nullSafeList(caseData.getHearingOrdersBundlesDrafts())
                .forEach(hearingBundleElement -> {
                        if (hearingOrdersBundleElement.getId().equals(hearingBundleElement.getId())) {
                            hearingBundleElement.getValue().setHearingName(hearing.getValue().toLabel());
                            hearingBundleElement.getValue().getOrders().forEach(
                                order -> order.getValue().setHearing(hearing.getValue().toLabel()));
                        }
                    }
                ));
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

    private String hearingLabels(List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .map(Element::getValue)
            .map(HearingBooking::toLabel)
            .collect(Collectors.joining("\n"));
    }
}
