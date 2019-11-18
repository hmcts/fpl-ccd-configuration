package uk.gov.hmcts.reform.fpl.service;

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
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createStandardDirectionOrders;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, JsonOrdersLookupService.class,
    HearingVenueLookUpService.class})
class CaseDataExtractionServiceTest {
    @SuppressWarnings({"membername", "AbbreviationAsWordInName"})

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);
    private static final LocalDate TODAYS_DATE = LocalDate.now();
    private static final LocalDateTime TODAYS_DATE_TIME = LocalDateTime.now();
    private static final String EMPTY_PLACEHOLDER = "BLANK - please complete";

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HearingBookingService hearingBookingService = new HearingBookingService();
    private DirectionHelperService directionHelperService = new DirectionHelperService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);
    private CommonCaseDataExtractionService commonCaseDataExtraction = new CommonCaseDataExtractionService(
        dateFormatterService);

    @Autowired
    private OrdersLookupService ordersLookupService;

    @Autowired
    private HearingVenueLookUpService hearingVenueLookUpService;

    private CaseDataExtractionService caseDataExtractionService;

    @BeforeEach
    void setup() {
        // required for DI
        this.caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
            hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService, directionHelperService,
            hearingVenueLookUpService, commonCaseDataExtraction);
    }

    @Test
    void shouldMapEmptyCaseDataForDraftSDO() throws IOException {
        Map<String, Object> templateData = caseDataExtractionService
            .getStandardOrderDirectionData(CaseData.builder().build());

        assertThat(templateData.get("judgeAndLegalAdvisor")).isEqualToComparingFieldByField(ImmutableMap.of(
            "judgeTitle", EMPTY_PLACEHOLDER,
            "legalAdvisorName", EMPTY_PLACEHOLDER
        ));
        assertThat(templateData.get("courtName")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("children")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("hearingDate")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingVenue")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("preHearingAttendance")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingTime")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("respondents")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("allParties")).isNull();
        assertThat(templateData.get("localAuthorityDirections")).isNull();
        assertThat(templateData.get("parentsAndRespondentsDirections")).isNull();
        assertThat(templateData.get("cafcassDirections")).isNull();
        assertThat(templateData.get("otherPartiesDirections")).isNull();
        assertThat(templateData.get("courtDirections")).isNull();
    }

    @Test
    void shouldMapCaseDataWhenEmptyListValues() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren())
            .hearingDetails(ImmutableList.of())
            .dateSubmitted(LocalDate.now())
            .respondents1(ImmutableList.of())
            .standardDirectionOrder(createStandardDirectionOrders(TODAYS_DATE_TIME, OrderStatus.DRAFT))
            .build();

        Map<String, Object> templateData = caseDataExtractionService
            .getStandardOrderDirectionData(caseData);

        assertThat(templateData.get("judgeAndLegalAdvisor")).isEqualTo(ImmutableMap.of(
            "judgeTitle", HER_HONOUR_JUDGE.getLabel(),
            "judgeLastName", "Smith",
            "legalAdvisorName", "Bob Ross"
        ));
        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE.plusWeeks(26), FormatStyle.LONG));
        assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
        assertThat(templateData.get("hearingDate")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingVenue")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("preHearingAttendance")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("hearingTime")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(templateData.get("respondents")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("allParties")).isEqualTo(getExpectedDirections());
        assertThat(templateData.get("draftbackground")).isNotNull();
    }

    @Test
    void shouldMapCompleteCaseDataForSDOTemplate() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren())
            .hearingDetails(createHearingBookings())
            .dateSubmitted(LocalDate.now())
            .respondents1(createRespondents())
            .standardDirectionOrder(createStandardDirectionOrders(TODAYS_DATE_TIME, OrderStatus.SEALED))
            .build();

        Map<String, Object> templateData = caseDataExtractionService
            .getStandardOrderDirectionData(caseData);

        assertThat(templateData.get("judgeAndLegalAdvisor")).isEqualTo(ImmutableMap.of(
            "judgeTitle", HER_HONOUR_JUDGE.getLabel(),
            "judgeLastName", "Smith",
            "legalAdvisorName", "Bob Ross"
        ));
        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE.plusWeeks(26), FormatStyle.LONG));
        assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
        assertThat(templateData.get("hearingDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("hearingVenue"))
            .isEqualTo("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW");
        assertThat(templateData.get("judgeName")).isEqualTo("HHJ Judith Law");
        assertThat(templateData.get("preHearingAttendance")).isEqualTo("8:30am");
        assertThat(templateData.get("hearingTime")).isEqualTo("9:30am - 11:30am");
        assertThat(templateData.get("respondents")).isEqualTo(getExpectedRespondents());
        assertThat(templateData.get("allParties")).isEqualTo(getExpectedDirections());
        assertThat(templateData.get("draftbackground")).isNull();
    }

    private List<Map<String, String>> getExpectedChildren() {
        return List.of(
            Map.of(
                "name", "Bran Stark",
                "gender", "Male",
                "dateOfBirth", dateFormatterService.formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG)),
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

    private List<Map<String, String>> getExpectedDirections() {
        return List.of(
            Map.of(
                "title", String.format("2. Test SDO type 1 on %s",
                    TODAYS_DATE_TIME.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK))),
                "body", "Test body 1"),
            Map.of(
                "title", String.format("3. Test SDO type 2 by %s",
                    TODAYS_DATE_TIME.format(DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy", Locale.UK))),
                "body", "Test body 2")
        );
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(TODAYS_DATE, LocalTime.of(9, 30)),
                    LocalDateTime.of(TODAYS_DATE, LocalTime.of(11, 30))))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(TODAYS_DATE, LocalTime.of(12, 30)),
                    LocalDateTime.of(TODAYS_DATE, LocalTime.of(13, 30))))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(TODAYS_DATE, LocalTime.of(15, 30)),
                    LocalDateTime.of(TODAYS_DATE, LocalTime.of(16, 0))))
                .build()
        );
    }
}
