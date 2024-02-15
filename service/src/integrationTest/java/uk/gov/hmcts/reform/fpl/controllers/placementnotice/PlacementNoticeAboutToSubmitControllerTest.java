package uk.gov.hmcts.reform.fpl.controllers.placementnotice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.PlacementNoticeController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@WebMvcTest(PlacementNoticeController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementNoticeAboutToSubmitControllerTest extends AbstractPlacementNoticeControllerTest {

    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldSavePlacementNotice() {

        when(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any(), any()))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(uploadDocumentService.uploadDocument(any(), any(), any())).thenReturn(testDocument());

        final Element<Placement> placement = element(Placement.builder()
            .childId(child1.getId())
            .placementRespondentsToNotify(newArrayList(father))
            .build());

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placement(placement.getValue())
            .placements(newArrayList(placement))
            .placementNoticeVenue("96")
            .placementNoticeDateTime(LocalDateTime.now())
            .placementNoticeDuration("1")
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .court(Court.builder().name("Test Court").build())
            .respondents1(List.of(mother, father))
            .placementEventData(placementEventData)
            .placementList(
                asDynamicList(placementEventData.getPlacements(), placement.getId(), Placement::getChildName))
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacement()).isNull(); // should be null, it's a temp field!
        assertThat(actualPlacementData.getPlacements().size()).isEqualTo(1);
        assertThat(actualPlacementData.getPlacements().get(0).getValue().getPlacementNotice()).isNotNull();
    }


}
