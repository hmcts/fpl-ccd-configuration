package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementChildSelectionMidEventTest extends AbstractPlacementControllerTest {
    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        given(userService.isCtscUser()).willReturn(false);
    }

    @Test
    void shouldPrepareNewPlacementForSelectedChild() {

        final DynamicList childrenList = dynamicLists.from(1,
            of("Alex Brown", child1.getId()),
            of("George White", child2.getId()));

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placementChildrenList(childrenList)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "child-selection"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        final Placement expectedPlacement = Placement.builder()
            .childId(child2.getId())
            .childName("George White")
            .confidentialDocuments(wrapElements(defaultAnnexB))
            .supportingDocuments(wrapElements(defaultBirthCertificate, defaultStatementOfFacts))
            .build();

        assertThat(actualPlacementData.getPlacementChildName()).isEqualTo("George White");
        assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
    }

    @Test
    void shouldPrepareExistingPlacementForSelectedChild() {

        final PlacementNoticeDocument localAuthorityNotice = PlacementNoticeDocument.builder()
            .type(LOCAL_AUTHORITY)
            .build();

        final Placement existingPlacement = Placement.builder()
            .application(testDocumentReference())
            .childId(child1.getId())
            .childName("Alex Brown")
            .placementUploadDateTime(LocalDateTime.now())
            .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
            .confidentialDocuments(wrapElements(annexB))
            .noticeDocuments(wrapElements(localAuthorityNotice))
            .build();

        final DynamicList childrenList = dynamicLists.from(1,
            of("Alex Brown", child1.getId()),
            of("George White", child2.getId()));

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1))
            .respondents1(List.of(mother, father))
            .placementEventData(PlacementEventData.builder()
                .placementChildrenList(childrenList)
                .placements(wrapElements(existingPlacement))
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementChildName())
            .isEqualTo("Alex Brown");

        assertThat(actualPlacementData.getPlacement())
            .isEqualTo(existingPlacement);
    }
}
