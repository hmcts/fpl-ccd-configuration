package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class EmailNotificationHelperTest {

    @Test
    void subjectLineShouldBeEmptyWhenNoRespondentOrCaseNumberEmpty() {
        CaseData data = CaseData.builder()
            .build();
        String subjectLine = buildSubjectLine(data.getFamilyManCaseNumber(), data.getRespondents1());
        assertThat(subjectLine).isEmpty();
    }

    @Test
    void subjectLineShouldMatchWhenRespondentAndCaseNumberGiven() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("FamilyManCaseNumber")
            .respondents1(createRespondents())
            .build();

        String expectedSubjectLine = "Jones, FamilyManCaseNumber";
        String subjectLine = buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getRespondents1());
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldNotBeEmptyWhenOnlyRespondentGiven() {
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .build();

        String expectedSubjectLine = "Jones";
        String subjectLine = buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getRespondents1());
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldReturnFirstRespondentElementAlwaysWhenMultipleRespondentsGiven() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Timothy")
                        .lastName(null)
                        .relationshipToChild("Father")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Timothy")
                        .lastName("Jones")
                        .relationshipToChild("Father")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Sarah")
                        .lastName("Simpson")
                        .relationshipToChild("Mother")
                        .build())
                    .build())
                .build()
        );

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .familyManCaseNumber("FamilyManCaseNumber-With-Empty-Lastname")
            .build();

        String expectedSubjectLine = "FamilyManCaseNumber-With-Empty-Lastname";
        String subjectLine = buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getRespondents1());
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldBeSuffixedWithHearingDate() {
        final LocalDateTime futureDate = LocalDateTime.of(2022, 5, 23, 0, 0, 0);
        List<Element<HearingBooking>> hearingBookingsFromInitialDate =
            createHearingBookingsFromInitialDate(futureDate);
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .hearingDetails(hearingBookingsFromInitialDate)
            .familyManCaseNumber("FamilyManCaseNumber")
            .build();

        HearingBooking hearingBooking = unwrapElements(caseData.getHearingDetails()).get(2);

        String expectedSubjectLine = "Jones, FamilyManCaseNumber, hearing 23 May 2022";
        String returnedSubjectLine = buildSubjectLineWithHearingBookingDateSuffix(caseData
                .getFamilyManCaseNumber(),
            caseData.getRespondents1(), hearingBooking);
        assertThat(returnedSubjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineSuffixShouldNotContainHearingDateWhenHearingBookingsNotProvided() {
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .hearingDetails(null)
            .familyManCaseNumber("FamilyManCaseNumber")
            .build();

        String expectedSubjectLine = "Jones, FamilyManCaseNumber";
        String returnedSubjectLine = buildSubjectLineWithHearingBookingDateSuffix(caseData
            .getFamilyManCaseNumber(), caseData.getRespondents1(), null);
        assertThat(returnedSubjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void shouldNotAddHearingDateWhenNoFutureHearings() {
        LocalDateTime pastDate = LocalDateTime.now().minusYears(10);
        List<Element<HearingBooking>> hearingBookings = createHearingBookingsFromInitialDate(pastDate);
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .hearingDetails(hearingBookings)
            .familyManCaseNumber("FamilyManCaseNumber")
            .build();

        String expected = "Jones, FamilyManCaseNumber";
        String actual = buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(), null);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldFormatCallOutWhenAllRequiredFieldsArePresent() {
        LocalDateTime hearingDate = LocalDateTime.now();
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(hearingDate)
            .build();

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(List.of(
                element(hearingBooking)))
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Davids")
                        .build())
                    .build())))
            .build();

        String expectedContent = String.format("Davids, 12345,%s", buildHearingDateText(hearingBooking));

        assertThat(buildCallout(caseData)).isEqualTo(expectedContent);
    }

    @Test
    void shouldFormatCallOutWhenOnlySomeRequiredFieldsArePresent() {
        LocalDateTime hearingDate = LocalDateTime.now();
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(hearingDate)
            .build();

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(List.of(
                element(hearingBooking)))
            .build();

        String expectedContent = String.format("12345,%s", buildHearingDateText(hearingBooking));

        assertThat(buildCallout(caseData)).isEqualTo(expectedContent);
    }

    @Test
    void shouldBuildCallOutWithNextHearing() {
        LocalDateTime hearingDate = LocalDateTime.now().plusDays(5);
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(hearingDate)
            .build();

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(List.of(
                element(hearingBooking)))
            .build();

        String expectedContent = String.format("^12345,%s", buildHearingDateText(hearingBooking));

        assertThat(buildCalloutWithNextHearing(caseData, LocalDateTime.now())).isEqualTo(expectedContent);
    }

    @Test
    void shouldNotIncludeHearingIfNoFutureHearings() {
        LocalDateTime hearingDate = LocalDateTime.now().minusDays(5);
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(hearingDate)
            .build();

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(List.of(
                element(hearingBooking)))
            .build();

        String expectedContent = "^12345";

        assertThat(buildCalloutWithNextHearing(caseData, LocalDateTime.now())).isEqualTo(expectedContent);
    }

    @Test
    void shouldReturnDistinctGatekeepersEmailAddressesWhenDuplicateEmailAddressesExist() {
        List<Element<EmailAddress>> emailCollection = List.of(
            element(EmailAddress.builder().email("gatekeeper1@test.com").build()),
            element(EmailAddress.builder().email("gatekeeper2@test.com").build()),
            element(EmailAddress.builder().email("gatekeeper2@test.com").build())
        );

        List<String> emailAddresses = EmailNotificationHelper.getDistinctGatekeeperEmails(emailCollection);

        assertThat(emailAddresses).containsAll(List.of("gatekeeper1@test.com", "gatekeeper2@test.com"));
    }

    @Test
    void shouldReturnGatekeepersEmailAddressesWhenDuplicateEmailAddressesDoNotExist() {
        List<Element<EmailAddress>> emailCollection = singletonList(
            element(EmailAddress.builder().email("gatekeeper1@test.com").build()));

        List<String> emailAddresses = EmailNotificationHelper.getDistinctGatekeeperEmails(emailCollection);

        assertThat(emailAddresses).containsOnly("gatekeeper1@test.com");
    }

    private static String buildHearingDateText(HearingBooking hearingBooking) {
        return " hearing " + formatLocalDateToString(hearingBooking
            .getStartDate().toLocalDate(), FormatStyle.MEDIUM);
    }
}
