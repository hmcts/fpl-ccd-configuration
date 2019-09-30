package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createStandardDirectionOrders;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, JsonOrdersLookupService.class})
class CaseDataExtractionServiceTest {
    @SuppressWarnings({"membername", "AbbreviationAsWordInName"})

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);
    private static final LocalDate TODAYS_DATE = LocalDate.now();
    private static final LocalDateTime TODAYS_DATE_TIME = LocalDateTime.now();
    private static final String EMPTY_STATE_PLACEHOLDER = "BLANK - please complete";

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HearingBookingService hearingBookingService = new HearingBookingService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);

    @Autowired
    private OrdersLookupService ordersLookupService;

    private CaseDataExtractionService caseDataExtractionService;

    @BeforeEach
    public void setup() {
        // required for DI
        this.caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
            hearingBookingService, hmctsCourtLookupConfiguration, ordersLookupService);
    }

    @Test
    void shouldConcatenateAllChildrenNames() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren())
            .applicants(createPopulatedApplicants())
            .hearingDetails(createHearingBookings())
            .orders(Orders.builder()
                .orderType(ImmutableList.<OrderType>of(CARE_ORDER)).build())
            .build();

        Map<String, Object> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark, Sansa Stark");
    }

    @Test
    void shouldReturnFirstApplicantName() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren())
            .applicants(createPopulatedApplicants())
            .hearingDetails(createHearingBookings())
            .orders(Orders.builder()
                .orderType(ImmutableList.<OrderType>of(CARE_ORDER)).build())
            .build();

        Map<String, Object> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
    }

    @Test
    void shouldGenerateNoticeOfProceedingsTemplateData() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren())
            .applicants(createPopulatedApplicants())
            .hearingDetails(createHearingBookings())
            .orders(Orders.builder()
                .orderType(ImmutableList.<OrderType>of(
                    CARE_ORDER,
                    EDUCATION_SUPERVISION_ORDER
                )).build())
            .build();

        Map<String, Object> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
        assertThat(templateData.get("orderTypes")).isEqualTo("Care order, Education supervision order");
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark, Sansa Stark");
        assertThat(templateData.get("hearingDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("hearingVenue")).isEqualTo("Venue");
        assertThat(templateData.get("preHearingAttendance")).isEqualTo("08.15am");
        assertThat(templateData.get("hearingTime")).isEqualTo("09.15am");
    }

    @Test
    void shouldMapEmptyCaseDataForDraftSDO() throws IOException {
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> templateData = caseDataExtractionService
            .getDraftStandardOrderDirectionTemplateData(caseData);

        assertThat(templateData.get("courtName")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("children")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("hearingDate")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("hearingVenue")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("preHearingAttendance")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("hearingTime")).isEqualTo(EMPTY_STATE_PLACEHOLDER);
        assertThat(templateData.get("respondents")).isEqualTo(ImmutableList.of());
        assertThat(templateData.get("allParties")).isNull();
        assertThat(templateData.get("localAuthorityDirections")).isNull();
        assertThat(templateData.get("parentsAndRespondentsDirections")).isNull();
        assertThat(templateData.get("cafcassDirections")).isNull();
        assertThat(templateData.get("otherPartiesDirections")).isNull();
        assertThat(templateData.get("courtDirections")).isNull();
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
            .standardDirectionOrder(createStandardDirectionOrders(TODAYS_DATE_TIME))
            .build();

        Map<String, Object> templateData = caseDataExtractionService
            .getDraftStandardOrderDirectionTemplateData(caseData);

        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("generationDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("complianceDeadline")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE.plusWeeks(26), FormatStyle.LONG));
        assertThat(templateData.get("children")).isEqualTo(getExpectedChildren());
        assertThat(templateData.get("hearingDate")).isEqualTo(dateFormatterService
            .formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG));
        assertThat(templateData.get("hearingVenue")).isEqualTo("Venue");
        assertThat(templateData.get("preHearingAttendance")).isEqualTo("08.15am");
        assertThat(templateData.get("hearingTime")).isEqualTo("09.15am");
        assertThat(templateData.get("respondents")).isEqualTo(getExpectedRespondents());
        assertThat(templateData.get("allParties")).isEqualTo(getExpectedDirections());
    }

    private List<Map<String, String>> getExpectedChildren() {
        return List.of(
            Map.of(
                "name", "Bran Stark",
                "gender", "Male",
                "dateOfBirth", dateFormatterService.formatLocalDateToString(TODAYS_DATE, FormatStyle.LONG)),
            Map.of(
                "name", "Sansa Stark",
                "gender", EMPTY_STATE_PLACEHOLDER,
                "dateOfBirth", EMPTY_STATE_PLACEHOLDER)
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
                "title", String.format("Test SDO type 1 on %s",
                    TODAYS_DATE_TIME.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK))),
                "body", "Test body 1"),
            Map.of(
                "title", String.format("Test SDO type 2 by %s",
                    TODAYS_DATE_TIME.format(DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy", Locale.UK))),
                "body", "Test body 2")
        );
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(TODAYS_DATE))
                .build());
    }
}
