package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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
        assertThatDynamicList(actualPlacementData.getPlacementChildrenList())
            .hasSize(2)
            .hasElement(child1.getId(), "Alex Brown")
            .hasElement(child2.getId(), "George White");
        assertThat(actualPlacementData.getPlacementNoticeForFirstParent()).isNull();
        assertThat(actualPlacementData.getPlacementNoticeForSecondParent()).isNull();
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

        assertThat(actualPlacementData.getPlacementNoticeForCafcassRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromCafcassReceived()).isEqualTo(NO);

        assertThat(actualPlacementData.getPlacementNoticeForLocalAuthorityRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromLocalAuthorityReceived()).isEqualTo(NO);

        assertThat(actualPlacementData.getPlacementNoticeForFirstParentRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromFirstParentReceived()).isEqualTo(NO);
        assertThatDynamicList(actualPlacementData.getPlacementNoticeForFirstParentParentsList())
            .hasSize(2)
            .hasNoSelectedValue()
            .hasElement(mother.getId(), "Emma Green - mother")
            .hasElement(father.getId(), "Adam Green - father");

        assertThat(actualPlacementData.getPlacementNoticeForSecondParentRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromSecondParentReceived()).isEqualTo(NO);
        assertThatDynamicList(actualPlacementData.getPlacementNoticeForSecondParentParentsList())
            .hasSize(2)
            .hasNoSelectedValue()
            .hasElement(mother.getId(), "Emma Green - mother")
            .hasElement(father.getId(), "Adam Green - father");

        assertThat(actualPlacementData.getPlacementChildrenList()).isNull();
    }

    @Test
    void shouldPrepareExistingPlacementForSingleChild() {

        final PlacementNoticeDocument localAuthorityNotice = PlacementNoticeDocument.builder()
            .type(LOCAL_AUTHORITY)
            .notice(testDocumentReference())
            .noticeDescription("Local authority description")
            .response(testDocumentReference())
            .responseDescription("Local authority response description")
            .build();

        final PlacementNoticeDocument cafcassNotice = PlacementNoticeDocument.builder()
            .type(CAFCASS)
            .notice(testDocumentReference())
            .noticeDescription("Cafcass description")
            .response(testDocumentReference())
            .noticeDescription("Cafcass response description")
            .build();

        final PlacementNoticeDocument firstParentNotice = PlacementNoticeDocument.builder()
            .type(PARENT_FIRST)
            .recipientName("Emma Green")
            .respondentId(mother.getId())
            .notice(testDocumentReference())
            .noticeDescription("First parent description")
            .response(testDocumentReference())
            .noticeDescription("First parent response description")
            .build();

        final PlacementNoticeDocument secondParentNotice = PlacementNoticeDocument.builder()
            .type(PARENT_SECOND)
            .recipientName("Adam Green")
            .respondentId(father.getId())
            .noticeDescription("Second parent description")
            .response(testDocumentReference())
            .noticeDescription("Second parent response description")
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

        assertThat(actualPlacementData.getPlacementNoticeForLocalAuthorityRequired())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeForLocalAuthority())
            .isEqualTo(localAuthorityNotice.getNotice());
        assertThat(actualPlacementData.getPlacementNoticeForLocalAuthorityDescription())
            .isEqualTo(localAuthorityNotice.getNoticeDescription());
        assertThat(actualPlacementData.getPlacementNoticeResponseFromLocalAuthorityReceived())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromLocalAuthority())
            .isEqualTo(localAuthorityNotice.getResponse());
        assertThat(actualPlacementData.getPlacementNoticeResponseFromLocalAuthorityDescription())
            .isEqualTo(localAuthorityNotice.getResponseDescription());

        assertThat(actualPlacementData.getPlacementNoticeForCafcassRequired())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeForCafcass())
            .isEqualTo(cafcassNotice.getNotice());
        assertThat(actualPlacementData.getPlacementNoticeForCafcassDescription())
            .isEqualTo(cafcassNotice.getNoticeDescription());
        assertThat(actualPlacementData.getPlacementNoticeResponseFromCafcassReceived())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromCafcass())
            .isEqualTo(cafcassNotice.getResponse());
        assertThat(actualPlacementData.getPlacementNoticeResponseFromCafcassDescription())
            .isEqualTo(cafcassNotice.getResponseDescription());

        assertThat(actualPlacementData.getPlacementNoticeForFirstParentRequired())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeForFirstParent())
            .isEqualTo(firstParentNotice.getNotice());
        assertThat(actualPlacementData.getPlacementNoticeForFirstParentDescription())
            .isEqualTo(firstParentNotice.getNoticeDescription());
        assertThatDynamicList(actualPlacementData.getPlacementNoticeForFirstParentParentsList())
            .hasSize(2)
            .hasSelectedValue(mother.getId(), "Emma Green - mother")
            .hasElement(mother.getId(), "Emma Green - mother")
            .hasElement(father.getId(), "Adam Green - father");
        assertThat(actualPlacementData.getPlacementNoticeResponseFromFirstParentReceived())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromFirstParent())
            .isEqualTo(firstParentNotice.getResponse());
        assertThat(actualPlacementData.getPlacementNoticeResponseFromFirstParentDescription())
            .isEqualTo(firstParentNotice.getResponseDescription());

        assertThat(actualPlacementData.getPlacementNoticeForSecondParentRequired())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeForSecondParent())
            .isEqualTo(secondParentNotice.getNotice());
        assertThat(actualPlacementData.getPlacementNoticeForSecondParentDescription())
            .isEqualTo(secondParentNotice.getNoticeDescription());
        assertThatDynamicList(actualPlacementData.getPlacementNoticeForSecondParentParentsList())
            .hasSize(2)
            .hasSelectedValue(father.getId(), "Adam Green - father")
            .hasElement(mother.getId(), "Emma Green - mother")
            .hasElement(father.getId(), "Adam Green - father");
        assertThat(actualPlacementData.getPlacementNoticeResponseFromSecondParentReceived())
            .isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementNoticeResponseFromSecondParent())
            .isEqualTo(secondParentNotice.getResponse());
        assertThat(actualPlacementData.getPlacementNoticeResponseFromSecondParentDescription())
            .isEqualTo(secondParentNotice.getResponseDescription());

        assertThat(actualPlacementData.getPlacementChildrenList()).isNull();
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
