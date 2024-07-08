package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.fpl.enums.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.HearingVacatedTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HearingVacatedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.of(2024, 01, 01, 10, 30);
    private static final LocalDateTime HEARING_END_DATE = HEARING_START_DATE.plusDays(1);
    private static final LocalDate VACATED_DATE = HEARING_START_DATE.minusDays(1).toLocalDate();
    private static final String HEARING_VENUE = "Hearing venue";
    private static final String CANCELLATION_REASON = "cancel reason";
    private static final String FAMILYMANID = "TEST";
    private static final Long CASE_ID = 1234L;

    private static final HearingBooking VACATED_HEARING = HearingBooking.builder()
        .startDate(HEARING_START_DATE)
        .endDate(HEARING_END_DATE)
        .venue(HEARING_VENUE)
        .vacatedDate(VACATED_DATE)
        .cancellationReason(CANCELLATION_REASON)
        .build();

    @Mock
    private CaseDataExtractionService caseDataExtractionService;
    @Mock
    private HearingVenueLookUpService hearingVenueLookUpService;

    @InjectMocks
    private HearingVacatedEmailContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(hearingVenueLookUpService.getHearingVenue(any(HearingBooking.class))).thenReturn(mock(HearingVenue.class));
        when(hearingVenueLookUpService.buildHearingVenue(any())).thenReturn(HEARING_VENUE);
        when(caseDataExtractionService.getHearingTime(any())).thenReturn("10:30 - 10:30");
        when(CASE_DATA.getFamilyManCaseNumber()).thenReturn(FAMILYMANID);
        when(CASE_DATA.getId()).thenReturn(CASE_ID);
    }

    @Test
    void shouldBuildVacatedAndRelistedEmailContent() {
        HearingVacatedTemplate actualEmailTemplate =
            underTest.buildHearingVacatedNotification(CASE_DATA, VACATED_HEARING, true);

        assertThat(actualEmailTemplate).isEqualTo(buildExpectedHearingVacatedTemplate(CANCELLATION_REASON, true));
    }

    @Test
    void shouldBuildVacatedButNotRelistedEmailContent() {
        HearingVacatedTemplate actualEmailTemplate =
            underTest.buildHearingVacatedNotification(CASE_DATA, VACATED_HEARING, false);

        assertThat(actualEmailTemplate).isEqualTo(buildExpectedHearingVacatedTemplate(CANCELLATION_REASON, false));
    }

    @Test
    void shouldGetCancellationReasonLabelIfValid() {
        HearingVacatedTemplate actualEmailTemplate =
            underTest.buildHearingVacatedNotification(CASE_DATA,
                VACATED_HEARING.toBuilder().cancellationReason("LA1").build(),
                false);

        assertThat(actualEmailTemplate)
            .isEqualTo(buildExpectedHearingVacatedTemplate(HearingCancellationReason.LA1.getLabel(), false));
    }

    @Test
    void shouldDefaultToCCDIdIfNoFamilyMan() {
        when(CASE_DATA.getFamilyManCaseNumber()).thenReturn(null);

        HearingVacatedTemplate actualEmailTemplate =
            underTest.buildHearingVacatedNotification(CASE_DATA,
                VACATED_HEARING.toBuilder().cancellationReason("LA1").build(),
                false);

        assertThat(actualEmailTemplate)
            .isEqualTo(buildExpectedTemplateWithCcdId(HearingCancellationReason.LA1.getLabel(), false));
    }

    private HearingVacatedTemplate buildExpectedHearingVacatedTemplate(String cancelReason, boolean isRelisted) {
        return HearingVacatedTemplate.builder()
            .hearingDate(HEARING_START_DATE)
            .hearingDateFormatted("1 January 2024")
            .hearingVenue(HEARING_VENUE)
            .hearingTime("10:30 - 10:30")
            .familyManCaseNumber(FAMILYMANID)
            .vacatedDate("31 December 2023")
            .vacatedReason(cancelReason)
            .relistAction(isRelisted ? HearingVacatedEmailContentProvider.RELIST_ACTION_RELISTED
                : HearingVacatedEmailContentProvider.RELIST_ACTION_NOT_RELISTED)
            .build();
    }

    private HearingVacatedTemplate buildExpectedTemplateWithCcdId(String cancelReason, boolean isRelisted) {
        return HearingVacatedTemplate.builder()
            .hearingDate(HEARING_START_DATE)
            .hearingDateFormatted("1 January 2024")
            .hearingVenue(HEARING_VENUE)
            .hearingTime("10:30 - 10:30")
            .familyManCaseNumber(CASE_ID.toString())
            .vacatedDate("31 December 2023")
            .vacatedReason(cancelReason)
            .relistAction(isRelisted ? HearingVacatedEmailContentProvider.RELIST_ACTION_RELISTED
                : HearingVacatedEmailContentProvider.RELIST_ACTION_NOT_RELISTED)
            .build();
    }

}
