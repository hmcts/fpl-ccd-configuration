package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ListGatekeepingHearingController.class)
@OverrideAutoConfiguration(enabled = true)
class ListGatekeepingHearingControllerAboutToStartTest extends AbstractCallbackTest {

    ListGatekeepingHearingControllerAboutToStartTest() {
        super("list-gatekeeping-hearing");
    }

    @Test
    void shouldSetFlagsForFirstHearingScenarioEmptyData() {

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

    @Test
    void shouldSetFlagsForSubsequentHearingsScenario() {

        var emptyValueMap = new LinkedHashMap<>();
        emptyValueMap.put("code", null);
        emptyValueMap.put("label", null);
        var emptyListMap = Map.of(
            "list_items", new ArrayList<>(),
            "value", emptyValueMap
        );
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(testHearing(LocalDateTime.now())))
            .build();

        var responseDataMap = postAboutToStartEvent(caseData).getData();

        assertThat(responseDataMap.get("selectedHearingId")).isNull();
        assertThat(responseDataMap.get("firstHearingFlag")).isEqualTo(NO.getValue());

        assertThat(responseDataMap.get("pastHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("futureHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("pastAndTodayHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("vacateHearingDateList")).isEqualTo(emptyListMap);
        assertThat(responseDataMap.get("toReListHearingDateList")).isEqualTo(emptyListMap);

        assertThat(responseDataMap.get("sendNoticeOfHearing")).isEqualTo(YES.getValue());
    }

    @Test
    void shouldPopulateFlagsAndHearingLists() {

        var emptyListMap = hearingListMap(List.of());
        var pastHearing = element(testHearing(LocalDateTime.now().minusDays(2)));
        var futureHearing = element(testHearing(LocalDateTime.now().plusDays(2)));
        var pastListMap = hearingListMap(List.of(pastHearing));
        var futureListMap = hearingListMap(List.of(futureHearing));
        var vacateListMap = hearingListMap(List.of(futureHearing, pastHearing));


        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(pastHearing, futureHearing))
            .build();

        var response = postAboutToStartEvent(caseData);
        var responseDataMap = response.getData();

        assertThat(responseDataMap.get("selectedHearingId")).isNull();
        assertThat(responseDataMap.get("firstHearingFlag")).isEqualTo(NO.getValue());

        assertThat(responseDataMap.get("pastHearingDateList")).isEqualTo(pastListMap);
        assertThat(responseDataMap.get("futureHearingDateList")).isEqualTo(futureListMap);
        assertThat(responseDataMap.get("pastAndTodayHearingDateList")).isEqualTo(pastListMap);
        assertThat(responseDataMap.get("vacateHearingDateList")).isEqualTo(vacateListMap);
        assertThat(responseDataMap.get("toReListHearingDateList")).isEqualTo(emptyListMap);

        assertThat(responseDataMap.get("sendNoticeOfHearing")).isEqualTo(YES.getValue());
        assertThat(responseDataMap.get("hearingOption")).isEqualTo(NEW_HEARING.toString());

    }

    private Map<String, Object> hearingListMap(List<Element<HearingBooking>> hearings) {
        var emptyValueMap = new LinkedHashMap<>();
        emptyValueMap.put("code", null);
        emptyValueMap.put("label", null);

        var hearingList = hearings.stream()
            .map(element -> Map.of("code", element.getId().toString(), "label", element.getValue().toLabel()))
            .collect(Collectors.toList());

        return Map.of("list_items", hearingList, "value", emptyValueMap);
    }

    private HearingBooking testHearing(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .status(null)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .endDateDerived("No")
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel("")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .others(emptyList())
            .venueCustomAddress(Address.builder().build())
            .caseManagementOrderId(null)
            .venue("96")
            .attendance(List.of(IN_PERSON))
            .othersNotified("")
            .build();
    }
}
