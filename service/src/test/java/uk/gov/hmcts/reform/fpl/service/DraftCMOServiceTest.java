package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.PARTIES_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, JsonOrdersLookupService.class,
    HearingVenueLookUpService.class})
class DraftCMOServiceTest {
    private final LocalDateTime date = LocalDateTime.now();
    private final String localAuthorityCode = "example";
    private final String courtName = "Example Court";
    private final String courtEmail = "example@court.com";
    private final String config = String.format("%s=>%s:%s", localAuthorityCode, courtName, courtEmail);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OrdersLookupService ordersLookupService;

    @Autowired
    private HearingVenueLookUpService hearingVenueLookUpService;

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HearingBookingService hearingBookingService = new HearingBookingService();
    private DirectionHelperService directionHelperService = new DirectionHelperService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(config);
    private CommonCaseDataExtractionService commonCaseDataExtraction = new CommonCaseDataExtractionService(
        dateFormatterService, hearingVenueLookUpService);
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration =
        new LocalAuthorityEmailLookupConfiguration(config);
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration =
        new LocalAuthorityNameLookupConfiguration(config);

    private DraftCMOService draftCMOService;

    @BeforeEach
    void setUp() {
        CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
            hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService, directionHelperService,
            hearingVenueLookUpService, commonCaseDataExtraction);
        this.draftCMOService = new DraftCMOService(mapper, dateFormatterService, directionHelperService,
            caseDataExtractionService, commonCaseDataExtraction, hmctsCourtLookupConfiguration,
            localAuthorityEmailLookupConfiguration, localAuthorityNameLookupConfiguration);
        hearingDetails = createHearingBookings(date);
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        hearingDetails = createHearingBookings(date);
        caseManagementOrder = CaseManagementOrder.builder().build();

        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = mapper.convertValue(data.get("cmoHearingDateList"), DynamicList.class);

