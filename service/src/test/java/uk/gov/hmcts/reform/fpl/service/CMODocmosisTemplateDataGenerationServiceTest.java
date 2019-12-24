package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_1;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.service.CommonCaseDataExtractionService.HEARING_EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildCaseDataMapForDraftCMODocmosisGeneration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, DraftCMOService.class, CommonCaseDataExtractionService.class,
    DateFormatterService.class, DirectionHelperService.class, HearingVenueLookUpService.class,
    HearingBookingService.class, JsonOrdersLookupService.class
})
class CMODocmosisTemplateDataGenerationServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";
    private static final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration =
        new HmctsCourtLookupConfiguration(
            String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL_ADDRESS));
    private static final String HEARING_VENUE = "Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW";
    private final DateFormatterService dateFormatterService;
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;
    private final DirectionHelperService directionHelperService;
    private final DraftCMOService draftCMOService;
    private final HearingBookingService hearingBookingService;
    private final ObjectMapper mapper;
    private final OrdersLookupService ordersLookupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final String[] scheduleKeys = {
        "includeSchedule", "allocation", "application", "todaysHearing", "childrensCurrentArrangement",
        "timetableForProceedings", "timetableForChildren", "alternativeCarers", "threshold", "keyIssues",
        "partiesPositions"
    };
    private CMODocmosisTemplateDataGenerationService templateDataGenerationService;

    @Autowired
    CMODocmosisTemplateDataGenerationServiceTest(DateFormatterService dateFormatterService,
                                                 CommonCaseDataExtractionService commonCaseDataExtractionService,
                                                 DirectionHelperService directionHelperService,
                                                 DraftCMOService draftCMOService,
                                                 HearingBookingService hearingBookingService,
                                                 ObjectMapper mapper,
                                                 OrdersLookupService ordersLookupService,
                                                 HearingVenueLookUpService hearingVenueLookUpService) {
        this.dateFormatterService = dateFormatterService;
        this.commonCaseDataExtractionService = commonCaseDataExtractionService;
        this.directionHelperService = directionHelperService;
        this.draftCMOService = draftCMOService;
        this.hearingBookingService = hearingBookingService;
        this.mapper = mapper;
        this.ordersLookupService = ordersLookupService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
    }

    @BeforeEach
    void setUp() {
        CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService(
            dateFormatterService, hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService,
            directionHelperService, hearingVenueLookUpService, commonCaseDataExtractionService);

        templateDataGenerationService = new CMODocmosisTemplateDataGenerationService(
            commonCaseDataExtractionService, caseDataExtractionService, dateFormatterService, directionHelperService,
            draftCMOService, hearingBookingService, hmctsCourtLookupConfiguration, mapper);
    }

    @Test
    void shouldReturnEmptyMapValuesWhenCaseDataIsEmpty() throws IOException {
        final Map<String, Object> templateData = templateDataGenerationService.getTemplateData(CaseData.builder()
            .build(), true);

        assertThat(templateData.get("courtName")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("children")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("numberOfChildren")).isEqualTo(0);
        assertThat(templateData.get("applicantName")).isEqualTo("");
        assertThat(templateData.get("respondents")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("representatives")).isEqualTo(getEmptyRepresentativeList());
        assertThat(templateData.get("hearingDate")).isEqualTo(HEARING_EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingVenue")).isEqualTo(HEARING_EMPTY_PLACEHOLDER);
        assertThat(templateData.get("preHearingAttendance")).isEqualTo(HEARING_EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingTime")).isEqualTo(HEARING_EMPTY_PLACEHOLDER);
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("legalAdvisorName")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("allParties")).isNull();
        assertThat(templateData.get("localAuthorityDirections")).isNull();
        assertThat(templateData.get("respondentDirections")).isNull();
        assertThat(templateData.get("cafcassDirections")).isNull();
        assertThat(templateData.get("otherPartiesDirections")).isNull();
        assertThat(templateData.get("courtDirections")).isNull();
        assertThat(templateData.get(RECITALS.getKey())).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("recitalsProvided")).isEqualTo(false);
        Arrays.stream(scheduleKeys).forEach(key -> assertThat(templateData.get(key)).isEqualTo(EMPTY_PLACEHOLDER));
        assertThat(templateData.get("scheduleProvided")).isEqualTo(false);
        assertThat(templateData.get("draftbackground")).isNotNull();
        assertThat(templateData.get("caseManagementNumber")).isEqualTo(1);
    }

    @Test
    void shouldReturnFullyPopulatedMapWhenCompleteCaseDetailsAreProvided() throws IOException {
        final Map<String, Object> caseDataMap = buildCaseDataMapForDraftCMODocmosisGeneration(NOW);

        final CaseData caseData = mapper.convertValue(caseDataMap, CaseData.class);

        final Map<String, Object> templateData = templateDataGenerationService.getTemplateData(caseData, true);

        assertThat(templateData.get("courtName")).isEqualTo(COURT_NAME);
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(dateFormatterService
            .formatLocalDateToString(NOW.toLocalDate().plusWeeks(26), FormatStyle.LONG));
        assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
        assertThat(templateData.get("numberOfChildren")).isEqualTo(getExpectedChildren().size());
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
        assertThat(templateData.get("respondents")).isEqualTo(getExpectedRespondents());
        assertThat(templateData.get("representatives")).isEqualTo(getExpectedRepresentatives());
        assertThat(templateData.get("hearingDate")).isEqualTo("");
        assertThat(templateData.get("hearingVenue")).isEqualTo(HEARING_VENUE);
        assertThat(templateData.get("preHearingAttendance")).isEqualTo(
            dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW.minusHours(1), "dd MMMM YYYY, h:mma"));
        assertThat(templateData.get("hearingTime")).isEqualTo(getHearingTime());
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("Her Honour Judge Law");
        assertThat(templateData.get("legalAdvisorName")).isEqualTo("Peter Parker");
        assertThat(templateData.get("allParties")).isEqualTo(getExpectedDirection(2));
        assertThat(templateData.get("localAuthorityDirections")).isEqualTo(getExpectedDirection(3));
        assertThat(templateData.get("respondentDirections")).isEqualTo(
            getExpectedDirectionWithHeader(4, RESPONDENT_1.getLabel()));
        assertThat(templateData.get("cafcassDirections")).isEqualTo(getExpectedDirection(5));
        assertThat(templateData.get("otherPartiesDirections")).isEqualTo(
            getExpectedDirectionWithHeader(6, OTHER_1.getLabel()));
        assertThat(templateData.get("courtDirections")).isEqualTo(getExpectedDirection(7));
        assertThat(templateData.get(RECITALS.getKey())).isEqualTo(getExpectedRecital());
        assertThat(templateData.get("recitalsProvided")).isEqualTo(true);
        assertThat(templateData).containsAllEntriesOf(getExpectedSchedule());
        assertThat(templateData.get("scheduleProvided")).isEqualTo(true);
        assertThat(templateData.get("draftbackground")).isNotNull();
        assertThat(templateData.get("caseManagementNumber")).isEqualTo(2);
    }

    private List<Map<String, Object>> getExpectedRepresentatives() {
        return List.of(
            Map.of("name", "Bran Stark", "representedBy", List.of(
                Map.of(
                    "representativeEmail", "bruce-wayne@notbatman.com",
                    "representativeName", "Bruce Wayne",
                    "representativePhoneNumber", "07700900304"))
            ),
            Map.of("name", "Timothy Jones", "representedBy", List.of(
                Map.of(
                    "representativeEmail", "1TJ@representatives.com",
                    "representativeName", "George Rep 1 (TJ)",
                    "representativePhoneNumber", "+44 79000001"),
                Map.of(
                    "representativeEmail", "2TJ@representatives.com",
                    "representativeName", "George Rep 2 (TJ)",
                    "representativePhoneNumber", "+44 79000002"))
            ),
            Map.of("name", "Sarah Simpson", "representedBy", List.of(
                Map.of(
                    "representativeEmail", "1SS@representatives.com",
                    "representativeName", "George Rep 1 (SS)",
                    "representativePhoneNumber", "+44 79000001"))
            ),
            Map.of("name", "Kyle Stafford", "representedBy", List.of(
                Map.of(
                    "representativeEmail", "1K@representatives.com",
                    "representativeName", "Barbara Rep 1 (K)",
                    "representativePhoneNumber", "+44 71000001"))
            )
        );
    }

    private List<Map<String, Object>> getEmptyRepresentativeList() {
        return List.of(
            Map.of(
                "name", EMPTY_PLACEHOLDER,
                "representedBy", List.of(
                    Map.of("representativeName", EMPTY_PLACEHOLDER,
                        "representativeEmail", EMPTY_PLACEHOLDER,
                        "representativePhoneNumber", EMPTY_PLACEHOLDER)))
        );
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

    private List<Map<String, Object>> getExpectedDirectionWithHeader(int index, String header) {
        return List.of(
            Map.of(
                "header", "For " + header,
                "directions", List.of(
                    Map.of(
                        "title", index + ". null by unknown",
                        "body", "Mock direction text"
                    )
                )
            )
        );
    }

    private String getHearingTime() {
        return String.format("%s - %s",
            dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW, "dd MMMM, h:mma"),
            dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW.plusDays(1), "dd MMMM, h:mma"));
    }

}
