package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class ActionCaseManagementOrderControllerMidEventTest extends AbstractControllerTest {

    private static final byte[] PDF = {1, 2, 3, 4, 5};

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    ActionCaseManagementOrderControllerMidEventTest() {
        super("action-cmo");
    }

    @BeforeEach
    void setUp() {
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", PDF);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());
    }

    @Test
    void midEventShouldAddDocumentReferenceToOrderAction() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(buildCaseDetails());

        verify(uploadDocumentService).uploadPDF(PDF, "draft-case-management-order.pdf");

        DocumentReference documentReference = getDocumentReference(callbackResponse);

        assertThat(documentReference).isEqualTo(
            DocumentReference.builder()
                .binaryUrl(document().links.binary.href)
                .filename(document().originalDocumentName)
                .url(document().links.self.href)
                .build());
    }

    private CaseDetails buildCaseDetails() {
        return CaseDetails.builder()
            .id(12345L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(emptyMap())
            .build();
    }

    private DocumentReference getDocumentReference(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> responseCaseData = callbackResponse.getData();

        return mapper.convertValue(responseCaseData.get(ORDER_ACTION.getKey()), OrderAction.class).getDocument();
    }

}
