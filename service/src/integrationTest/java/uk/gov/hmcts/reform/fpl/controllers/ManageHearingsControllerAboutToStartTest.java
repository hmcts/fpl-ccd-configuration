package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerAboutToStartTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    ManageHearingsControllerAboutToStartTest() {
        super("manage-hearings");
    }

    @Test
    void shouldSetFirstHearingFlagWhenHearingsEmpty() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getFirstHearingFlag()).isEqualTo("Yes");
        assertThat(caseData.getHasExistingHearings()).isNull();
    }

    @Test
    void shouldSetJudgeWhenAllocatedJudgePresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("allocatedJudge", Judge.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Richards")
                .judgeEmailAddress("richards@example.com")
                .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel("Case assigned to: His Honour Judge Richards").build());
    }

    @Test
    void shouldSetHearingDateListOfFutureHearingsAndExistingHearingsFlagWhenHearingsPresent() {
        List<Element<HearingBooking>> hearings = hearings();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingDetails", hearings))
            .build();

        DynamicList dynamicList = dynamicList(hearings);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getFirstHearingFlag()).isNull();
        assertThat(caseData.getHearingDateList()).isEqualTo(
            mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {}));
        assertThat(caseData.getHasExistingHearings()).isEqualTo("Yes");
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return DynamicList.builder()
            .value(DynamicListElement.builder().build())
            .listItems(List.of(
                DynamicListElement.builder()
                    .code(hearings.get(0).getId())
                    .label("Case management hearing, 15 March 2099")
                    .build()
            ))
            .build();
    }

    private List<Element<HearingBooking>> hearings() {
        return List.of(
            element(hearing(LocalDateTime.of(2099, 3, 15, 20, 20))),
            element(hearing(LocalDateTime.of(2020, 1, 1, 10, 10)))
        );
    }

    private HearingBooking hearing(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .build();
    }
}
