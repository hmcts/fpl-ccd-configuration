package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToSubmitControllerTest extends AbstractPlacementControllerTest {

    private final Document sealedDocument = testDocument();
    private final DocumentReference sealedApplication = buildFromDocument(sealedDocument);
    private final DocumentReference application = testDocumentReference("application.doc");

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocumentConversionService documentConversionService;

    @BeforeEach
    void init() {

        final byte[] applicationContent = testDocumentBinaries();
        final byte[] applicationContentAsPdf = readBytes("documents/document.pdf");
        final byte[] sealedApplicationContent = readBytes("documents/document-sealed.pdf");

        when(documentDownloadService.downloadDocument(application.getBinaryUrl()))
            .thenReturn(applicationContent);

        when(documentConversionService.convertToPdf(applicationContent, application.getFilename()))
            .thenReturn(applicationContentAsPdf);

        when(uploadDocumentService.uploadPDF(sealedApplicationContent, "application.pdf"))
            .thenReturn(sealedDocument);
    }

    @Test
    void shouldSaveNewPlacementApplication() {

        final List<Element<PlacementNoticeDocument>> noticeResponses = wrapElements(PlacementNoticeDocument.builder()
            .type(LOCAL_AUTHORITY)
            .recipientName("Local authority")
            .response(testDocumentReference())
            .build());

        final Placement newPlacement = Placement.builder()
            .childId(child1.getId())
            .childName("Alex Brown")
            .application(application)
            .placementUploadDateTime(now())
            .supportingDocuments(wrapElements(statementOfFacts))
            .confidentialDocuments(wrapElements(annexB))
            .noticeDocuments(noticeResponses)
            .build();

        final Placement existingPlacement = Placement.builder()
            .childId(child2.getId())
            .childName("Emma Green")
            .application(testDocumentReference())
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placement(newPlacement)
                .placements(wrapElements(existingPlacement))
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        final Placement expectedNewPlacement = newPlacement.toBuilder()
            .application(sealedApplication)
            .noticeDocuments(noticeResponses)
            .placementUploadDateTime(now())
            .build();

        final Placement expectedNewNonConfidentialPlacement = expectedNewPlacement.toBuilder()
            .confidentialDocuments(null)
            .build();

        assertThat(actualPlacementData.getPlacements())
            .extracting(Element::getValue)
            .containsExactly(existingPlacement, expectedNewPlacement);

        assertThat(actualPlacementData.getPlacementsNonConfidential(true))
            .extracting(Element::getValue)
            .containsExactly(existingPlacement, expectedNewNonConfidentialPlacement);
    }

    @Test
    void shouldUpdateExistingPlacement() {

        final List<Element<PlacementNoticeDocument>> noticeResponses = wrapElements(PlacementNoticeDocument.builder()
            .type(LOCAL_AUTHORITY)
            .recipientName("Local authority")
            .response(testDocumentReference())
            .build());


        final Placement existingApplicationForChild1 = Placement.builder()
            .childId(child1.getId())
            .childName("Alex Brown")
            .application(testDocumentReference())
            .placementUploadDateTime(LocalDateTime.of(2020, 10, 10, 12, 0))
            .build();

        final Placement existingApplicationForChild2 = Placement.builder()
            .childId(child2.getId())
            .childName("Emma Green")
            .application(testDocumentReference())
            .confidentialDocuments(wrapElements(PlacementConfidentialDocument.builder()
                .type(ANNEX_B)
                .document(testDocumentReference())
                .build()))
            .build();

        final Placement newPlacementForChild1 = existingApplicationForChild1.toBuilder()
            .supportingDocuments(wrapElements(statementOfFacts))
            .confidentialDocuments(wrapElements(annexB))
            .noticeDocuments(noticeResponses)
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placement(newPlacementForChild1)
                .placements(wrapElements(existingApplicationForChild1, existingApplicationForChild2))
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        final Placement expectedNewPlacementForChild1 = newPlacementForChild1.toBuilder()
            .noticeDocuments(noticeResponses)
            .build();

        final Placement expectedNewNonConfidentialPlacementForChild1 = expectedNewPlacementForChild1.toBuilder()
            .confidentialDocuments(null)
            .build();

        final Placement expectedNonConfidentialPlacementForChild2 = existingApplicationForChild2.toBuilder()
            .confidentialDocuments(null)
            .build();

        assertThat(actualPlacementData.getPlacements())
            .extracting(Element::getValue)
            .containsExactly(expectedNewPlacementForChild1, existingApplicationForChild2);

        assertThat(actualPlacementData.getPlacementsNonConfidential(true))
            .extracting(Element::getValue)
            .containsExactly(expectedNewNonConfidentialPlacementForChild1, expectedNonConfidentialPlacementForChild2);
    }
}
