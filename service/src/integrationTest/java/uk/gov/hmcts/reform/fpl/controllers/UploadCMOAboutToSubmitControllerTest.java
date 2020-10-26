package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadCMOAboutToSubmitControllerTest extends AbstractUploadCMOControllerTest {

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
        givenLegacyFlow();

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .uploadCMOEventData(UploadCMOEventData.builder()
                .pastHearingsForCMO(dynamicList(hearings))
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        CaseManagementOrder cmo = order(hearings.get(0).getValue(), SEND_TO_JUDGE);

        List<Element<CaseManagementOrder>> uploadedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(uploadedCMOs).first().extracting(Element::getValue).isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(uploadedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldNotAlterHearingAndDraftCMOListsIfThereWereNoValidHearings() {
        givenLegacyFlow();

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().plusDays(3));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);
    }

    @Test
    void shouldAddCMOToListWithDraftStatusAndNotMigrateDocs() {
        givenNewFlow();

        List<Element<SupportingEvidenceBundle>> bundles = List.of(element(
            SupportingEvidenceBundle.builder()
                .name("case summary")
                .build()
        ));

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().plusDays(3));

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .futureHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.DRAFT)
            .cmoSupportingDocs(bundles)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(eventData)
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        CaseManagementOrder cmo = orderWithDocs(hearings.get(0).getValue(), DRAFT, bundles);

        List<Element<CaseManagementOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);

        assertThat(responseData.getHearingFurtherEvidenceDocuments()).isEmpty();
    }

    @Test
    void shouldAddCMOToListWithSendToJudgeStatusAndMigrateDocs() {
        givenNewFlow();

        List<Element<SupportingEvidenceBundle>> bundles = List.of(element(
            SupportingEvidenceBundle.builder()
                .name("case summary")
                .build()
        ));

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().minusDays(3));

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.AGREED)
            .cmoSupportingDocs(bundles)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(eventData)
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        CaseManagementOrder cmo = orderWithDocs(hearings.get(0).getValue(), SEND_TO_JUDGE, bundles);

        List<Element<CaseManagementOrder>> unsealedCMOs = responseData.getDraftUploadedCMOs();

        assertThat(unsealedCMOs)
            .hasSize(1)
            .first()
            .extracting(Element::getValue)
            .isEqualTo(cmo);

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedCMOs.get(0).getId());

        assertThat(responseData.getHearingDetails()).isEqualTo(hearings);

        List<Element<HearingFurtherEvidenceBundle>> furtherEvidenceBundle = List.of(
            element(hearings.get(0).getId(), HearingFurtherEvidenceBundle.builder()
                .hearingName(hearings.get(0).getValue().toLabel())
                .supportingEvidenceBundle(bundles)
                .build())
        );

        assertThat(responseData.getHearingFurtherEvidenceDocuments())
            .hasSize(1)
            .isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldRemoveTemporaryFields() {
        givenNewFlow();

        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(LocalDateTime.of(2020, 3, 15, 10, 7));
        List<Element<CaseManagementOrder>> draftCMOs = List.of();

        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(UploadCMOEventData.builder()
                .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
                .pastHearingsForCMO(dynamicList(hearings))
                .futureHearingsForCMO("DUMMY DATA")
                .cmoJudgeInfo("DUMMY DATA")
                .cmoHearingInfo("DUMMY DATA")
                .showReplacementCMO(YesNo.NO)
                .replacementCMO(DOCUMENT_REFERENCE)
                .previousCMO(DOCUMENT_REFERENCE)
                .cmoSupportingDocs(List.of())
                .cmoToSend(DOCUMENT_REFERENCE)
                .showCMOsSentToJudge(YesNo.NO)
                .cmosSentToJudge("DUMMY DATA")
                .cmoUploadType(CMOType.DRAFT)
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

        Set<String> keys = mapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        }).keySet();

        keys.removeAll(List.of(
            "showCMOsSentToJudge", "cmosSentToJudge", "cmoUploadType", "pastHearingsForCMO", "futureHearingsForCMO",
            "cmoHearingInfo", "showReplacementCMO", "previousCMO", "uploadedCaseManagementOrder", "replacementCMO",
            "cmoSupportingDocs", "cmoJudgeInfo", "cmoToSend",
            // Delete these ones below when cleaning up
            "numHearingsWithoutCMO", "singleHearingWithCMO", "multiHearingsWithCMOs", "showHearingsSingleTextArea",
            "showHearingsMultiTextArea"
        ));

        assertThat(response.getData().keySet()).isEqualTo(keys);
    }

    private CaseManagementOrder order(HearingBooking hearing, CMOStatus status) {
        return orderWithDocs(hearing, status, List.of());
    }

    private CaseManagementOrder orderWithDocs(HearingBooking hearing, CMOStatus status,
                                              List<Element<SupportingEvidenceBundle>> supportingDocs) {
        return CaseManagementOrder.builder()
            .status(status)
            .hearing(hearing.toLabel())
            .order(DOCUMENT_REFERENCE)
            .dateSent(dateNow())
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .supportingDocs(supportingDocs)
            .build();
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return dynamicListWithFirstSelected(
            Pair.of(hearings.get(0).getValue().toLabel(), hearings.get(0).getId()),
            Pair.of(hearings.get(1).getValue().toLabel(), hearings.get(1).getId())
        );
    }

    private List<Element<HearingBooking>> hearingsOnDateAndDayAfter(LocalDateTime startDate) {
        return List.of(
            hearing(startDate),
            hearing(startDate.plusDays(1))
        );
    }
}
