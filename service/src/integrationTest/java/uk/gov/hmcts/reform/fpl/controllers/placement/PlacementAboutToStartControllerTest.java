package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.assertions.DynamicListAssert.assertThatDynamicList;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToStartControllerTest extends AbstractPlacementControllerTest {

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        given(userService.isCtscUser()).willReturn(false);
    }

    @Test
    void shouldPreparePlacementWhenMultipleChildren() {

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .respondents1(List.of(mother, father))
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementChildrenCardinality()).isEqualTo(MANY);
        assertThat(actualPlacementData.getPlacementChildName()).isNull();
        assertThat(actualPlacementData.getPlacement()).isNull();
        assertThat(updatedCaseData.getIsCTSCUser()).isEqualTo(YesNo.NO);
        assertThatDynamicList(actualPlacementData.getPlacementChildrenList())
            .hasSize(2)
            .hasElement(child1.getId(), "Alex Brown")
            .hasElement(child2.getId(), "George White");
    }

    @Test
    void shouldPrepareNewPlacementWhenSingleChild() {

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child2))
            .respondents1(List.of(mother, father))
            .placementEventData(PlacementEventData.builder().build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementChildrenCardinality()).isEqualTo(ONE);

        assertThat(actualPlacementData.getPlacementChildName()).isEqualTo("George White");

        assertThat(actualPlacementData.getPlacement()).isEqualTo(Placement.builder()
            .childId(child2.getId())
            .childName("George White")
            .supportingDocuments(wrapElements(defaultBirthCertificate, defaultStatementOfFacts))
            .confidentialDocuments(wrapElements(defaultAnnexB))
            .build());


        assertThat(actualPlacementData.getPlacementChildrenList()).isNull();
        assertThat(updatedCaseData.getIsCTSCUser()).isEqualTo(YesNo.NO);
    }

    @Test
    void shouldPrepareExistingPlacementForSingleChild() {

        final PlacementNoticeDocument localAuthorityNotice = PlacementNoticeDocument.builder()
            .type(LOCAL_AUTHORITY)
            .response(testDocumentReference())
            .responseDescription("Local authority response description")
            .build();

        final PlacementNoticeDocument cafcassNotice = PlacementNoticeDocument.builder()
            .type(CAFCASS)
            .response(testDocumentReference())
            .responseDescription("Cafcass response description")
            .build();

        final PlacementNoticeDocument firstParentNotice = PlacementNoticeDocument.builder()
            .type(PARENT_FIRST)
            .recipientName("Emma Green")
            .respondentId(mother.getId())
            .response(testDocumentReference())
            .responseDescription("First parent response description")
            .build();

        final PlacementNoticeDocument secondParentNotice = PlacementNoticeDocument.builder()
            .type(PARENT_SECOND)
            .recipientName("Adam Green")
            .respondentId(father.getId())
            .response(testDocumentReference())
            .responseDescription("Second parent response description")
            .build();

        final Placement existingPlacement = Placement.builder()
            .application(testDocumentReference())
            .childId(child1.getId())
            .childName("Alex Brown")
            .placementUploadDateTime(LocalDateTime.now())
            .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
            .confidentialDocuments(wrapElements(annexB))
            .noticeDocuments(wrapElements(localAuthorityNotice, cafcassNotice, firstParentNotice, secondParentNotice))
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1))
            .respondents1(List.of(mother, father))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(existingPlacement))
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementChildrenCardinality()).isEqualTo(ONE);

        assertThat(actualPlacementData.getPlacementChildName()).isEqualTo("Alex Brown");

        assertThat(actualPlacementData.getPlacement()).isEqualTo(existingPlacement);

        assertThat(actualPlacementData.getPlacementChildrenList()).isNull();

        assertThat(updatedCaseData.getIsCTSCUser()).isEqualTo(YesNo.NO);
    }

    @Test
    void shouldReturnErrorWhenNoChildren() {

        final CaseData caseData = CaseData.builder()
            .children1(emptyList())
            .respondents1(List.of(mother, father))
            .placementEventData(PlacementEventData.builder()
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).containsExactly("There are no children available for placement application");
    }

}
