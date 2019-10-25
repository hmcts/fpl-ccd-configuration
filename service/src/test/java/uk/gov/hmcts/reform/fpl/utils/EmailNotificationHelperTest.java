package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationHelperTest {

    @Test
    void subjectLineShouldBeEmpty() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData).isNotNull();

        String subjectLine = EmailNotificationHelper.buildSubjectLine(caseData, new ArrayList<>());
        assertThat(subjectLine).isEmpty();
    }
}
