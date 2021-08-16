package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GatekeepingOrderDataFixerTest {

    private final GatekeepingOrderDataFixer underTest = new GatekeepingOrderDataFixer();

    @Test
    void testIfLanguageNotPresent() {
        CaseDetailsMap actual = underTest.fix(CaseDetailsMap.caseDetailsMap(Map.of("some", "rubbish")));

        assertThat(actual).isEqualTo(Map.of("some", "rubbish"));
    }

    @Test
    void testIfLanguagePresent() {
        CaseDetailsMap actual = underTest.fix(CaseDetailsMap.caseDetailsMap(Map.of(
            "some", "rubbish",
            "languageRequirement", "Yes"
        )));

        assertThat(actual).isEqualTo(Map.of(
            "some", "rubbish",
            "languageRequirement", "Yes",
            "languageRequirementUrgent", "Yes"
        ));
    }

    @Test
    void testIfLanguagePresentAndOverride() {
        CaseDetailsMap actual = underTest.fix(CaseDetailsMap.caseDetailsMap(Map.of(
            "some", "rubbish",
            "languageRequirement", "No",
            "languageRequirementUrgent", "Yes"
        )));

        assertThat(actual).isEqualTo(Map.of(
            "some", "rubbish",
            "languageRequirement", "No",
            "languageRequirementUrgent", "No"
        ));
    }
}
