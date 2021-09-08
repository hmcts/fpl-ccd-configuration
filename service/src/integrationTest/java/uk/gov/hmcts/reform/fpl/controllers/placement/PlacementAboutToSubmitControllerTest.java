package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.PlacementDocumentType.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.PlacementDocumentType.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToSubmitControllerTest extends AbstractCallbackTest {

    private final Element<Child> child1 = testChild("Alex", "Brown");
    private final Element<Child> child2 = testChild("Emma", "Green");
    private final Document sealedDocument = testDocument();
    private final DocumentReference application = testDocumentReference("application.doc");
    private final DocumentReference sealedApplication = DocumentReference.buildFromDocument(sealedDocument);

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocumentConversionService documentConversionService;

    PlacementAboutToSubmitControllerTest() {
        super("placement");
    }

    @BeforeEach
    void init() {

        byte[] content = testDocumentBinaries();
        final byte[] converted = readBytes("documents/document.pdf");
        final byte[] sealed = readBytes("documents/document-sealed.pdf");

        when(documentDownloadService.downloadDocument(application.getBinaryUrl())).thenReturn(content);
        when(documentConversionService.convertToPdf(content, application.getFilename())).thenReturn(converted);
        when(uploadDocumentService.uploadPDF(sealed, "application.pdf")).thenReturn(sealedDocument);
    }

    @Test
    void shouldSealApplicationDocumentAndSavePlacementInConfidentialAndNonConfidentialVersions() {

        final PlacementSupportingDocument supportingDocument = PlacementSupportingDocument.builder()
            .document(testDocumentReference())
            .type(STATEMENT_OF_FACTS)
            .build();

        final PlacementConfidentialDocument confidentialDocument = PlacementConfidentialDocument.builder()
            .document(testDocumentReference())
            .type(ANNEX_B)
            .build();

        final Placement newPlacement = Placement.builder()
            .childId(child1.getId())
            .childName("Alex Brown")
            .application(application)
            .supportingDocuments(wrapElements(supportingDocument))
            .confidentialDocuments(wrapElements(confidentialDocument))
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

        final PlacementEventData actualPlacementEventData = updatedCaseData.getPlacementEventData();

        final Placement expectedNewPlacement = newPlacement.toBuilder()
            .application(sealedApplication)
            .build();

        final Placement expectedNewNonConfidentialPlacement = expectedNewPlacement.toBuilder()
            .confidentialDocuments(null)
            .build();

        assertThat(actualPlacementEventData.getPlacements())
            .extracting(Element::getValue)
            .containsExactly(existingPlacement, expectedNewPlacement);

        assertThat(actualPlacementEventData.getPlacementsNonConfidential())
            .extracting(Element::getValue)
            .containsExactly(existingPlacement, expectedNewNonConfidentialPlacement);
    }
}
