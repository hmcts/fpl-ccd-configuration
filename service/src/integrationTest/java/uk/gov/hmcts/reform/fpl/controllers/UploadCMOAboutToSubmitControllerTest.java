package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
public class UploadCMOAboutToSubmitControllerTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.builder()
        .binaryUrl("FAKE BINARY")
        .url("FAKE URL")
        .filename("FAKE FILE")
        .build();

    protected UploadCMOAboutToSubmitControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldUpdateHearingAndAppendToDraftCMOList() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(UploadCMOEventData.builder()
                .hearingsWithoutApprovedCMO(dynamicList(hearings))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        CaseManagementOrder cmo = order(hearings);

        List<Element<CaseManagementOrder>> uploadedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(uploadedCMOs).extracting("value").containsOnly(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(uploadedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldNotAlterHearingAndDraftCMOListsIfThereWereNoValidHearings() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.now().plusDays(3));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldRemoveTemporaryFields() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<CaseManagementOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(UploadCMOEventData.builder()
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .hearingsWithoutApprovedCMO(dynamicList(hearings))
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.MULTI)
                .showHearingsMultiTextArea(YesNo.YES)
                .multiHearingsWithCMOs("DUMMY DATA")
                .showHearingsSingleTextArea(YesNo.NO)
                .singleHearingWithCMO("DUMMY DATA")
                .build())
            .hearingDetails(hearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData()).doesNotContainKeys(
            "uploadedCaseManagementOrder",
            "cmoJudgeInfo",
            "cmoHearingInfo",
            "numHearingsWithoutCMO",
            "singleHearingWithCMO",
            "multiHearingsWithCMOs",
            "showHearingsSingleTextArea",
            "showHearingsMultiTextArea",
            "hearingsWithoutApprovedCMO"
        );
    }

    private CaseManagementOrder order(List<Element<HearingBooking>> hearings) {
        return CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .hearing(hearings.get(0).getValue().toLabel(DATE))
            .order(DOCUMENT_REFERENCE)
            .dateSent(dateNow())
            .judgeTitleAndName(formatJudgeTitleAndName(hearings.get(0).getValue().getJudgeAndLegalAdvisor()))
            .build();
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(hearings.get(0).getId())
                .label(hearings.get(0).getValue().toLabel(DATE))
                .build()
            ).listItems(List.of(
                DynamicListElement.builder()
                    .code(hearings.get(0).getId())
                    .label(hearings.get(0).getValue().toLabel(DATE))
                    .build(),
                DynamicListElement.builder()
                    .code(hearings.get(1).getId())
                    .label(hearings.get(1).getValue().toLabel(DATE))
                    .build()
            ))
            .build();
    }

    private List<Element<HearingBooking>> hearingsOnDateAndDayAfter(LocalDateTime startDate) {
        return List.of(
            element(hearing(startDate)),
            element(hearing(startDate.plusDays(1)))
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
