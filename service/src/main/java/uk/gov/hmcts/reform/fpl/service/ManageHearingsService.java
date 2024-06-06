package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingDuration;
import uk.gov.hmcts.reform.fpl.enums.HearingHousekeepReason;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.ManageHearingHousekeepEventData;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.others.OthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING_VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.HOURS_MINS;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_LATER;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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
    public static final String HAS_PAST_HEARINGS = "hasPastHearings";
    public static final String HAS_HEARINGS_TO_VACATE = "hasHearingsToVacate";
    public static final String HAS_FUTURE_HEARINGS = "hasFutureHearings";
    public static final String HAS_HEARING_TO_RE_LIST = "hasHearingsToReList";
    public static final String PAST_HEARING_DATE_LIST = "pastHearingDateList";
    public static final String FUTURE_HEARING_LIST = "futureHearingDateList";
    public static final String PAST_AND_TODAY_HEARING_DATE_LIST = "pastAndTodayHearingDateList";
    public static final String VACATE_HEARING_LIST = "vacateHearingDateList";
    public static final String TO_RE_LIST_HEARING_LIST = "toReListHearingDateList";
    public static final String HAS_EXISTING_HEARINGS_FLAG = "hasExistingHearings";
    private static final String HEARING_START_DATE = "hearingStartDate";
    private static final String HEARING_END_DATE = "hearingEndDate";
    private static final String HEARING_START_DATE_LABEL = "hearingStartDateLabel";
    private static final String HEARING_DURATION_LABEL = "hearingDurationLabel";
    private static final String START_DATE_FLAG = "startDateFlag";
    private static final String END_DATE_FLAG = "endDateFlag";
    private static final String SHOW_PAST_HEARINGS_PAGE = "showConfirmPastHearingDatesPage";
    public static final String TO_RE_LIST_HEARING_LABEL = "toReListHearingsLabel";
    public static final String DEFAULT_PRE_ATTENDANCE = "1 hour before the hearing";
    private static final String HEARING_DURATION = "hearingDuration";
    private static final String HEARING_DAYS = "hearingDays";
    private static final String HEARING_MINUTES = "hearingMinutes";
    private static final String HEARING_HOURS = "hearingHours";
    private static final String HEARING_END_DATE_TIME = "hearingEndDateTime";
    public static final String PRE_HEARING_ATTENDANCE_DETAILS_KEY = "preHearingAttendanceDetails";
    public static final String PREVIOUS_HEARING_VENUE_KEY = "previousHearingVenue";
    public static final String HEARING_ATTENDANCE = "hearingAttendance";
    public static final String HEARING_ATTENDANCE_DETAILS = "hearingAttendanceDetails";
    public static final String HEARING_TYPE = "hearingType";
    public static final String HEARING_TYPE_DETAILS = "hearingTypeDetails";
    public static final String HEARING_TYPE_REASON = "hearingTypeReason";
    public static final String HEARING_VENUE = "hearingVenue";
    public static final String HEARING_VENUE_CUSTOM = "hearingVenueCustom";
    public static final String JUDGE_AND_LEGAL_ADVISOR = "judgeAndLegalAdvisor";
    public static final String SEND_NOTICE_OF_HEARING_TRANSLATION_REQUIREMENTS
        = "sendNoticeOfHearingTranslationRequirements";

    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final OthersService othersService;
    private final OthersNotifiedGenerator othersNotifiedGenerator;
    private final ObjectMapper mapper;
    private final IdentityService identityService;
    private final Time time;

    public Map<String, Object> populateHearingLists(CaseData caseData) {

        List<Element<HearingBooking>> pastAndTodayHearings = caseData.getPastAndTodayHearings();
        List<Element<HearingBooking>> pastHearings = caseData.getPastHearings();
        List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();
        List<Element<HearingBooking>> toBeReListedHearings = caseData.getToBeReListedHearings();
        List<Element<HearingBooking>> nonCancelledHearings = caseData.getAllNonCancelledHearings();
        List<Element<HearingBooking>> sortedNonCancelledHearings = nonCancelledHearings
            .stream().sorted(Comparator.comparing(hearingBooking -> hearingBooking.getValue().getStartDate()))
            .collect(toList());

        Collections.reverse(sortedNonCancelledHearings);

        Map<String, Object> listAndLabel = new HashMap<>(Map.of(
            PAST_HEARING_DATE_LIST, asDynamicList(pastHearings),
            FUTURE_HEARING_LIST, asDynamicList(futureHearings),
            PAST_AND_TODAY_HEARING_DATE_LIST, asDynamicList(pastAndTodayHearings),
            VACATE_HEARING_LIST, asDynamicList(sortedNonCancelledHearings),
            TO_RE_LIST_HEARING_LIST, asDynamicList(toBeReListedHearings)
        ));

        if (isNotEmpty(caseData.getHearingDetails()) || isNotEmpty(caseData.getToBeReListedHearings())) {
            listAndLabel.put(HAS_EXISTING_HEARINGS_FLAG, YES.getValue());
        }

        if (isNotEmpty(pastAndTodayHearings)) {
            listAndLabel.put(HAS_HEARINGS_TO_ADJOURN, YES.getValue());
        }

        if (isNotEmpty(sortedNonCancelledHearings)) {
            listAndLabel.put(HAS_HEARINGS_TO_VACATE, YES.getValue());
        }

        if (isNotEmpty(pastHearings)) {
            listAndLabel.put(HAS_PAST_HEARINGS, YES.getValue());
        }

        if (isNotEmpty(futureHearings)) {
            listAndLabel.put(HAS_FUTURE_HEARINGS, YES.getValue());
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

        return reListedBooking.getId();
    }

    public Map<String, Object> clearPopulatedHearingFields() {
        Map<String, Object> data = new HashMap<>();
        data.put(HEARING_TYPE_DETAILS, null);
        data.put(HEARING_TYPE, null);
        data.put(HEARING_TYPE_REASON, null);
        data.put(HEARING_START_DATE, null);
        data.put(HEARING_END_DATE, null);
        data.put(JUDGE_AND_LEGAL_ADVISOR, null);
        data.put(HEARING_ATTENDANCE, List.of());
        data.put(HEARING_ATTENDANCE_DETAILS, null);
        data.put(PRE_HEARING_ATTENDANCE_DETAILS_KEY, null);
        data.put(SEND_NOTICE_OF_HEARING_TRANSLATION_REQUIREMENTS, null);
        data.put(HEARING_DURATION, null);
        data.put(HEARING_DAYS, null);
        data.put(HEARING_HOURS, null);
        data.put(HEARING_MINUTES, null);
        data.put(HEARING_END_DATE_TIME, null);
        data.put(PREVIOUS_HEARING_VENUE_KEY, null);
        data.put(HEARING_VENUE, null);
        data.put(HEARING_VENUE_CUSTOM, null);
        return data;
    }

    public Map<String, Object> initiateNewHearing(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (isEmpty(caseData.getHearingDetails())) {
            return data;
        }

        HearingVenue previousHearingVenue = getPreviousHearingVenue(caseData);

        Address customAddress = "OTHER".equals(previousHearingVenue.getHearingVenueId())
            ? previousHearingVenue.getAddress() : null;

        data.put(PREVIOUS_HEARING_VENUE_KEY,
            PreviousHearingVenue.builder()
                .previousVenue(hearingVenueLookUpService.buildHearingVenue(previousHearingVenue))
                .newVenueCustomAddress(customAddress)
                .build());

        data.put(PRE_HEARING_ATTENDANCE_DETAILS_KEY, DEFAULT_PRE_ATTENDANCE);

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

        if (Objects.nonNull(hearingBooking.getTypeDetails())) {
            caseFields.put(HEARING_TYPE_DETAILS, hearingBooking.getTypeDetails());
        }

        caseFields.put(HEARING_TYPE, hearingBooking.getType());
        caseFields.put(HEARING_TYPE_REASON, hearingBooking.getTypeReason());
        caseFields.put(HEARING_START_DATE, hearingBooking.getStartDate());
        caseFields.put(HEARING_END_DATE, hearingBooking.getEndDate());
        caseFields.put(JUDGE_AND_LEGAL_ADVISOR, judgeAndLegalAdvisor);
        caseFields.put(HEARING_ATTENDANCE, hearingBooking.getAttendance());
        caseFields.put(HEARING_ATTENDANCE_DETAILS, hearingBooking.getAttendanceDetails());
        caseFields.put(PRE_HEARING_ATTENDANCE_DETAILS_KEY, hearingBooking.getPreAttendanceDetails());
        caseFields.put(SEND_NOTICE_OF_HEARING_TRANSLATION_REQUIREMENTS, hearingBooking.getTranslationRequirements());

        if (YES.getValue().equals(hearingBooking.getEndDateDerived())) {
            if (hearingBooking.getHearingDays() != null) {
                caseFields.put(HEARING_DURATION, DAYS.getType());
                caseFields.put(HEARING_DAYS, hearingBooking.getHearingDays());
            } else if (hearingBooking.getHearingHours() != null || hearingBooking.getHearingMinutes() != null) {
                caseFields.put(HEARING_DURATION, HOURS_MINS.getType());
                caseFields.put(HEARING_HOURS, hearingBooking.getHearingHours());
                caseFields.put(HEARING_MINUTES, hearingBooking.getHearingMinutes());
            }
        } else if (hearingBooking.getEndDate() != null) {
            caseFields.put(HEARING_DURATION, HearingDuration.DATE_TIME.getType());
            caseFields.put(HEARING_END_DATE_TIME, hearingBooking.getEndDate());
        }

        if (hearingBooking.getPreviousHearingVenue() != null
            && hearingBooking.getPreviousHearingVenue().getPreviousVenue() != null) {
            caseFields.put(PREVIOUS_HEARING_VENUE_KEY, hearingBooking.getPreviousHearingVenue());
        }

        caseFields.put(HEARING_VENUE, hearingBooking.getVenue());
        caseFields.put(HEARING_VENUE_CUSTOM, hearingBooking.getVenueCustomAddress());

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

    public void buildNoticeOfHearingIfYes(CaseData caseData, HearingBooking hearingBooking) {
        if (YES.getValue().equals(caseData.getSendNoticeOfHearing())) {
            buildNoticeOfHearing(caseData, hearingBooking);
        }
    }

    public void buildNoticeOfHearing(CaseData caseData, HearingBooking hearingBooking) {
        DocmosisNoticeOfHearing notice = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(notice,
            NOTICE_OF_HEARING);
        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));

        hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));

        Optional.ofNullable(caseData.getSendNoticeOfHearingTranslationRequirements()).ifPresent(
            hearingBooking::setTranslationRequirements
        );
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

        caseData.setHearingDetails(caseData.getHearingDetails().stream()
            .sorted((hearing1, hearing2) ->
                hearing2.getValue().getStartDate().compareTo(hearing1.getValue().getStartDate()))
            .collect(toList())
        );
    }

    public Object getHearingsDynamicList(CaseData caseData) {
        switch (caseData.getHearingOption()) {
            case VACATE_HEARING:
                return caseData.getVacateHearingDateList();
            case ADJOURN_HEARING:
                return caseData.getPastAndTodayHearingDateList();
            case RE_LIST_HEARING:
                return caseData.getToReListHearingDateList();
            case EDIT_PAST_HEARING:
                return caseData.getPastHearingDateList();
            case EDIT_FUTURE_HEARING:
                return caseData.getFutureHearingDateList();
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
            HEARING_TYPE,
            HEARING_TYPE_DETAILS,
            HEARING_TYPE_REASON,
            HEARING_VENUE,
            HEARING_VENUE_CUSTOM,
            HEARING_START_DATE,
            HEARING_END_DATE,
            "sendNoticeOfHearing",
            SEND_NOTICE_OF_HEARING_TRANSLATION_REQUIREMENTS,
            JUDGE_AND_LEGAL_ADVISOR,
            "noticeOfHearingNotes",
            PREVIOUS_HEARING_VENUE_KEY,
            "firstHearingFlag",
            "hasPreviousHearingVenue",
            "adjournmentReason",
            "vacatedReason",
            PAST_HEARING_DATE_LIST,
            FUTURE_HEARING_LIST,
            PAST_AND_TODAY_HEARING_DATE_LIST,
            VACATE_HEARING_LIST,
            "vacatedHearingDate",
            HAS_HEARINGS_TO_ADJOURN,
            HAS_PAST_HEARINGS,
            HAS_FUTURE_HEARINGS,
            HAS_EXISTING_HEARINGS_FLAG,
            "hearingReListOption",
            HEARING_START_DATE_LABEL,
            SHOW_PAST_HEARINGS_PAGE,
            HEARING_DURATION_LABEL,
            "confirmHearingDate",
            "hearingStartDateConfirmation",
            "hearingEndDateConfirmation",
            START_DATE_FLAG,
            END_DATE_FLAG,
            "hasSession",
            HEARING_ATTENDANCE,
            HEARING_ATTENDANCE_DETAILS,
            PRE_HEARING_ATTENDANCE_DETAILS_KEY,
            "hearingOption",
            "hasOthers",
            "sendOrderToAllOthers",
            "othersSelector",
            "others_label",
            HEARING_DAYS,
            HEARING_MINUTES,
            HEARING_HOURS,
            HEARING_DURATION,
            HEARING_END_DATE_TIME,
            "judicialUser",
            "enterManually",
            "judicialUserHearingJudge",
            "enterManuallyHearingJudge",
            "hearingJudge",
            "allocatedJudgeLabel",
            "useAllocatedJudge",
            "hearingHousekeepOption",
            "hearingHousekeepReason",
            "hearingHousekeepReasonOther"
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

    public Map<String, Object> populateFieldsWhenPastHearingDateAdded(CaseData caseData) {

        Map<String, Object> data = new HashMap<>();
        LocalDateTime currentDateTime = LocalDateTime.now();

        data.put(SHOW_PAST_HEARINGS_PAGE, NO.getValue());

        if (caseData.getHearingStartDate().isBefore(currentDateTime)) {
            data.put(HEARING_START_DATE_LABEL,
                formatLocalDateTimeBaseUsingFormat(caseData.getHearingStartDate(), DateFormatterHelper.DATE_TIME));
            data.put(START_DATE_FLAG, YES.getValue());
            data.put(SHOW_PAST_HEARINGS_PAGE, YES.getValue());
        }

        BiConsumer<LocalDateTime, String> populateFields = (endDateTime, endDateLabel) -> {
            data.put(HEARING_END_DATE, endDateTime);
            if (endDateTime.isBefore(currentDateTime)) {
                data.put(HEARING_DURATION_LABEL, endDateLabel);
                data.put(END_DATE_FLAG, YES.getValue());
                data.put(SHOW_PAST_HEARINGS_PAGE, YES.getValue());
            }
        };

        if (HearingDuration.DATE_TIME.getType().equals(caseData.getHearingDuration())) {
            populateFields.accept(caseData.getHearingEndDateTime(),
                formatLocalDateTimeBaseUsingFormat(caseData.getHearingEndDateTime(), DateFormatterHelper.DATE_TIME));
        } else if (DAYS.getType().equals(caseData.getHearingDuration())) {
            LocalDateTime endDateTime = getEndDate(caseData.getHearingStartDate(), caseData.getHearingDays());
            populateFields.accept(endDateTime, getHearingDays(caseData.getHearingDays()));
        } else if (HOURS_MINS.getType().equals(caseData.getHearingDuration())) {
            LocalDateTime startDate = caseData.getHearingStartDate();
            LocalDateTime endDateTime = startDate.plusHours(caseData.getHearingHours())
                .plusMinutes(caseData.getHearingMinutes());
            populateFields.accept(endDateTime,
                getHearingHoursAndMins(caseData.getHearingHours(), caseData.getHearingMinutes()));
        } else {
            throw new IllegalArgumentException("Invalid hearing duration " + caseData.getHearingDuration());
        }

        return data;
    }

    private String getHearingDays(Integer days) {
        return String.join(" ", String.valueOf(days), "days");
    }

    private String getHearingHoursAndMins(Integer hours, Integer minutes) {
        return String.join(" ", String.valueOf(hours), "hours", String.valueOf(minutes), "minutes");
    }

    private LocalDateTime getEndDate(LocalDateTime startDate, Integer hearingDays) {

        return HearingBooking.builder()
            .startDate(startDate)
            .hearingDays(hearingDays)
            .build().getEndDate();
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
        cancelHearing(caseData, hearingId, hearingStatus);
        Element<HearingBooking> reListedBooking = reList(caseData, hearingToBeReListed);
        return reListedBooking.getId();
    }

    private String getHearingInfo(Integer days, Integer hours, Integer minutes) {
        String hearingDuration = null;
        if (days != null) {
            hearingDuration = getHearingDays(days);
        } else if (hours != null && minutes != null) {
            hearingDuration = getHearingHoursAndMins(hours, minutes);
        }
        return hearingDuration;
    }

    private HearingBooking buildFirstHearing(CaseData caseData) {

        String endDateDerived = NO.getValue();
        String hearingDuration = getHearingInfo(caseData.getHearingDays(),
            caseData.getHearingHours(), caseData.getHearingMinutes());

        if (hearingDuration != null) {
            endDateDerived = YES.getValue();
        }

        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .typeDetails(caseData.getHearingTypeDetails())
            .typeReason(caseData.getHearingTypeReason())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .attendance(caseData.getHearingAttendance())
            .attendanceDetails(caseData.getHearingAttendanceDetails())
            .preAttendanceDetails(caseData.getPreHearingAttendanceDetails())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .hearingDays(caseData.getHearingDays())
            .hearingHours(caseData.getHearingHours())
            .hearingMinutes(caseData.getHearingMinutes())
            .endDateDerived(endDateDerived)
            .hearingDuration(hearingDuration)
            .allocatedJudgeLabel(caseData.getAllocatedJudge() != null
                ? formatJudgeTitleAndName(caseData.getAllocatedJudge().toJudgeAndLegalAdvisor()) : null)
            .hearingJudgeLabel(getHearingJudge(caseData.getJudgeAndLegalAdvisor()))
            .legalAdvisorLabel(getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .others(othersService.getSelectedOthers(caseData))
            .othersNotified(othersNotifiedGenerator.getOthersNotified(othersService.getSelectedOthers(caseData)))
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

        String endDateDerived = NO.getValue();
        String hearingDuration = getHearingInfo(caseData.getHearingDays(),
            caseData.getHearingHours(), caseData.getHearingMinutes());

        if (hearingDuration != null) {
            endDateDerived = YES.getValue();
        }

        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .typeDetails(caseData.getHearingTypeDetails())
            .typeReason(caseData.getHearingTypeReason())
            .venue(venue)
            .venueCustomAddress(caseData.getPreviousHearingVenue().getNewVenueCustomAddress())
            .customPreviousVenue(customPreviousVenue)
            .attendance(caseData.getHearingAttendance())
            .attendanceDetails(caseData.getHearingAttendanceDetails())
            .preAttendanceDetails(caseData.getPreHearingAttendanceDetails())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .hearingDays(caseData.getHearingDays())
            .hearingHours(caseData.getHearingHours())
            .hearingMinutes(caseData.getHearingMinutes())
            .hearingDuration(hearingDuration)
            .endDateDerived(endDateDerived)
            .allocatedJudgeLabel(caseData.getAllocatedJudge() != null
                ? formatJudgeTitleAndName(caseData.getAllocatedJudge().toJudgeAndLegalAdvisor()) : null)
            .hearingJudgeLabel(getHearingJudge(caseData.getJudgeAndLegalAdvisor()))
            .legalAdvisorLabel(getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .others(othersService.getSelectedOthers(caseData))
            .othersNotified(othersNotifiedGenerator.getOthersNotified(othersService.getSelectedOthers(caseData)))
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
        ManageHearingHousekeepEventData housekeepEventData = caseData.getManageHearingHousekeepEventData();

        Element<HearingBooking> originalHearingBooking = findElement(hearingId, caseData.getHearingDetails())
            .orElseThrow(() -> new NoHearingBookingException(hearingId));

        HearingBooking.HearingBookingBuilder cancelledHearingBuilder = originalHearingBooking.getValue()
            .toBuilder()
            .status(hearingStatus)
            .vacatedDate(caseData.getVacatedHearingDate());

        if (YES.equals(housekeepEventData.getHearingHousekeepOptions())) {
            HearingHousekeepReason housekeepReason = housekeepEventData.getHearingHousekeepReason();
            cancelledHearingBuilder = cancelledHearingBuilder
                .housekeepReason((HearingHousekeepReason.OTHER.equals(housekeepReason))
                    ? housekeepEventData.getHearingHousekeepReasonOther()
                    : housekeepReason.getLabel());
        } else {
            cancelledHearingBuilder = cancelledHearingBuilder
                .cancellationReason(getCancellationReason(caseData, hearingStatus));
        }

        Element<HearingBooking> cancelledHearing = element(hearingId, cancelledHearingBuilder.build());

        caseData.addCancelledHearingBooking(cancelledHearing);
        caseData.removeHearingDetails(originalHearingBooking);

        if (isNotEmpty(cancelledHearing.getValue().getCaseManagementOrderId())) {
            updateHearingOrderBundles(caseData, cancelledHearing);
            updateDraftUploadedCaseManagementOrders(caseData, cancelledHearing);
        }

        if (!YES.equals(housekeepEventData.getHearingHousekeepOptions())) {
            buildNoticeOfHearingVacated(caseData, cancelledHearing.getValue());
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

    private String hearingLabels(List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .map(Element::getValue)
            .map(HearingBooking::toLabel)
            .collect(Collectors.joining("\n"));
    }

    public void buildNoticeOfHearingVacated(CaseData caseData, HearingBooking hearingBooking) {
        DocmosisNoticeOfHearingVacated notice =
            noticeOfHearingGenerationService.getHearingVacatedTemplateData(caseData, hearingBooking,
                caseData.getHearingReListOption() == RE_LIST_NOW);
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(notice,
            NOTICE_OF_HEARING_VACATED);
        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            NOTICE_OF_HEARING_VACATED.getDocumentTitle(time.now().toLocalDate()));

        hearingBooking.setNoticeOfHearingVacated(DocumentReference.buildFromDocument(document));
    }
}
