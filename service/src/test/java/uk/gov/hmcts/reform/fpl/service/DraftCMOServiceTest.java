package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
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
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.PARTIES_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.values;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, DateFormatterService.class, DirectionHelperService.class, DraftCMOService.class
})
class DraftCMOServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();

    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;
    private final DraftCMOService draftCMOService;

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;

    @Autowired
    public DraftCMOServiceTest(ObjectMapper mapper,
                               DateFormatterService dateFormatterService,
                               DraftCMOService draftCMOService) {
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
        this.draftCMOService = draftCMOService;
    }

    @BeforeEach
    void setUp() {
        hearingDetails = createHearingBookings(NOW);
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        hearingDetails = createHearingBookings(NOW);
        caseManagementOrder = CaseManagementOrder.builder().build();

        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseManagementOrder, hearingDetails);

        DynamicList hearingList = (DynamicList) data.get("cmoHearingDateList");

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
        hearingDetails = createHearingBookings(NOW);
        caseManagementOrder = createCaseManagementOrder();

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

        Stream.of(values()).forEach(direction ->
            caseData.put(direction.getValue() + "Custom", createElementCollection(createUnassignedDirection()))
        );

        caseData.put("cmoHearingDateList", getDynamicList());

        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(
            mapper.convertValue(caseData, CaseData.class));

        assertThat(caseManagementOrder).isNotNull()
            .extracting("id", "hearingDate").containsExactly(
            fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"),
            formatLocalDateToMediumStyle(5));

        assertThat(caseManagementOrder.getDirections()).containsAll(createCmoDirections());
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
            "Other person 1 - Sarah Simpson");
    }

    @Test
    void shouldIncludeEmptyStatePlaceholderWhenAnOtherDoesNotIncludeFullName() {
        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(createFirstOtherWithoutAName());

        assertThat(othersKey).contains(
            "Person 1 - " + EMPTY_PLACEHOLDER,
            "Other person 1 - Peter Smith");
    }

    @Test
    void shouldReturnEmptyStringIfOthersDoesNotExist() {
        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(Others.builder().build());
        assertThat(othersKey).isEqualTo("");
    }

    @Test
    void shouldReturnAMapWithAllIndividualCMOEntriesPopulated() {
        caseManagementOrder = createCaseManagementOrder();

        hearingDetails = createHearingBookings(NOW);

        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseManagementOrder, hearingDetails);

        assertThat(data).containsKeys("cmoHearingDateList", "schedule", "recitals");
    }

    @Test
    void shouldReturnAMapWithEmptyRepopulatedEntriesWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = draftCMOService.extractIndividualCaseManagementOrderObjects(
            null, List.of());

        DynamicList emptyDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("").build())
            .listItems(List.of())
            .build();

        assertThat(data.get("cmoHearingDateList")).isEqualTo(emptyDynamicList);
        assertThat(data.get("schedule")).isNull();
        assertThat(data.get("recitals")).isNull();
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

        Stream.of(values()).forEach(direction ->
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

    private DynamicList getDynamicList() {
        DynamicList dynamicList = draftCMOService.buildDynamicListFromHearingDetails(createHearingBookings(NOW));

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
            .cmoStatus(SELF_REVIEW)
            .orderDoc(DocumentReference.builder().build())
            .build();
    }

    private String formatLocalDateToMediumStyle(int i) {
        return dateFormatterService.formatLocalDateToString(NOW.plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
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
            "schedule"};

        private HashMap<String, Object> data; // Tries to use an ImmutableMap unless specified

        @Test
        void shouldRemoveScheduleAndRecitalsAndHearingDateListFromCaseData() {
            data = new HashMap<>();

            Arrays.stream(keys).forEach(key -> data.put(key, ""));

            draftCMOService.removeTransientObjectsFromCaseData(data);

            assertThat(data).doesNotContainKeys(keys);
        }

        @Test
        void shouldOnlyPopulateCaseManagementOrderWhenCMOStatusIsSelfReview() {
            data = new HashMap<>();

            caseManagementOrder = CaseManagementOrder.builder().cmoStatus(SELF_REVIEW).build();

            draftCMOService.populateCaseDataWithCMO(data, caseManagementOrder);

            assertThat(data.get("caseManagementOrder")).isEqualTo(caseManagementOrder);
        }

        @Test
        void shouldMakeSharedDraftCMODocumentNullWhenCMOStatusIsSelfReview() {
            data = new HashMap<>();

            caseManagementOrder = CaseManagementOrder.builder().cmoStatus(SELF_REVIEW).build();
            data.put("sharedDraftCMODocument", DocumentReference.builder().build());

            draftCMOService.populateCaseDataWithCMO(data, caseManagementOrder);

            assertThat(data.get("sharedDraftCMODocument")).isNull();
        }

        @Test
        void shouldPopulateSharedDraftCMODocumentWhenCMOStatusIsPartyReview() {
            data = new HashMap<>();

            DocumentReference documentReference = DocumentReference.builder().build();
            caseManagementOrder = CaseManagementOrder.builder()
                .cmoStatus(PARTIES_REVIEW)
                .orderDoc(documentReference)
                .build();

            draftCMOService.populateCaseDataWithCMO(data, caseManagementOrder);

            assertThat(data.get("sharedDraftCMODocument")).isEqualTo(documentReference);
        }
    }
}