        assertThat(hearingList.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(2))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(0))
                    .build()));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCmoHasPreviousSelectedValue() {
        hearingDetails = createHearingBookings(date);
        caseManagementOrder = CaseManagementOrder.builder()
            .hearingDate(formatLocalDateToMediumStyle(2))
            .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
            .build();

        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = mapper.convertValue(data.get("cmoHearingDateList"), DynamicList.class);

        assertThat(hearingList.getValue())
            .isEqualTo(DynamicListElement.builder()
                .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .label(formatLocalDateToMediumStyle(2))
                .build());
    }

    @Test
    void shouldReturnCaseManagementOrderWhenProvidedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(DirectionAssignee.values()).forEach(direction ->
            caseData.put(direction.getValue() + "Custom", createElementCollection(createUnassignedDirection()))
        );

        caseData.put("cmoHearingDateList", getDynamicList());
        caseData.put("reviewCaseManagementOrder", ImmutableMap.of());

        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseData);

        assertThat(caseManagementOrder).isNotNull()
            .extracting("id", "hearingDate").containsExactly(
            fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"),
            formatLocalDateToMediumStyle(5));

        assertThat(caseManagementOrder.getDirections()).isEqualTo(createCmoDirections());
    }

    @Test
    void shouldFormatRespondentsIntoKeyWhenRespondentsArePresent() {
        String respondentsKey = draftCMOService.createRespondentAssigneeDropdownKey(createRespondents());

        assertThat(respondentsKey).contains(
            "Respondent 1 - Timothy Jones",
            "Respondent 2 - Sarah Simpson");
    }

    @Test
    void shouldFormatOthersIntoKeyWhenOthersArePresent() {
        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(createOthers());

        assertThat(othersKey).contains(
            "Person 1 - Kyle Stafford",
            "Other Person 1 - Sarah Simpson");
    }

    @Test
    void shouldIncludeEmptyStatePlaceholderWhenAnOtherDoesNotIncludeFullName() {
        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(createFirstOtherWithoutAName());

        assertThat(othersKey).contains(
            "Person 1 - " + EMPTY_PLACEHOLDER,
            "Other Person 1 - Peter Smith");
    }

    @Test
    void shouldReturnEmptyStringIfOthersDoesNotExist() {
        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(Others.builder().build());
        assertThat(othersKey).isEqualTo("");
    }

    @Test
    void shouldReturnAMapWithAllIndividualCMOEntriesPopulated() {
        caseManagementOrder = CaseManagementOrder.builder()
            .hearingDate(formatLocalDateToMediumStyle(2))
            .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
            .recitals(List.of(Element.<Recital>builder()
                .value(Recital.builder().build())
                .build()))
            .schedule(Schedule.builder().build())
            .cmoStatus(SELF_REVIEW)
            .build();

        hearingDetails = createHearingBookings(date);

        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseManagementOrder, hearingDetails);

        assertThat(data).containsKeys("cmoHearingDateList", "schedule", "recitals", "reviewCaseManagementOrder");
    }

    @Test
    void shouldReturnAMapWithEmptyRepopulatedEntriesWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            null, hearingDetails);

        assertThat(data.get("schedule")).isNull();
        assertThat(data.get("recitals")).isNull();
        assertThat(data.get("reviewCaseManagementOrder")).extracting("cmoStatus").isNull();
    }


    private DynamicList getDynamicList() {
        DynamicList dynamicList = draftCMOService.buildDynamicListFromHearingDetails(createHearingBookings(date));

        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatLocalDateToMediumStyle(5))
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .build();

        dynamicList.setValue(listElement);
        return dynamicList;
    }

    @Test
    void shouldMoveDirectionsToCaseDetailsWhenCMOExistsWithDirections() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put("caseManagementOrder", CaseManagementOrder.builder()
            .directions(createCmoDirections())
            .build());

        draftCMOService.prepareCustomDirections(caseData);

        assertThat(caseData).containsKeys("allParties",
            "localAuthorityDirections",
            "cafcassDirections",
            "courtDirections",
            "otherPartiesDirections",
            "respondentDirections");
    }

    @Test
    void shouldRemoveCustomDirectionsWhenCMODoesNotExistOnCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(DirectionAssignee.values()).forEach(direction ->
            caseData.put(direction.getValue() + "Custom", createElementCollection(createUnassignedDirection()))
        );

        draftCMOService.prepareCustomDirections(caseData);

        assertThat(caseData).doesNotContainKeys("allPartiesCustom",
            "localAuthorityDirectionsCustom",
            "cafcassDirectionsCustom",
            "courtDirectionsCustom",
            "otherPartiesDirections",
            "respondentDirections");
    }

    private List<Element<HearingBooking>> createHearingBookings(LocalDateTime now) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .value(createHearingBooking(now.plusDays(5), now.plusDays(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .value(createHearingBooking(now.plusDays(2), now.plusDays(3)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                .value(createHearingBooking(now, now.plusDays(1)))
                .build());
    }

    private String formatLocalDateToMediumStyle(int i) {
        return dateFormatterService.formatLocalDateToString(date.plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
    }

    private Others createFirstOtherWithoutAName() {
        return Others.builder()
            .firstOther(Other.builder()
                .DOB("02/05/1988")
                .build())
            .additionalOthers(ImmutableList.of(
                Element.<Other>builder()
                    .value(Other.builder()
                        .name("Peter Smith")
                        .DOB("02/05/1988")
                        .build())
                    .build()
            )).build();
    }

    @Nested
    class PrepareCaseDetailsTest {
        private final String[] keys = {
            "cmoHearingDateList",
            "recitals",
            "schedule",
            "reviewCaseManagementOrder"};

        private HashMap<String, Object> data; // Tries to use an ImmutableMap unless specified

        @BeforeEach
        void setUp() {
            data = new HashMap<>();
        }

        @Test
        void shouldRemoveAllRelatedEntriesInCaseDetailsThatAreNotCMOObjectsWhenCMOStatusIsPartyReview() {
            final CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
                .cmoStatus(PARTIES_REVIEW).build();

            Arrays.stream(keys).forEach(key -> data.put(key, ""));

            draftCMOService.prepareCaseDetails(data, caseManagementOrder);

            assertThat(data).doesNotContainKeys(keys);
            assertThat(data).containsKeys("sharedDraftCMO", "caseManagementOrder");
        }

        @Test
        void shouldRemoveAllRelatedEntriesInCaseDetailsApartFromCaseManagementOrderWhenCMOStatusIsSelfReview() {
            final CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
                .cmoStatus(SELF_REVIEW).build();

            data.put("sharedDraftCMO", caseManagementOrder);

            draftCMOService.prepareCaseDetails(data, caseManagementOrder);
            assertThat(data).doesNotContainKeys(add(keys, "sharedDraftCMO"));
            assertThat(data).containsKey("caseManagementOrder");
        }

    }
}
