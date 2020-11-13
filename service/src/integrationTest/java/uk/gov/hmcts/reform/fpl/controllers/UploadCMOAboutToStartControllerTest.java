package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadCMOAboutToStartControllerTest extends AbstractUploadCMOControllerTest {

    protected UploadCMOAboutToStartControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldReturnMultiHearingData() {
        givenLegacyFlow();

        List<Element<HearingBooking>> hearings = List.of(
            hearing(LocalDateTime.of(2020, 3, 15, 20, 20)),
            hearing(LocalDateTime.of(2020, 3, 16, 10, 10))
        );

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(asCaseDetails(caseData)));

        Map<String, Object> dynamicList = dynamicListMap(
            "Case management hearing, 15 March 2020", hearings.get(0).getId(),
            "Case management hearing, 16 March 2020", hearings.get(1).getId()
        );

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.MULTI)
            .pastHearingsForCMO(dynamicList)
            .build();

        CaseData expectedCaseData = caseData.toBuilder().uploadCMOEventData(eventData).build();

        assertThat(responseData).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldReturnSingleHearingData() {
        givenLegacyFlow();
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(hearing(LocalDateTime.of(2020, 3, 15, 20, 20))))
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(asCaseDetails(caseData)));

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.SINGLE)
            .cmoJudgeInfo("Her Honour Judge Judy")
            .cmoHearingInfo("Send agreed CMO for Case management hearing, 15 March 2020.\n"
                + "This must have been discussed by all parties at the hearing.")
            .build();

        CaseData expectedCaseData = caseData.toBuilder().uploadCMOEventData(eventData).build();

        assertThat(responseData).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldReturnNoHearingData() {
        givenLegacyFlow();
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(hearing(now().plusDays(3))))
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(asCaseDetails(caseData)));

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.NONE)
            .build();

        CaseData expectedCaseData = caseData.toBuilder().uploadCMOEventData(eventData).build();

        assertThat(responseData).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldReturnDynamicListsAndHearingsWithSealedCMOs() {
        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder()
            .order(DocumentReference.builder().build())
            .status(CMOStatus.SEND_TO_JUDGE)
            .build());

        List<Element<HearingBooking>> hearings = List.of(
            hearing(LocalDateTime.of(2020, 3, 3, 11, 30)),
            hearing(LocalDateTime.of(3000, 3, 3, 11, 30)),
            hearingWithCMOId(LocalDateTime.of(2020, 3, 2, 11, 30), cmo.getId())
        );

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(List.of(cmo))
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(caseData));

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicListMap("Case management hearing, 3 March 2020", hearings.get(0).getId()))
            .futureHearingsForCMO(dynamicListMap("Case management hearing, 3 March 3000", hearings.get(1).getId()))
            .showCMOsSentToJudge(YesNo.YES)
            .cmosSentToJudge("Case management hearing, 2 March 2020")
            .build();

        CaseData expectedCaseData = caseData.toBuilder().uploadCMOEventData(expectedEventData).build();

        assertThat(responseData).isEqualTo(expectedCaseData);
    }

    private Map<String, Object> dynamicListMap(String label, UUID code) {
        return mapper.convertValue(dynamicListWithoutSelected(Pair.of(label, code)), new TypeReference<>() {});
    }

    private Map<String, Object> dynamicListMap(String label1, UUID code1, String label2, UUID code2) {
        return mapper.convertValue(dynamicListWithoutSelected(Pair.of(label1, code1), Pair.of(label2, code2)),
            new TypeReference<>() {});
    }

}
