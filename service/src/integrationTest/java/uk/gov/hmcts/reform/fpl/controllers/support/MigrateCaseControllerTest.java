package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.time.Month.FEBRUARY;
import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla2491 {
        String familyManCaseNumber = "SN20C50018";
        String migrationId = "FPLA-2491";

        Element<HearingBooking> hearing1 = buildHearing(2020, NOVEMBER, 19, CASE_MANAGEMENT);
        Element<HearingBooking> hearing2 = buildHearing(2021, FEBRUARY, 5, FURTHER_CASE_MANAGEMENT);
        Element<HearingBooking> hearing3 = buildHearing(2021, MARCH, 4, ISSUE_RESOLUTION);
        Element<HearingBooking> hearing4 = buildHearing(2021, FEBRUARY, 5, FURTHER_CASE_MANAGEMENT);
        Element<HearingBooking> hearing5 = buildHearing(2021, MARCH, 4, ISSUE_RESOLUTION);

        @Test
        void removeCorrectHearingsFromTheCase() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, hearing5);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).containsOnly(hearing1, hearing2, hearing3);
        }

        @Test
        void shouldNotRemoveHearingsIfNotExpectedFamilyManNumber() {
            familyManCaseNumber = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, hearing5);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails())
                .containsOnly(hearing1, hearing2, hearing3, hearing4, hearing5);
        }

        @Test
        void shouldNotRemoveHearingsIfNotExpectedMigrationId() {
            migrationId = "something different";

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, hearing5);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails())
                .containsOnly(hearing1, hearing2, hearing3, hearing4, hearing5);
        }

        @Test
        void shouldThrowExceptionIfUnexpectedNumberOfHearings() {
            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Case has 3 hearing(s), expected at least 5");
        }

        @Test
        void shouldThrowExceptionIfHearing4HasUnexpectedDate() {
            LocalDate invalidDate = LocalDate.of(1990, 1, 1);

            Element<HearingBooking> unexpectedHearing4 = buildHearing(invalidDate, FURTHER_CASE_MANAGEMENT);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                unexpectedHearing4, hearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing date %s", invalidDate));
        }

        @Test
        void shouldThrowExceptionIfHearing5HasUnexpectedDate() {
            LocalDate invalidDate = LocalDate.of(1999, 1, 1);

            Element<HearingBooking> unexpectedHearing5 = buildHearing(invalidDate, FURTHER_CASE_MANAGEMENT);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, unexpectedHearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing date %s", invalidDate));
        }

        @Test
        void shouldThrowExceptionIfHearing4HasUnexpectedType() {
            HearingType invalidType = ISSUE_RESOLUTION;

            Element<HearingBooking> unexpectedHearing4 = buildHearing(2021, FEBRUARY, 5, invalidType);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                unexpectedHearing4, hearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing type %s", invalidType));
        }

        @Test
        void shouldThrowExceptionIfHearing5HasUnexpectedType() {
            HearingType invalidType = FURTHER_CASE_MANAGEMENT;

            Element<HearingBooking> unexpectedHearing5 = buildHearing(2021, MARCH, 4, invalidType);

            CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3,
                hearing4, unexpectedHearing5);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(format("Invalid hearing type %s", invalidType));
        }

        @SafeVarargs
        private CaseDetails caseDetails(String familyManCaseNumber, String migrationId,
                                        Element<HearingBooking>... hearings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .hearingDetails(List.of(hearings))
                .build());
            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private Element<HearingBooking> buildHearing(int year, Month month, int day, HearingType type) {
            return buildHearing(LocalDate.of(year, month, day), type);
        }

        private Element<HearingBooking> buildHearing(LocalDate date, HearingType type) {
            return element(HearingBooking.builder()
                .startDate(LocalDateTime.of(date, LocalTime.now()))
                .type(type)
                .build());
        }
    }

    @Nested
    class Fpla2501 {

        final String migrationId = "FPLA-2501";
        final String caseName = "test name";

        @Test
        void shouldRemoveLegacyFields() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "caseName", caseName,
                    "respondents", List.of(element(Respondent.builder()
                        .party(RespondentParty.builder().lastName("Wilson").build())
                        .build())),
                    "children", List.of(element(Child.builder()
                        .party(ChildParty.builder().lastName("Smith").build())
                        .build())),
                    "applicant", List.of(element(Applicant.builder()
                        .party(ApplicantParty.builder().lastName("White").build())
                        .build())),
                    "migrationId", migrationId))
                .build();

            Map<String, Object> extractedCaseData = postAboutToSubmitEvent(caseDetails).getData();

            assertThat(extractedCaseData).isEqualTo(Map.of("caseName", caseName));
        }

        @Test
        void shouldRemoveMigrationIdOnlyIfRespondentsAndChildrenFiledNotPresent() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(Map.of(
                    "caseName", caseName,
                    "migrationId", migrationId))
                .build();

            Map<String, Object> extractedCaseData = postAboutToSubmitEvent(caseDetails).getData();

            assertThat(extractedCaseData).isEqualTo(Map.of("caseName", caseName));
        }
    }
}
