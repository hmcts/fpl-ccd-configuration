package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
public class MigrationControllerTest extends AbstractControllerTest {

    private static final LocalTime TIME = LocalTime.now();

    private Element<HearingBooking> hearing1;
    private Element<HearingBooking> hearing2;
    private Element<HearingBooking> hearing3;
    private Element<HearingBooking> hearing4;
    private String familyManCaseNumber;
    private String migrationId;

    protected MigrationControllerTest() {
        super("migrate-case");
    }

    @BeforeEach
    void setCaseIdentifiers() {
        familyManCaseNumber = "ZW20C50003";
        migrationId = "FPLA-2437";

        hearing1 = element(buildHearing());
        hearing2 = element(buildHearing());
        hearing3 = element(buildHearing());
        hearing4 = element(buildHearing());
    }

    @Test
    void removeCorrectHearingsFromTheCase() {
        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3, hearing4);

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

        assertThat(extractedCaseData.getHearingDetails()).hasSize(2)
            .containsOnly(hearing2, hearing4);
    }

    @Test
    void shouldNotRemoveHearingsIfNotExpectedFamilyManNumber() {
        familyManCaseNumber = "something different";

        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3, hearing4);

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

        assertThat(extractedCaseData.getHearingDetails()).hasSize(4)
            .containsOnly(hearing1, hearing2, hearing3, hearing4);
    }

    @Test
    void shouldNotRemoveHearingsIfNotExpectedMigrationId() {
        migrationId = "something different";

        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3, hearing4);

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

        assertThat(extractedCaseData.getHearingDetails()).hasSize(4)
            .containsOnly(hearing1, hearing2, hearing3, hearing4);
    }

    @Test
    void shouldThrowExceptionIfOneOfTheDatesIsUnexpected() {
        LocalDate invalidDate = LocalDate.of(1990, 1, 1);
        hearing2 = element(buildHearing(invalidDate));

        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, hearing1, hearing2, hearing3, hearing4);

        assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
            .getRootCause()
            .hasMessage(String.format("Invalid hearing date %s", LocalDateTime.of(invalidDate, TIME)));
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

    private HearingBooking buildHearing() {
        return buildHearing(LocalDate.of(2020, 11, 10));
    }

    private HearingBooking buildHearing(LocalDate date) {
        return HearingBooking.builder()
            .startDate(LocalDateTime.of(date, TIME))
            .build();
    }
}
