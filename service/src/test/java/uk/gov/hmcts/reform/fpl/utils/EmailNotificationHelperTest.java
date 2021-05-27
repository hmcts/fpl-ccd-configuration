package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.getDistinctGatekeeperEmails;

class EmailNotificationHelperTest {

    private static final LocalDateTime CURRENT_DATE = LocalDateTime.of(2020, 12, 1, 0, 0, 0);
    private static final LocalDateTime PAST_DATE = LocalDateTime.of(2020, 11, 20, 0, 0, 0);
    private static final List<Element<Respondent>> RESPONDENTS = createRespondents();
    private static final String FAMILY_MAN_CASE_NUMBER = "FamilyManCaseNumber";
    private static final HearingBooking CURRENT_HEARING = HearingBooking.builder().startDate(CURRENT_DATE).build();

    private final EmailNotificationHelper underTest = new EmailNotificationHelper();

    @Test
    void shouldReturnLastNameOfEldestChild() {
        List<Element<Child>> children = wrapElements(
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Jones")
                    .dateOfBirth(CURRENT_DATE.toLocalDate())
                    .build())
                .build(),
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Ross")
                    .dateOfBirth(PAST_DATE.toLocalDate())
                    .build())
                .build()
        );

        assertThat(underTest.getEldestChildLastName(children)).isEqualTo("Ross");
    }

    @Test
    void shouldReturnConsistentNameWhenMultipleChildrenBornOnSameDay() {
        List<Element<Child>> children = wrapElements(
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Jones")
                    .dateOfBirth(CURRENT_DATE.toLocalDate())
                    .build())
                .build(),
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Ross")
                    .dateOfBirth(CURRENT_DATE.toLocalDate())
                    .build())
                .build()
        );

        List<String> names = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            names.add(underTest.getEldestChildLastName(children));
        }

        assertThat(names).containsOnly("Jones");
    }

    @Test
    void shouldIgnoreChildrenWithNoDateOfBirth() {
        List<Element<Child>> children = wrapElements(
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Jones")
                    .build())
                .build(),
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Ross")
                    .dateOfBirth(PAST_DATE.toLocalDate())
                    .build())
                .build()
        );

        assertThat(underTest.getEldestChildLastName(children)).isEqualTo("Ross");
    }

    @Test
    void shouldReturnEmptyStringWhenNoDateOfBirthPresent() {
        List<Element<Child>> children = wrapElements(
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Jones")
                    .build())
                .build(),
            Child.builder()
                .party(ChildParty.builder()
                    .lastName("Ross")
                    .build())
                .build()
        );

        assertThat(underTest.getEldestChildLastName(children)).isEmpty();
    }

    @Test
    void subjectLineShouldBeEmptyWhenNoRespondentOrCaseNumberEmpty() {
        String subjectLine = buildSubjectLine(null, null);
        assertThat(subjectLine).isEmpty();
    }

    @Test
    void subjectLineShouldMatchWhenRespondentAndCaseNumberGiven() {
        String expectedSubjectLine = "Jones, FamilyManCaseNumber";
        String subjectLine = buildSubjectLine(FAMILY_MAN_CASE_NUMBER, RESPONDENTS);
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldNotBeEmptyWhenOnlyRespondentGiven() {
        String expectedSubjectLine = "Jones";
        String subjectLine = buildSubjectLine(null, RESPONDENTS);
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldBeSuffixedWithHearingDate() {
        String returnedSubjectLine = buildSubjectLineWithHearingBookingDateSuffix(
            FAMILY_MAN_CASE_NUMBER, RESPONDENTS, CURRENT_HEARING
        );

        assertThat(returnedSubjectLine).isEqualTo("Jones, FamilyManCaseNumber, hearing 1 Dec 2020");
    }

    @Test
    void subjectLineSuffixShouldNotContainHearingDateWhenHearingBookingsNotProvided() {
        String actual = buildSubjectLineWithHearingBookingDateSuffix(
            FAMILY_MAN_CASE_NUMBER, RESPONDENTS, null
        );

        assertThat(actual).isEqualTo("Jones, FamilyManCaseNumber");
    }

    @Test
    void shouldNotAddHearingDateWhenNoFutureHearings() {
        String actual = buildSubjectLineWithHearingBookingDateSuffix(
            FAMILY_MAN_CASE_NUMBER, RESPONDENTS, null
        );

        assertThat(actual).isEqualTo("Jones, FamilyManCaseNumber");
    }

    @Test
    void shouldFormatCallOutWhenAllRequiredFieldsArePresent() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(wrapElements(CURRENT_HEARING))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName("Davids")
                    .build())
                .build()))
            .build();

        assertThat(buildCallout(caseData)).isEqualTo("Davids, 12345, hearing 1 Dec 2020");
    }

    @Test
    void shouldFormatCallOutWhenOnlySomeRequiredFieldsArePresent() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(wrapElements(CURRENT_HEARING))
            .build();

        assertThat(buildCallout(caseData)).isEqualTo("12345, hearing 1 Dec 2020");
    }

    @Test
    void shouldBuildCallOutWithNextHearing() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(wrapElements(CURRENT_HEARING))
            .build();

        assertThat(buildCalloutWithNextHearing(caseData, PAST_DATE)).isEqualTo("^12345, hearing 1 Dec 2020");
    }

    @Test
    void shouldNotIncludeHearingIfNoFutureHearings() {
        HearingBooking hearing = HearingBooking.builder()
            .startDate(PAST_DATE)
            .build();

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("12345")
            .hearingDetails(wrapElements(hearing))
            .build();

        assertThat(buildCalloutWithNextHearing(caseData, CURRENT_DATE)).isEqualTo("^12345");
    }

    @Test
    void shouldReturnDistinctGatekeepersEmailAddressesWhenDuplicateEmailAddressesExist() {
        List<Element<EmailAddress>> emailCollection = wrapElements(
            EmailAddress.builder().email("gatekeeper1@test.com").build(),
            EmailAddress.builder().email("gatekeeper2@test.com").build(),
            EmailAddress.builder().email("gatekeeper2@test.com").build()
        );

        List<String> emailAddresses = getDistinctGatekeeperEmails(emailCollection);

        assertThat(emailAddresses).containsAll(List.of("gatekeeper1@test.com", "gatekeeper2@test.com"));
    }

    @Test
    void shouldReturnGatekeepersEmailAddressesWhenDuplicateEmailAddressesDoNotExist() {
        List<Element<EmailAddress>> emailCollection = wrapElements(
            EmailAddress.builder().email("gatekeeper1@test.com").build()
        );

        List<String> emailAddresses = getDistinctGatekeeperEmails(emailCollection);

        assertThat(emailAddresses).containsOnly("gatekeeper1@test.com");
    }
}
