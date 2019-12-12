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
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_1;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
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
        assertThat(templateData.get("hearingDate")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingVenue")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("preHearingAttendance")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingTime")).isEqualTo(EMPTY_PLACEHOLDER);
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
        assertThat(templateData.get("recitals")).isEqualTo(getExpectedRecital());
        assertThat(templateData.get("recitalsProvided")).isEqualTo(true);
        assertThat(templateData).containsAllEntriesOf(getExpectedSchedule());
        assertThat(templateData.get("scheduleProvided")).isEqualTo(true);
        assertThat(templateData.get("draftbackground")).isNotNull();
        assertThat(templateData.get("caseManagementNumber")).isEqualTo(2);
    }

    private List<Map<String, String>> getExpectedRepresentatives() {
        return List.of(
            Map.of(
                "respondentName", "Bran Stark",
                "representativeEmail", "bruce-wayne@notbatman.com",
                "representativeName", "Bruce Wayne",
                "representativePhoneNumber", "07700900304"
            ),
            Map.of(
                "respondentName", "Timothy Jones",
                "representativeName", "BLANK - please complete",
                "representativeEmail", "BLANK - please complete",
                "representativePhoneNumber", "BLANK - please complete"
            ),
            Map.of(
                "respondentName", "Sarah Simpson",
                "representativeName", "BLANK - please complete",
                "representativeEmail", "BLANK - please complete",
                "representativePhoneNumber", "BLANK - please complete"
            )
        );
    }

    private List<Map<String, String>> getEmptyRepresentativeList() {
        return List.of(
            Map.of(
                "respondentName", EMPTY_PLACEHOLDER,
                "representativeName", EMPTY_PLACEHOLDER,
                "representativeEmail", EMPTY_PLACEHOLDER,
                "representativePhoneNumber", EMPTY_PLACEHOLDER
            )
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

}
