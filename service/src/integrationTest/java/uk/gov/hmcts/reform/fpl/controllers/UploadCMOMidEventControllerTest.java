package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
public class UploadCMOMidEventControllerTest extends AbstractControllerTest {

    protected UploadCMOMidEventControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldAddJudgeAndHearingInfo() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = dynamicList(hearings);

        CaseData caseData = CaseData.builder()
            .pastHearingList(dynamicList)
            .hearingDetails(hearings)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData())
            .extracting("cmoHearingInfo", "cmoJudgeInfo")
            .containsOnly(
                "Case management hearing, 15 March 2020",
                "Her Honour Judge Judy"
            );
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearings.get(0).getId())
                    .label("Case management hearing, 15 March 2020")
                    .build()
                ).listItems(List.of(
                    DynamicListElement.builder()
                        .code(hearings.get(0).getId())
                        .label("Case management hearing, 15 March 2020")
                        .build(),
                    DynamicListElement.builder()
                        .code(hearings.get(1).getId())
                        .label("Case management hearing, 16 March 2020")
                        .build()
                ))
                .build();
    }

    @Test
    void shouldRegenerateDynamicListIfCCDSendsMalformedData() {
        List<Element<HearingBooking>> hearings = hearings();

        CaseData caseData = CaseData.builder()
            .pastHearingList(hearings.get(0).getId())
            .hearingDetails(hearings)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData())
            .extracting("pastHearingList")
            .isEqualTo(mapper.convertValue(dynamicList(hearings), new TypeReference<Map<String, Object>>() {}));
    }

    private List<Element<HearingBooking>> hearings() {
        return List.of(
            element(hearing(LocalDateTime.of(2020, 3, 15, 20, 20))),
            element(hearing(LocalDateTime.of(2020, 3, 16, 10, 10)))
        );
    }

    private HearingBooking hearing(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .build();
    }
}
