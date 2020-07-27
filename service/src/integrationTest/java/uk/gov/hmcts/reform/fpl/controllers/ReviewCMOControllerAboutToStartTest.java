package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.cmo.ReviewCMOController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
public class ReviewCMOControllerAboutToStartTest extends AbstractControllerTest {

    @MockBean
    private DocumentSealingService documentSealingService;

    protected ReviewCMOControllerAboutToStartTest() {
        super("review-cmo");
    }

    @Test
    void shouldReturnCorrectMapWhenMultipleCMOsReadyForApproval() {
        DocumentReference order = TestDataHelper.testDocumentReference();
        List<Element<CaseManagementOrder>> draftCMOs = List.of(
            element(buildCMO("Test hearing 21st August 2020", order)),
            element(buildCMO("Test hearing 9th April 2021", order)));

        CaseData caseData = CaseData.builder().draftUploadedCMOs(draftCMOs).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                DynamicListElement.builder()
                    .code(draftCMOs.get(0).getId())
                    .label("Test hearing 21st August 2020")
                    .build(),
                DynamicListElement.builder()
                    .code(draftCMOs.get(1).getId())
                    .label("Test hearing 9th April 2021")
                    .build()
            ))
            .build();

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getNumDraftCMOs()).isEqualTo("MULTI");
        assertThat(responseData.getCmoToReviewList()).isEqualTo(
            mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {}));
    }

    @Test
    void shouldReturnCorrectMapWhenOneDraftCMOReadyForApproval() {
        DocumentReference order = TestDataHelper.testDocumentReference();
        List<Element<CaseManagementOrder>> draftCMOs = List.of(
            element(buildCMO("Test hearing 21st August 2020", order)));

        CaseData caseData = CaseData.builder().draftUploadedCMOs(draftCMOs).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getNumDraftCMOs()).isEqualTo("SINGLE");
        assertThat(responseData.getReviewCMODecision().getHearing()).isEqualTo("Test hearing 21st August 2020");
        assertThat(responseData.getReviewCMODecision().getDocument()).isEqualTo(order);
    }

    @Test
    void shouldReturnCorrectMapWhenNoDraftCMOsReadyForApproval() {
        CaseData caseData = CaseData.builder().draftUploadedCMOs(List.of()).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

        assertThat(response.getData()).extracting("numDraftCMOs").isEqualTo("NONE");
    }

    private CaseManagementOrder buildCMO(String hearing, DocumentReference order) {
        return CaseManagementOrder.builder()
            .hearing(hearing)
            .order(order)
            .status(SEND_TO_JUDGE).build();
    }
}
