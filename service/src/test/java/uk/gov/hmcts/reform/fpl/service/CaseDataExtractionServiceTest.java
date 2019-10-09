package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;

@ExtendWith(SpringExtension.class)
class CaseDataExtractionServiceTest {
    @SuppressWarnings({"membername", "AbbreviationAsWordInName"})

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);
    private static final LocalDate TODAYS_DATE = LocalDate.now();

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HearingBookingService hearingBookingService = new HearingBookingService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);

    private CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService(dateFormatterService,
        hearingBookingService, hmctsCourtLookupConfiguration);

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

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);
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

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
    }

    @Test
    void shouldMapCaseDataPropertiesToTemplatePlaceholderData() {
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

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);
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
                .build()
        );
    }
}
