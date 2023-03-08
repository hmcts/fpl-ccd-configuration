package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;

@WebMvcTest(ListGatekeepingHearingController.class)
@OverrideAutoConfiguration(enabled = true)
class ListGatekeepingHearingControllerAboutToStartTest extends AbstractCallbackTest {

    ListGatekeepingHearingControllerAboutToStartTest() {
        super("list-gatekeeping-hearing");
    }

    @Test
    void shouldPopulateFlagsAndHearingListsForFirstHearingScenarioEmptyData() {

        var emptyValueMap = new LinkedHashMap<>();
        emptyValueMap.put("code", null);
        emptyValueMap.put("label", null);
        var emptyListMap = Map.of(
            "list_items", new ArrayList<>(),
            "value", emptyValueMap
        );
        CaseData caseData = CaseData.builder().build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);
        var responseDataMap = response.getData();

        assertThat(responseDataMap.get("selectedHearingId")).isNull();
        assertThat(responseDataMap.get("firstHearingFlag")).isEqualTo(YES.getValue());
        assertThat(responseDataMap.get("preHearingAttendanceDetails")).isEqualTo(DEFAULT_PRE_ATTENDANCE);
        assertThat(responseDataMap.get("hasOthers")).isNull();

        assertThat(responseDataMap.get("pastHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("futureHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("pastAndTodayHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("vacateHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("toReListHearingDateList")).isEqualTo(emptyListMap);

        assertThat(responseDataMap.get("sendNoticeOfHearing")).isEqualTo(YES.getValue());
    }
}
