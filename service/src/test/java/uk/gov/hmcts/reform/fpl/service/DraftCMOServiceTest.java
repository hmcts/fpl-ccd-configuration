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
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
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

import java.io.IOException;
import java.time.LocalDate;
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
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCMO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, HearingVenueLookUpService.class,
    DocmosisDocumentGeneratorService.class, RestTemplate.class, DocmosisConfiguration.class,
    CommonCaseDataExtractionService.class, DateFormatterService.class, HearingBookingService.class,
    DirectionHelperService.class
})
class DraftCMOServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private final ObjectMapper mapper;
    private final OrdersLookupService ordersLookupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService commonCaseDataExtraction;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;
    private final DirectionHelperService directionHelperService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(
        String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL_ADDRESS));
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration =
        new LocalAuthorityEmailLookupConfiguration(String.format("%s=>%s",
            LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_EMAIL_ADDRESS));
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration =
        new LocalAuthorityNameLookupConfiguration(String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_NAME));

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;
    private DraftCMOService draftCMOService;

    @Autowired
    public DraftCMOServiceTest(ObjectMapper mapper, OrdersLookupService ordersLookupService,
                               HearingVenueLookUpService hearingVenueLookUpService,
                               CommonCaseDataExtractionService commonCaseDataExtraction,
                               DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                               DateFormatterService dateFormatterService,
                               HearingBookingService hearingBookingService,
                               DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.ordersLookupService = ordersLookupService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
        this.commonCaseDataExtraction = commonCaseDataExtraction;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.directionHelperService = directionHelperService;
    }

    @BeforeEach
    void setUp() {
        CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
            hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService, directionHelperService,
            hearingVenueLookUpService, commonCaseDataExtraction, docmosisDocumentGeneratorService);

        draftCMOService = new DraftCMOService(mapper, dateFormatterService, directionHelperService,
            caseDataExtractionService, commonCaseDataExtraction, hmctsCourtLookupConfiguration,
            localAuthorityEmailLookupConfiguration, localAuthorityNameLookupConfiguration, ordersLookupService,
            docmosisDocumentGeneratorService);

        hearingDetails = createHearingBookings(NOW);
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        hearingDetails = createHearingBookings(NOW);
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
        hearingDetails = createHearingBookings(NOW);
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

        hearingDetails = createHearingBookings(NOW);

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

    private DynamicList getDynamicList() {
        DynamicList dynamicList = draftCMOService.buildDynamicListFromHearingDetails(createHearingBookings(NOW));

        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatLocalDateToMediumStyle(5))
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .build();

        dynamicList.setValue(listElement);
        return dynamicList;
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

    @Nested
    class GenerateTemplateData {
        private final LocalDateTime NOW = LocalDateTime.now();

        @Test
        void shouldReturnEmptyMapValuesWhenCaseDataIsEmpty() throws IOException {
            final Map<String, Object> templateData = draftCMOService.generateCMOTemplateData(ImmutableMap.of());

            assertThat(templateData.get("courtName")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("familyManCaseNumber")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
                .formatLocalDateToString(DraftCMOServiceTest.NOW.toLocalDate(), FormatStyle.LONG));
            assertThat(templateData.get("complianceDeadline")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("children")).isEqualTo(ImmutableList.of());
            assertThat(templateData.get("numberOfChildren")).isEqualTo(0);
            assertThat(templateData.get("applicantName")).isEqualTo("");
            assertThat(templateData.get("respondents")).isEqualTo(ImmutableList.of());
            assertThat(templateData.get("respondentsProvided")).isEqualTo(false);
            assertThat(templateData.get("localAuthoritySolicitorEmail")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("localAuthorityName")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("localAuthoritySolicitorName")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("localAuthoritySolicitorPhoneNumber")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("respondentOneName")).asString().isBlank();
            assertThat(templateData.get("hearingDate")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("hearingVenue")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("preHearingAttendance")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("hearingTime")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("judgeTitleAndName")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("legalAdvisorName")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("allParties")).isNull();
            assertThat(templateData.get("localAuthorityDirections")).isNull();
            assertThat(templateData.get("respondentDirections")).isNull();
            assertThat(templateData.get("cafcassDirections")).isNull();
            assertThat(templateData.get("otherPartiesDirections")).isNull();
            assertThat(templateData.get("courtDirections")).isNull();
            assertThat(templateData.get("recitals")).isEqualTo(ImmutableList.of());
            assertThat(templateData.get("recitalsProvided")).isEqualTo(false);
            assertThat(templateData.get("schedule")).isEqualTo(Schedule.builder().build());
            assertThat(templateData.get("scheduleProvided")).isEqualTo(false);
            assertThat(templateData.get("draftbackground")).isNotNull();
            assertThat(templateData.get("caseManagementNumber")).isEqualTo(1);
        }

        @Test
        void shouldReturnFullyPopulatedMapWhenCompleteCaseDetailsAreProvided() throws IOException {
            final Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
                .put("caseLocalAuthority", "example")
                .put("familyManCaseNumber", "123")
                .put("children1", createPopulatedChildren())
                .put("hearingDetails", createHearingBookings(NOW))
                .put("dateSubmitted", LocalDate.now())
                .put("respondents1", createRespondents())
                .put("cmoHearingDateList", DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                        .label(formatLocalDateToMediumStyle(5))
                        .build())
                    .build())
                .put("caseManagementOrder", createCMO(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"), true))
                .build();

            final Map<String, Object> templateData = draftCMOService.generateCMOTemplateData(caseData);

            assertThat(templateData.get("courtName")).isEqualTo(COURT_NAME);
            assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
            assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
                .formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG));
            assertThat(templateData.get("complianceDeadline")).isEqualTo(dateFormatterService
                .formatLocalDateToString(NOW.toLocalDate().plusWeeks(26), FormatStyle.LONG));
            assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
            assertThat(templateData.get("numberOfChildren")).isEqualTo(getExpectedChildren().size());
            assertThat(templateData.get("applicantName")).isEqualTo("");
            assertThat(templateData.get("respondents")).isEqualTo(getExpectedRespondents());
            assertThat(templateData.get("respondentsProvided")).isEqualTo(true);
            assertThat(templateData.get("localAuthoritySolicitorEmail"))
                .isEqualTo(LOCAL_AUTHORITY_EMAIL_ADDRESS);
            assertThat(templateData.get("localAuthorityName")).isEqualTo(LOCAL_AUTHORITY_NAME);
            assertThat(templateData.get("localAuthoritySolicitorName")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("localAuthoritySolicitorPhoneNumber")).isEqualTo(EMPTY_PLACEHOLDER);
            assertThat(templateData.get("respondentOneName")).isEqualTo("Timothy Jones");
            assertThat(templateData.get("hearingDate")).asString().isBlank();
            assertThat(templateData.get("hearingVenue"))
                .isEqualTo("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW");
            assertThat(templateData.get("preHearingAttendance")).isEqualTo(getExpectedPrehearingAttendance());
            assertThat(templateData.get("hearingTime")).isEqualTo(getExpectedHearingTime());
//            assertThat(templateData.get("judgeTitleAndName")).isEqualTo("Her Honour Judge Smith");
//            assertThat(templateData.get("legalAdvisorName")).isEqualTo("Bob Ross");
            assertThat(templateData.get("allParties")).isEqualTo(getExpectedDirection(2));
            assertThat(templateData.get("localAuthorityDirections")).isEqualTo(getExpectedDirection(3));
            assertThat(templateData.get("cafcassDirections")).isEqualTo(getExpectedDirection(4));
            assertThat(templateData.get("courtDirections")).isEqualTo(getExpectedDirection(5));
            assertThat(templateData.get("respondentDirections")).isEqualTo(getExpectedDirection(6));
            assertThat(templateData.get("otherPartiesDirections")).isEqualTo(getExpectedDirection(7));
            assertThat(templateData.get("recitals")).isEqualTo(getExpectedRecital());
            assertThat(templateData.get("recitalsProvided")).isEqualTo(true);
//            assertThat(templateData).containsAllEntriesOf(getExpectedSchedule());
            assertThat(templateData.get("scheduleProvided")).isEqualTo(true);
            assertThat(templateData.get("draftbackground")).isNotNull();
            assertThat(templateData.get("caseManagementNumber")).isEqualTo(1);
        }

        private String getExpectedHearingTime() {
            return String.format("%s - %s",
                dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW, "d MMMM, h:mma"),
                dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW.plusDays(1), "d MMMM, h:mma"));
        }

        private String getExpectedPrehearingAttendance() {
            return dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW.minusHours(1), "d MMMM yyyy, h:mma");
        }

        private Map<String, String> getExpectedSchedule() {
            return ImmutableMap.<String, String>builder()
                .put("allocation", "An allocation")
                .put("alternativeCarers", "Alternatives")
                .put("application", "An application")
                .put("childrensCurrentArrangement", "Current arrangement")
                .put("includeSchedule", "Yes")
                .put("keyIssues", "Key Issues")
                .put("partiesPositions", "Some positions")
                .put("threshold", "threshold")
                .put("timetableForChildren", "time goes by")
                .put("timetableForProceedings", "so slowly")
                .put("todaysHearing", "slowly")
                .build();
        }

        private List<Map<String, String>> getExpectedRecital() {
            return List.of(
                Map.of(
                    "title", "A title",
                    "body", "A description"
                )
            );
        }

        private List<Map<String, String>> getExpectedChildren() {
            return List.of(
                Map.of(
                    "name", "Bran Stark",
                    "gender", "Male",
                    "dateOfBirth", dateFormatterService.formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG)),
                Map.of(
                    "name", "Sansa Stark",
                    "gender", EMPTY_PLACEHOLDER,
                    "dateOfBirth", EMPTY_PLACEHOLDER),
                Map.of(
                    "name", "Jon Snow",
                    "gender", EMPTY_PLACEHOLDER,
                    "dateOfBirth", EMPTY_PLACEHOLDER)
            );
        }

        private List<Map<String, String>> getExpectedRespondents() {
            return List.of(
                Map.of(
                    "name", "Timothy Jones",
                    "relationshipToChild", "Father"
                ),
                Map.of(
                    "name", "Sarah Simpson",
                    "relationshipToChild", "Mother"
                )
            );
        }

        private List<Map<String, String>> getExpectedDirection(int index) {
            return List.of(
                Map.of(
                    "title", index + ". null by unknown",
                    "body", "Mock direction text"
                )
            );
        }
    }
}
