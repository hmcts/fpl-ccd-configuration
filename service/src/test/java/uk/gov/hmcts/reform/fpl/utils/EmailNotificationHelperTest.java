package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationHelperTest {

    @Test
    void subjectLineShouldBeEmptyWhenNoRespondentOrCaseNumberEmpty() {
        String subjectLine = EmailNotificationHelper.buildSubjectLine(CaseData.builder().build());
        assertThat(subjectLine).isEmpty();
    }

    @Test
    void subjectLineShouldMatchWhenRespondentOrCaseNumberGiven() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("FamilyManCaseNumber")
            .respondents1(CaseDataGeneratorHelper.createRespondents())
            .build();

        String expectedSubjectLine = "Jones, FamilyManCaseNumber";
        String subjectLine = EmailNotificationHelper.buildSubjectLine(caseData);
        assertThat(subjectLine).isEqualTo(expectedSubjectLine);
    }
}
