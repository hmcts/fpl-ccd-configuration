package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.getFirstRespondentLastName;

class EmailNotificationHelperTest {
    private final DateFormatterService dateFormatterService = new DateFormatterService();

    @Test
    void subjectLineShouldBeEmptyWhenNoRespondentOrCaseNumberEmpty() {
        String subjectLine = buildSubjectLine(CaseData.builder().build());
        assertThat(subjectLine).isEmpty();
    }

    @Test
    void subjectLineShouldMatchWhenRespondentAndCaseNumberGiven() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("FamilyManCaseNumber")
            .respondents1(createRespondents())
            .build();

        String expectedSubjectLine = "Jones, FamilyManCaseNumber";
        String subjectLine = buildSubjectLine(caseData);
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldNotBeEmptyWhenOnlyRespondentGiven() {
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .build();

        String expectedSubjectLine = "Jones";
        String subjectLine = buildSubjectLine(caseData);
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
        String subjectLine = buildSubjectLine(caseData);
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void subjectLineShouldBeSuffixedWithHearingDate() {
        final LocalDateTime dateInTenMonths = LocalDateTime.now().plusMonths(10);
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .hearingDetails(createHearingBookings(dateInTenMonths))
            .familyManCaseNumber("FamilyManCaseNumber")
            .build();

        String expectedSubjectLine = "Jones, FamilyManCaseNumber, hearing "
            + dateFormatterService.formatLocalDateTimeBaseUsingFormat(dateInTenMonths, "d MMM yyyy");
        String subjectLine = buildSubjectLine(caseData);
        String returnedSubjectLine = buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
            caseData.getHearingDetails());
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
        String subjectLine = buildSubjectLine(caseData);
        String returnedSubjectLine = buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
            caseData.getHearingDetails());
        assertThat(returnedSubjectLine).isEqualTo(expectedSubjectLine);
    }

    @Test
    void shouldReturnFirstRespondentSurnameWhenFirstRespondentWithNamePresent() {
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .build();

        String respondentName = getFirstRespondentLastName(caseData);
        assertThat(respondentName).isEqualTo("Jones");
    }

    @Test
    void shouldReturnEmptyStringWhenNoRespondents() {
        CaseData caseData = CaseData.builder()
            .respondents1(null)
            .build();

        String respondentName = getFirstRespondentLastName(caseData);
        assertThat(respondentName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyStringWhenRespondentWithNoPartyPresent() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(Respondent.builder().build()))
            .build();

        String respondentName = getFirstRespondentLastName(caseData);
        assertThat(respondentName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyStringWhenRespondentWithNoNamePresent() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder().build()).build()))
            .build();

        String respondentName = getFirstRespondentLastName(caseData);
        assertThat(respondentName).isEqualTo("");
    }
}
