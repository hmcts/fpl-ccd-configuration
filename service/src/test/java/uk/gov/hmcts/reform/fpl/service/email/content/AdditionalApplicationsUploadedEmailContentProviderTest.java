package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType.SAME_DAY;
import static uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType.WITHIN_2_DAYS;
import static uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType.WITHIN_5_DAYS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {AdditionalApplicationsUploadedEmailContentProvider.class})
class AdditionalApplicationsUploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final LocalDateTime PAST_HEARING_DATE = LocalDateTime.of(2000, 2, 12, 0, 0, 0);
    private static final LocalDateTime FUTURE_HEARING_DATE = LocalDateTime.of(2099, 2, 12, 0, 0, 0);
    private static final String HEARING_CALLOUT = "hearing 12 Feb 2099";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final String CHILD_LAST_NAME = "Jones";

    @MockBean
    private Time time;
    @MockBean
    private EmailNotificationHelper helper;
    @MockBean
    private CalendarService calendarService;

    @Autowired
    private AdditionalApplicationsUploadedEmailContentProvider underTest;

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        CaseData caseData = buildCaseData();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(CHILD_LAST_NAME);
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters = AdditionalApplicationsUploadedTemplate.builder()
            .callout(RESPONDENT_LAST_NAME + ", 12345, " + HEARING_CALLOUT)
            .lastName(CHILD_LAST_NAME)
            .childLastName(CHILD_LAST_NAME)
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .applicationTypes(Arrays.asList("C2 (With notice) - Appointment of a guardian",
                "C13A - Special guardianship order",
                "C20 - Secure accommodation (England)",
                "C1 - Parental responsibility by the father",
                "C13A - Special guardianship order",
                "C20 - Secure accommodation (England)"))
            .urgencyDetails("")
            .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetailsWithUrgencyDetails() {
        CaseData caseData = buildCaseData().toBuilder()
                .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(
                    OtherApplicationsBundle.builder()
                        .applicationType(C1_PARENTAL_RESPONSIBILITY)
                        .parentalResponsibilityType(PR_BY_FATHER)
                        .urgencyTImeFrameType(WITHIN_2_DAYS)
                        .build()
                )
                .build()))
            .build();

        when(calendarService.getWorkingDayFrom(LocalDate.now(), WITHIN_2_DAYS.getCount()))
                .thenReturn(LocalDate.now().plusDays(WITHIN_2_DAYS.getCount()));
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(CHILD_LAST_NAME);
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters = AdditionalApplicationsUploadedTemplate.builder()
            .callout(RESPONDENT_LAST_NAME + ", 12345, " + HEARING_CALLOUT)
            .lastName(CHILD_LAST_NAME)
            .childLastName(CHILD_LAST_NAME)
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .applicationTypes(List.of("C1 - Parental responsibility by the father"))
            .urgencyDetails("This application will need to be considered by the judge within "
                    + formatLocalDateToString(LocalDate.now().plusDays(WITHIN_2_DAYS.getCount()),DATE))
            .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetailsWithUrgencyDetailsOfSameDay() {
        CaseData caseData = buildCaseData().toBuilder()
                .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                        .otherApplicationsBundle(
                                OtherApplicationsBundle.builder()
                                        .applicationType(C1_PARENTAL_RESPONSIBILITY)
                                        .parentalResponsibilityType(PR_BY_FATHER)
                                        .urgencyTImeFrameType(SAME_DAY)
                                        .build()
                        )
                        .build()))
                .build();

        when(calendarService.isWorkingDay(LocalDate.now())).thenReturn(true);
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(CHILD_LAST_NAME);
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters = AdditionalApplicationsUploadedTemplate.builder()
                .callout(RESPONDENT_LAST_NAME + ", 12345, " + HEARING_CALLOUT)
                .lastName(CHILD_LAST_NAME)
                .childLastName(CHILD_LAST_NAME)
                .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
                .applicationTypes(List.of("C1 - Parental responsibility by the father"))
                .urgencyDetails("This application will need to be considered by the judge on "
                        + formatLocalDateToString(LocalDate.now(),DATE))
                .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetailsWithUrgencyDetailsOfSameDayNonWorkingDay() {
        CaseData caseData = buildCaseData().toBuilder()
                .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                        .otherApplicationsBundle(
                                OtherApplicationsBundle.builder()
                                        .applicationType(C1_PARENTAL_RESPONSIBILITY)
                                        .parentalResponsibilityType(PR_BY_FATHER)
                                        .urgencyTImeFrameType(SAME_DAY)
                                        .build()
                        )
                        .build()))
                .build();

        LocalDate nextDay = LocalDate.now().plusDays(1);
        when(calendarService.isWorkingDay(LocalDate.now())).thenReturn(false);
        when(calendarService.getWorkingDayFrom(LocalDate.now(), 1))
                .thenReturn(nextDay);
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(CHILD_LAST_NAME);
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters = AdditionalApplicationsUploadedTemplate.builder()
                .callout(RESPONDENT_LAST_NAME + ", 12345, " + HEARING_CALLOUT)
                .lastName(CHILD_LAST_NAME)
                .childLastName(CHILD_LAST_NAME)
                .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
                .applicationTypes(List.of("C1 - Parental responsibility by the father"))
                .urgencyDetails("This application will need to be considered by the judge on "
                        + formatLocalDateToString(nextDay,DATE))
                .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetailsWhenRequestingC2WithParentalResponsibility() {
        CaseData caseData = buildCaseData().toBuilder()
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder().c2DocumentBundle(
                C2DocumentBundle.builder()
                    .type(C2ApplicationType.WITH_NOTICE)
                    .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
                    .parentalResponsibilityType(PR_BY_FATHER)
                    .supplementsBundle(List.of())
                    .build())
                .build()))
            .build();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(CHILD_LAST_NAME);
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters =
            AdditionalApplicationsUploadedTemplate.builder()
                .callout(RESPONDENT_LAST_NAME + ", 12345, " + HEARING_CALLOUT)
                .lastName(CHILD_LAST_NAME)
                .childLastName(CHILD_LAST_NAME)
                .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
                .applicationTypes(List.of("C2 (With notice) - Parental responsibility by the father"))
                .urgencyDetails("")
                .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetailsWhenRequestingC2WithParentalResponsibilityAndUrgency() {
        CaseData caseData = buildCaseData().toBuilder()
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder().c2DocumentBundle(
                C2DocumentBundle.builder()
                    .type(C2ApplicationType.WITH_NOTICE)
                    .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
                    .parentalResponsibilityType(PR_BY_FATHER)
                    .supplementsBundle(List.of())
                    .urgencyTimeFrameType(WITHIN_5_DAYS)
                    .build())
                .build()))
            .build();
        given(calendarService.getWorkingDayFrom(LocalDate.now(), WITHIN_5_DAYS.getCount()))
                .willReturn(LocalDate.now().plusDays(WITHIN_5_DAYS.getCount()));
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(CHILD_LAST_NAME);
        when(time.now()).thenReturn(FUTURE_HEARING_DATE.minusDays(1));

        AdditionalApplicationsUploadedTemplate expectedParameters =
            AdditionalApplicationsUploadedTemplate.builder()
                .callout(RESPONDENT_LAST_NAME + ", 12345, " + HEARING_CALLOUT)
                .lastName(CHILD_LAST_NAME)
                .childLastName(CHILD_LAST_NAME)
                .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
                .applicationTypes(List.of("C2 (With notice) - Parental responsibility by the father"))
                .urgencyDetails("This application will need to be considered by the judge within "
                        + formatLocalDateToString(LocalDate.now().plusDays(WITHIN_5_DAYS.getCount()),DATE))
                .build();

        AdditionalApplicationsUploadedTemplate actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        CaseData caseData = buildCaseData();

        BaseCaseNotifyData expectedParameters = BaseCaseNotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, OTHER_APPLICATIONS))
            .build();

        BaseCaseNotifyData actualParameters = underTest.getPbaPaymentNotTakenNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    private CaseData buildCaseData() {
        List<Supplement> supplements = Arrays.asList(
            Supplement.builder().name(C13A_SPECIAL_GUARDIANSHIP).build(),
            Supplement.builder()
                .name(C20_SECURE_ACCOMMODATION)
                .secureAccommodationType(SecureAccommodationType.ENGLAND)
                .build()
        );

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .type(C2ApplicationType.WITH_NOTICE)
            .supplementsBundle(wrapElements(supplements))
            .c2AdditionalOrdersRequested(Collections.singletonList(C2AdditionalOrdersRequested.APPOINTMENT_OF_GUARDIAN))
            .parentalResponsibilityType(PR_BY_FATHER)
            .build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .applicationType(C1_PARENTAL_RESPONSIBILITY)
            .parentalResponsibilityType(PR_BY_FATHER)
            .supplementsBundle(wrapElements(supplements))
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build();

        return CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("John").lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .hearingDetails(wrapElements(
                    HearingBooking.builder().startDate((PAST_HEARING_DATE)).build(),
                    HearingBooking.builder().startDate((FUTURE_HEARING_DATE)).build()))
            .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
            .build();
    }
}
