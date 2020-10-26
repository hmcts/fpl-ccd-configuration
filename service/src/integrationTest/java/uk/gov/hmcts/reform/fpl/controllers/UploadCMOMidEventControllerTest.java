package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadCMOMidEventControllerTest extends AbstractUploadCMOControllerTest {

    protected UploadCMOMidEventControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldAddJudgeAndHearingInfo() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = dynamicList(hearings);

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(UploadCMOEventData.builder()
                .pastHearingsForCMO(dynamicList)
                .build())
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

    @Test
    void shouldRemoveDocumentFromDataWhenDocumentFieldsAreNull() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = dynamicList(hearings);

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(UploadCMOEventData.builder()
                .pastHearingsForCMO(dynamicList)
                .uploadedCaseManagementOrder(DocumentReference.builder().build())
                .build())
            .hearingDetails(hearings)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData()).doesNotContainKey("uploadedCaseManagementOrder");
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return dynamicListWithFirstSelected(
            Pair.of("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            Pair.of("Case management hearing, 16 March 2020", hearings.get(1).getId())
        );
    }
}
