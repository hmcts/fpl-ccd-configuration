package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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
        List<Element<HearingBooking>> hearings = hearings(LocalDateTime.of(2020, 3, 15, 10, 7));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .pastHearingList(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
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
    void shouldUpdateHearingListAndUpdateDraftCMOListIfCMOHasBeenReturned() {
        List<Element<HearingBooking>> hearings = hearings(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<CaseManagementOrder>> orders = List.of(
            element(CaseManagementOrder.builder()
                .status(RETURNED)
                .build()),
            element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
        );

        hearings.get(0).getValue().setCaseManagementOrderId(orders.get(0).getId());

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(orders)
            .pastHearingList(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        CaseManagementOrder cmo = order(hearings);

        List<Element<CaseManagementOrder>> uploadedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(uploadedCMOs).hasSize(2).first().extracting("value").isEqualTo(cmo);
        assertThat(uploadedCMOs.get(1)).isEqualTo(orders.get(1));

        hearings.get(0).getValue().setCaseManagementOrderId(uploadedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldNotAlterHearingAndDraftCMOListsIfThereWereNoValidHearings() {
        List<Element<HearingBooking>> hearings = hearings(LocalDateTime.now().plusDays(3));
        List<Element<CaseManagementOrder>> draftCMOs = List.of(element(CaseManagementOrder.builder().build()));

        CaseData caseData = CaseData.builder()
            .pastHearingList(dynamicList(hearings))
            .hearingDetails(hearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getDraftUploadedCMOs()).isEqualTo(draftCMOs);
        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldRemoveTemporaryFields() {
        List<Element<HearingBooking>> hearings = hearings(LocalDateTime.now().plusDays(3));
        List<Element<CaseManagementOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .pastHearingList(dynamicList(hearings))
            .hearingDetails(hearings)
            .draftUploadedCMOs(draftCMOs)
            .build();

        HashMap<String, Object> data = mapper.convertValue(caseData, new TypeReference<>() {});

        data.putAll(Map.of(
            "uploadedCaseManagementOrder", DocumentReference.builder().build(),
            "cmoJudgeInfo", "DUMMY DATA",
            "cmoHearingInfo", "DUMMY DATA",
            "numHearings", "DUMMY DATA",
            "singleHearingsWithCMOs", "DUMMY DATA",
            "multiHearingsWithCMOs", "DUMMY DATA",
            "showHearingsSingleTextArea", "DUMMY DATA",
            "showHearingsMultiTextArea", "DUMMY DATA"
        ));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "uploadedCaseManagementOrder",
            "cmoJudgeInfo",
            "cmoHearingInfo",
            "numHearings",
            "singleHearingsWithCMOs",
            "multiHearingsWithCMOs",
            "showHearingsSingleTextArea",
            "showHearingsMultiTextArea"
        );
    }

    private CaseManagementOrder order(List<Element<HearingBooking>> hearings) {
        return CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .hearing(hearings.get(0).getValue().toLabel(DATE))
            .order(DOCUMENT_REFERENCE)
            .dateSent(dateNow())
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

    private List<Element<HearingBooking>> hearings(LocalDateTime startDate) {
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
