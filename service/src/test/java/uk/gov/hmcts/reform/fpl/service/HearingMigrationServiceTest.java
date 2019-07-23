package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HearingMigrationServiceTest {

    @InjectMocks
    private HearingMigrationService classUnderTest;

    @Test
    void shouldSetMigratedHearingToYesWhenNoHearingDataPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = classUnderTest.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("hearingMigrated", "Yes");
    }

    @Test
    void shouldSetMigratedHearingToYesWhenHearing1Exists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("hearing1", "some value"))
            .build();
        AboutToStartOrSubmitCallbackResponse response = classUnderTest.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("hearingMigrated", "Yes");
    }

    @Test
    void shouldSetMigratedHearingToNoWhenMigratedHearingExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("hearing", "some value"))
            .build();
        AboutToStartOrSubmitCallbackResponse response = classUnderTest.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("hearingMigrated", "No");
    }

    @Test
    void shouldNotAddHearingToCaseDataWhenNoHearingDataPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse actual = classUnderTest.addHiddenValues(caseDetails);
        assertThat(actual.getData()).doesNotContainKey("hearing1");
    }

    @Test
    void shouldAddHearingWithHiddenFieldsToCaseDataWhenHearingDataPresent() {
        Hearing hearing = createHearing();
        Map<String, Object> caseData = createData("data", "some data");
        caseData.put("hearing1", hearing);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse actual = classUnderTest.addHiddenValues(caseDetails);

        assertThat(actual.getData()).containsKey("hearing1");

        Hearing actualHearingAddedToCaseData = (Hearing) actual.getData().get("hearing1");

        assertThat(actualHearingAddedToCaseData.getHearingID()).isNotNull();
        assertThat(actualHearingAddedToCaseData.getHearingDescription()).isEqualTo("this is a hearing description");
        assertThat(actualHearingAddedToCaseData.getHearingDate().length()).isEqualTo(10);
        assertThat(actualHearingAddedToCaseData.getReason()).isEqualTo("hearing reason");
        assertThat(actualHearingAddedToCaseData.getTimeFrame()).isEqualTo("hearing timeframe");
        assertThat(actualHearingAddedToCaseData.getSameDayHearingReason()).isEqualTo("hearing same day hearing reason");
        assertThat(actualHearingAddedToCaseData.getTwoDayHearingReason()).isNull();
        assertThat(actualHearingAddedToCaseData.getSevenDayHearingReason()).isNull();
        assertThat(actualHearingAddedToCaseData.getTwelveDayHearingReason()).isNull();
        assertThat(actualHearingAddedToCaseData.getWithoutNotice()).isEqualTo("hearing without notice");
        assertThat(actualHearingAddedToCaseData.getReasonForNoNotice()).isEqualTo("hearing reason for no notice");
        assertThat(actualHearingAddedToCaseData.getReducedNotice()).isEqualTo("hearing reduced notice");
        assertThat(actualHearingAddedToCaseData.getReasonForReducedNotice())
            .isEqualTo("hearing reason for reduced notice");
        assertThat(actualHearingAddedToCaseData.getRespondentsAware())
            .isEqualTo("hearing respondents aware");
        assertThat(actualHearingAddedToCaseData.getReasonsForRespondentsNotBeingAware())
            .isEqualTo("hearing reasons for respondents not being aware");
        assertThat(actualHearingAddedToCaseData.getCreatedBy()).isBlank();
        assertThat(actualHearingAddedToCaseData.getCreatedDate().length()).isEqualTo(10);
        assertThat(actualHearingAddedToCaseData.getUpdatedBy()).isNull();
        assertThat(actualHearingAddedToCaseData.getUpdatedOn()).isNull();
    }

    private Hearing createHearing() {
        return Hearing.builder()
            .hearingDescription("this is a hearing description")
            .hearingDate("2007-12-03T10:15:30")
            .reason("hearing reason")
            .timeFrame("hearing timeframe")
            .sameDayHearingReason("hearing same day hearing reason")
            .withoutNotice("hearing without notice")
            .reasonForNoNotice("hearing reason for no notice")
            .reducedNotice("hearing reduced notice")
            .reasonForReducedNotice("hearing reason for reduced notice")
            .respondentsAware("hearing respondents aware")
            .reasonsForRespondentsNotBeingAware("hearing reasons for respondents not being aware")
            .build();
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return data;
    }
}
