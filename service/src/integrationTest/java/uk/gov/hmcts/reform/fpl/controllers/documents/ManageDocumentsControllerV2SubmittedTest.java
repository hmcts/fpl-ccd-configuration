package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@WebMvcTest(ManageDocumentsControllerV2.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerV2SubmittedTest extends AbstractCallbackTest {

    private static final long CASE_ID = 1L;

    @MockBean
    private ManageDocumentService manageDocumentService;

    @MockBean
    private EventService eventPublisher;

    @Captor
    private ArgumentCaptor<ManageDocumentsUploadedEvent> eventDataCaptor;


    ManageDocumentsControllerV2SubmittedTest() {
        super("manage-documentsv2");
    }

    @Test
    void shouldPublishManageDocumentsUploadedEvent() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of("id", CASE_ID)).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of("id", CASE_ID)).build();

        CaseData caseData = extractCaseData(caseDetails);

        UserDetails initiatedBy = UserDetails.builder().build();
        DocumentUploaderType uploadedUserType = DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocuments =
            Map.of(DocumentType.CASE_SUMMARY,
                wrapElementsWithUUIDs(CaseSummary.builder().document(mock(DocumentReference.class)).build()));
        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocumentsLA =
            Map.of(DocumentType.TOXICOLOGY_REPORT,
                wrapElementsWithUUIDs(ManagedDocument.builder().document(mock(DocumentReference.class)).build()));
        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocumentsCTSC =
            Map.of(DocumentType.COURT_BUNDLE,
                wrapElementsWithUUIDs(CourtBundle.builder().document(mock(DocumentReference.class)).build()));

        ManageDocumentsUploadedEvent expectedEventData = ManageDocumentsUploadedEvent.builder()
            .caseData(caseData)
            .initiatedBy(initiatedBy)
            .uploadedUserType(uploadedUserType)
            .newDocuments(newDocuments)
            .newDocumentsLA(newDocumentsLA)
            .newDocumentsCTSC(newDocumentsCTSC)
            .build();

        when(manageDocumentService.buildManageDocumentsUploadedEvent(any(), any())).thenReturn(expectedEventData);

        postSubmittedEvent(CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build());

        verify(eventPublisher).publishEvent(eventDataCaptor.capture());
        verifyNoMoreInteractions(eventPublisher);

        ManageDocumentsUploadedEvent capturedEventData = eventDataCaptor.getValue();
        assertEquals(capturedEventData.getCaseData(), expectedEventData.getCaseData());
        assertEquals(capturedEventData.getInitiatedBy(), expectedEventData.getInitiatedBy());
        assertEquals(capturedEventData.getUploadedUserType(), expectedEventData.getUploadedUserType());
        assertEquals(capturedEventData.getNewDocuments(), expectedEventData.getNewDocuments());
        assertEquals(capturedEventData.getNewDocumentsLA(), expectedEventData.getNewDocumentsLA());
        assertEquals(capturedEventData.getNewDocumentsCTSC(), expectedEventData.getNewDocumentsCTSC());
    }

}
