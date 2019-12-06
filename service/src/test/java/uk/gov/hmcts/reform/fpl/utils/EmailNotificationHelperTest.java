package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

class EmailNotificationHelperTest {

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
        List<Element<Respondent>> respondents =  ImmutableList.of(
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
    void subjectLineShouldReturnFirstNameFamilyManCaseNumberWithHearingDateSuffixed() {
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .hearingDetails(createHearingBookings(LocalDateTime.now().plusMonths(10)))
            .familyManCaseNumber("FamilyManCaseNumber")
            .build();

        String expectedSubjectLineWithHearingDateSuffix = "Jones, FamilyManCaseNumber, hearing 6 Oct 2020";
        String subjectLine = buildSubjectLine(caseData);
        String subjectLineWithHearingDateSuffix = buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
            caseData.getHearingDetails());
        assertThat(subjectLineWithHearingDateSuffix).isEqualTo(expectedSubjectLineWithHearingDateSuffix);
    }

    @Test
    void subjectLineShouldReturnFirstNameFamilyManCaseNumberWithNoHearingDateSuffixed() {
        CaseData caseData = CaseData.builder()
            .respondents1(createRespondents())
            .hearingDetails(null)
            .familyManCaseNumber("FamilyManCaseNumber")
            .build();

        String expectedSubjectLineWithHearingDateSuffix = "Jones, FamilyManCaseNumber";
        String subjectLine = buildSubjectLine(caseData);
        String subjectLineWithHearingDateSuffix = buildSubjectLineWithHearingBookingDateSuffix(subjectLine,
            caseData.getHearingDetails());
        assertThat(subjectLineWithHearingDateSuffix).isEqualTo(expectedSubjectLineWithHearingDateSuffix);
    }
}
