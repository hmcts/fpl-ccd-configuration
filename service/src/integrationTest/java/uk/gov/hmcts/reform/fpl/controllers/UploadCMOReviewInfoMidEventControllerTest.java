package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadCMOReviewInfoMidEventControllerTest extends AbstractUploadCMOControllerTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();

    protected UploadCMOReviewInfoMidEventControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldAddJudgeAndUploadedDocument() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList pastHearingList = dynamicListWithFirstSelected(
            Pair.of("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            Pair.of("Case management hearing, 16 March 2020", hearings.get(1).getId())
        );
        Map<String, Object> pastHearingListAsMap = mapper.convertValue(pastHearingList, new TypeReference<>() {});

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(UploadCMOEventData.builder()
                .pastHearingsForCMO(pastHearingList)
                .uploadedCaseManagementOrder(DOCUMENT)
                .build())
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "review-info"));

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .cmoToSend(DOCUMENT)
            .cmoJudgeInfo("Her Honour Judge Judy")
            // These 2, whilst not set in the controller, are populated due to having been populated at the start of
            // the test
            .uploadedCaseManagementOrder(DOCUMENT)
            .pastHearingsForCMO(pastHearingListAsMap)
            .build();

        assertThat(responseData.getUploadCMOEventData()).isEqualTo(expectedEventData);
    }
}
