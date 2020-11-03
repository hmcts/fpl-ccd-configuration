package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.UPLOAD;

@ActiveProfiles("integration-test")
@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerAboutToStartTest extends AbstractControllerTest {
    @MockBean
    private FeatureToggleService featureToggleService;

    private static final DocumentReference SDO = DocumentReference.builder().filename("sdo.pdf").build();

    StandardDirectionsOrderControllerAboutToStartTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldPopulateDateOfIssueWithPreviouslyEnteredDateWhenRouterIsService() {
        CaseDetails caseDetails = buildCaseDetailsWithDateOfIssueAndRoute("20 March 2020", SERVICE);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData().get("dateOfIssue")).isEqualTo(LocalDate.of(2020, 3, 20).toString());
    }

    @Test
    void shouldNotPopulateDateOfIssueWhenRouterIsUpload() {
        CaseDetails caseDetails = buildCaseDetailsWithDateOfIssueAndRoute("20 March 2020", UPLOAD);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData().get("dateOfIssue")).isNull();
    }

    @Test
    void shouldPopulateCurrentSDOFieldWithDocumentFromSDOWhenRouterIsUpload() {
        CaseDetails caseDetails = buildCaseDetailsWithUploadedDocument();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        DocumentReference doc = mapper.convertValue(response.getData().get("currentSDO"), DocumentReference.class);

        assertThat(doc).isEqualTo(SDO);
    }

    @Test
    void shouldPopulateServiceRoutingPageConditionVariableWhenRouterIsService() {
        CaseDetails caseDetails = buildCaseDetailsWithDateOfIssueAndRoute("13 March 2020", SERVICE);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData())
            .doesNotContainKey("useUploadRoute")
            .containsEntry("useServiceRoute", "YES");
    }

    @Test
    void shouldPopulateUploadRoutingPageConditionVariableWhenRouterIsUpload() {
        CaseDetails caseDetails = buildCaseDetailsWithUploadedDocument();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData())
            .doesNotContainKey("useServiceRoute")
            .containsEntry("useUploadRoute", "YES");
    }

    @Test
    void shouldPopulateJudgeAndLegalAdvisorInUploadRouteWhenSendNoticeOfProceedingsViaSDO() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "sdoRouter", UPLOAD,
                "standardDirectionOrder", StandardDirectionOrder.builder()
                    .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                    .build()
            )).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);
        CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
    }

    private CaseDetails buildCaseDetailsWithDateOfIssueAndRoute(String date, SDORoute route) {
        return buildCaseDetails(date, null, route);
    }

    private CaseDetails buildCaseDetailsWithUploadedDocument() {
        return buildCaseDetails(null, SDO, UPLOAD);
    }

    private CaseDetails buildCaseDetails(String date, DocumentReference doc, SDORoute route) {
        Map<String, Object> data = new HashMap<>(Map.of(
            "standardDirectionOrder", StandardDirectionOrder.builder().dateOfIssue(date).orderDoc(doc).build()
        ));

        data.put("sdoRouter", route);

        return CaseDetails.builder()
            .data(data)
            .build();
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .build();
    }
}
