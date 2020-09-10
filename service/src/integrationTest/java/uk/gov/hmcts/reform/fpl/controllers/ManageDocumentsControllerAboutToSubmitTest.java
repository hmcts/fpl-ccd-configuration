package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsControllerAboutToSubmitTest extends AbstractControllerTest {

    ManageDocumentsControllerAboutToSubmitTest() {
        super("manage-documents");
    }

    @Test
    void shouldPopulateHearingFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        Map<String, Object> data = new HashMap<>(Map.of(
            "hearingDetails", List.of(element(hearingId, hearingBooking)),
            "manageDocumentsHearingList", DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build(),
            TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue())));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getHearingFurtherEvidenceDocuments().get(0).getValue()
            .getSupportingEvidenceBundle()).isEqualTo(furtherEvidenceBundle);

        assertThat(caseData.getFurtherEvidenceDocumentsTEMP()).isEmpty();
    }

    @Test
    void shouldPopulateFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue())));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getFurtherEvidenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertThat(caseData.getFurtherEvidenceDocumentsTEMP()).isEmpty();
        assertThat(caseData.getManageDocument()).isNull();
    }

    @Test
    void shouldPopulateCorrespondenceEvidenceCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        Map<String, Object> data = new HashMap<>(Map.of(
            CORRESPONDING_DOCUMENTS_COLLECTION_KEY, furtherEvidenceBundle,
            MANAGE_DOCUMENT_KEY, buildManagementDocument(CORRESPONDENCE)));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(buildCaseDetails(Map.of()))
            .caseDetails(buildCaseDetails(data))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCorrespondenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertThat(caseData.getManageDocument()).isNull();
    }

    @Test
    void shouldPopulateC2DocumentBundleCollection() {
        // TODO
        // Test needed to assert c2 document bundle is built
        // Should just be able to re-use the majority of what is done in the equiv service test
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder().data(caseData).build();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(LocalDateTime.now())
            .build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type) {
        return ManageDocument.builder().type(type).build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(LocalDateTime.now())
            .name("test")
            .build());
    }
}
