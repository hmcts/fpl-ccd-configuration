package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
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
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.values;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, CommonDirectionService.class, DraftCMOService.class
})
class DraftCMOServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();

    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    DraftCMOServiceTest(ObjectMapper mapper, DraftCMOService draftCMOService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
    }

    @BeforeEach
    void setUp() {
        hearingDetails = createHearingBookingsFromInitialDate(NOW);
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        hearingDetails = createHearingBookingsFromInitialDate(NOW.plusDays(5));
        caseManagementOrder = CaseManagementOrder.builder().build();

        Map<String, Object> data = draftCMOService.extractCaseManagementOrderVariables(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = (DynamicList) data.get("cmoHearingDateList");

        assertThat(hearingList.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(10))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(7))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build()));
    }

    @Test
    void shouldNotReturnHearingDatesWhenHearingDateIsInThePast() {
        hearingDetails = createHearingBookingsFromInitialDate(NOW.minusDays(10));
        caseManagementOrder = CaseManagementOrder.builder().build();

        Map<String, Object> data = draftCMOService.extractCaseManagementOrderVariables(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = (DynamicList) data.get(HEARING_DATE_LIST.getKey());

        assertThat(hearingList.getListItems()).isEmpty();
    }

    @Test
    void shouldReturnHearingDatesWhenHearingDateIsSameDayButLaterTime() {
        hearingDetails = createHearingBookingsFromInitialDate(NOW.plusMinutes(5));
        caseManagementOrder = CaseManagementOrder.builder().build();

        Map<String, Object> data = draftCMOService.extractCaseManagementOrderVariables(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = (DynamicList) data.get(HEARING_DATE_LIST.getKey());

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
        hearingDetails = createHearingBookingsFromInitialDate(NOW);
        caseManagementOrder = createCaseManagementOrder();

        Map<String, Object> data = draftCMOService.extractCaseManagementOrderVariables(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = mapper.convertValue(data.get(HEARING_DATE_LIST.getKey()), DynamicList.class);

        assertThat(hearingList.getValue())
            .isEqualTo(DynamicListElement.builder()
                .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .label(formatLocalDateToMediumStyle(2))
                .build());
    }

    @Test
    void shouldReturnCaseManagementOrderWhenProvidedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(values()).forEach(direction ->
            caseData.put(direction.toCustomDirectionField().concat("CMO"),
                createElementCollection(createUnassignedDirection()))
        );

        caseData.put(HEARING_DATE_LIST.getKey(), getDynamicList());

        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(
            mapper.convertValue(caseData, CaseData.class), null);

        assertThat(caseManagementOrder).isNotNull()
            .extracting("id", "hearingDate").containsExactly(
            fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"),
            formatLocalDateToMediumStyle(5));

        assertThat(caseManagementOrder.getDirections()).containsAll(createCmoDirections());
    }

    @Test
    void shouldReturnAMapWithAllIndividualCMOEntriesPopulated() {
        caseManagementOrder = createCaseManagementOrder();

        hearingDetails = createHearingBookingsFromInitialDate(NOW);

        Map<String, Object> data = draftCMOService.extractCaseManagementOrderVariables(
            caseManagementOrder, hearingDetails);

        assertThat(data).containsKeys(HEARING_DATE_LIST.getKey(), SCHEDULE.getKey(), RECITALS.getKey());
    }

    @Test
    void shouldReturnAMapWithEmptyRepopulatedEntriesWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = draftCMOService.extractCaseManagementOrderVariables(
            null, List.of());

        DynamicList emptyDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("").build())
            .listItems(List.of())
            .build();

        assertThat(data.get(HEARING_DATE_LIST.getKey())).isEqualTo(emptyDynamicList);
        assertThat(data.get(SCHEDULE.getKey())).isNull();
        assertThat(data.get(RECITALS.getKey())).isNull();
    }

    @Test
    void shouldMoveDirectionsToCaseDetailsWhenCMOExistsWithDirections() {
        Map<String, Object> caseData = new HashMap<>();

        draftCMOService.prepareCustomDirections(CaseDetails.builder().data(caseData).build(),
            CaseManagementOrder.builder()
                .directions(createCmoDirections())
                .build());

        assertThat(caseData).containsKeys("allParties", "localAuthorityDirections", "cafcassDirections",
            "courtDirections", "otherPartiesDirections", "respondentDirections");
    }

    @Test
    void shouldRemoveCustomDirectionsWhenCMODoesNotExistOnCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(values()).forEach(direction ->
            caseData.put(direction.toCustomDirectionField().concat("CMO"),
                createElementCollection(createUnassignedDirection()))
        );

        draftCMOService.prepareCustomDirections(CaseDetails.builder().data(caseData).build(), null);

        assertThat(caseData).doesNotContainKeys("allPartiesCustomCMO", "localAuthorityDirectionsCustomCMO",
            "cafcassDirectionsCustomCMO", "courtDirectionsCustomCMO", "otherPartiesDirections", "respondentDirections");
    }

    private DynamicList getDynamicList() {
        DynamicList dynamicList = draftCMOService.buildDynamicListFromHearingDetails(
            createHearingBookingsFromInitialDate(NOW));

        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatLocalDateToMediumStyle(5))
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .build();

        dynamicList.setValue(listElement);
        return dynamicList;
    }

    private CaseManagementOrder createCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .hearingDate(formatLocalDateToMediumStyle(2))
            .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
            .recitals(List.of(Element.<Recital>builder()
                .value(Recital.builder().build())
                .build()))
            .schedule(Schedule.builder().build())
            .status(SELF_REVIEW)
            .orderDoc(DocumentReference.builder().build())
            .build();
    }

    private String formatLocalDateToMediumStyle(int i) {
        return formatLocalDateToString(NOW.plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
    }

    @Nested
    class PrepareCaseDetailsTest {
        private final String[] keys = {
            HEARING_DATE_LIST.getKey(),
            RECITALS.getKey(),
            SCHEDULE.getKey()};

        private HashMap<String, Object> data; // Tries to use an ImmutableMap unless specified

        @Test
        void shouldRemoveScheduleAndRecitalsAndHearingDateListFromCaseData() {
            data = new HashMap<>();

            Arrays.stream(keys).forEach(key -> data.put(key, ""));

            draftCMOService.removeTransientObjectsFromCaseData(data);

            assertThat(data).doesNotContainKeys(keys);
        }
    }
}
