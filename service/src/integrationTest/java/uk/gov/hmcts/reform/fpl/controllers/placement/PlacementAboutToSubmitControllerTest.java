package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToSubmitControllerTest extends AbstractPlacementControllerTest {

    private final Document sealedDocument = testDocument();
    private final DocumentReference application = testDocumentReference("application.doc");

    private final DocumentReference placementNoticeDocument = testDocumentReference("placementNotice.pdf");

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
            .placementNotice(placementNoticeDocument)
            .noticeDocuments(noticeResponses)
            .build();

        final Placement existingPlacement = Placement.builder()
            .childId(child2.getId())
            .childName("Emma Green")
            .application(testDocumentReference())
            .placementNotice(placementNoticeDocument)
            .build();

        final DynamicList pbaNumberList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code("PBA1234567")
                .build())
            .build();

        final PBAPayment placementPayment = PBAPayment.builder()
            .pbaNumberDynamicList(pbaNumberList)
            .clientCode("code")
            .fileReference("reference")
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placement(newPlacement)
                .placements(wrapElements(existingPlacement))
                .placementPayment(placementPayment)
                .build())
            .isCTSCUser(NO)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        final Placement expectedNewPlacement = newPlacement.toBuilder()
            .application(application)
            .noticeDocuments(noticeResponses)
            .placementUploadDateTime(now())
            .placementRespondentsToNotify(Collections.emptyList())
            .build();

        final Placement expectedNewNonConfidentialPlacement = expectedNewPlacement.toBuilder()
            .confidentialDocuments(null)
            .placementNotice(placementNoticeDocument)
            .build();

        assertThat(actualPlacementData.getPlacements())
            .extracting(Element::getValue)
            .containsExactly(existingPlacement, expectedNewPlacement);

        assertThat(actualPlacementData.getPlacementsNonConfidentialWithNotices(true))
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
            .placementRespondentsToNotify(Collections.emptyList())
            .placementNotice(placementNoticeDocument)
            .build();

        final Placement existingApplicationForChild2 = Placement.builder()
            .childId(child2.getId())
            .childName("Emma Green")
            .application(testDocumentReference())
            .confidentialDocuments(wrapElements(PlacementConfidentialDocument.builder()
                .type(ANNEX_B)
                .document(testDocumentReference())
                .build()))
            .placementRespondentsToNotify(Collections.emptyList())
            .placementNotice(placementNoticeDocument)
            .build();

        final Placement newPlacementForChild1 = existingApplicationForChild1.toBuilder()
            .supportingDocuments(wrapElements(statementOfFacts))
            .confidentialDocuments(wrapElements(annexB))
            .noticeDocuments(noticeResponses)
            .placementNotice(placementNoticeDocument)
            .build();

        final DynamicList pbaNumberList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code("PBA1234567")
                .build())
            .build();

        final PBAPayment placementPayment = PBAPayment.builder()
            .pbaNumberDynamicList(pbaNumberList)
            .clientCode("code")
            .fileReference("reference")
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placement(newPlacementForChild1)
                .placements(wrapElements(existingApplicationForChild1, existingApplicationForChild2))
                .placementPayment(placementPayment)
                .build())
            .isCTSCUser(NO)
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
            .placementNotice(placementNoticeDocument)
            .build();

        assertThat(actualPlacementData.getPlacements())
            .extracting(Element::getValue)
            .containsExactly(expectedNewPlacementForChild1, existingApplicationForChild2);

        assertThat(actualPlacementData.getPlacementsNonConfidentialWithNotices(true))
            .extracting(Element::getValue)
            .containsExactly(expectedNewNonConfidentialPlacementForChild1, expectedNonConfidentialPlacementForChild2);
    }
}
