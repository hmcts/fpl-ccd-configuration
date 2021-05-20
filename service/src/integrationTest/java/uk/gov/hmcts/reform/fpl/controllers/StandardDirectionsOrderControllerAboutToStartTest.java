package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerAboutToStartTest extends AbstractCallbackTest {
    private static final DocumentReference SDO = testDocumentReference("sdo.pdf");

    StandardDirectionsOrderControllerAboutToStartTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldReturnErrorWhenSDOSealedAndUrgentHearingOrderPopulated() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(OrderStatus.SEALED).build())
            .urgentHearingOrder(UrgentHearingOrder.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).isEqualTo(List.of("There is already a gatekeeping order for this case"));
    }

    @Test
    void shouldPopulateDateOfIssueWithPreviouslyEnteredDateWhenRouterIsService() {
        CaseData responseData = extractCaseData(postAboutToStartEvent(buildCaseDetailsWithDateOfIssueAndRoute(
            "20 March 2020", SERVICE
        )));

        assertThat(responseData.getDateOfIssue()).isEqualTo(LocalDate.of(2020, 3, 20).toString());
    }

    @Test
    void shouldNotPopulateDateOfIssueWhenRouterIsUpload() {
        CaseData responseData = extractCaseData(postAboutToStartEvent(buildCaseDetailsWithDateOfIssueAndRoute(
            "20 March 2020", UPLOAD
        )));

        assertThat(responseData.getDateOfIssue()).isNull();
    }

    @Test
    void shouldPopulateCurrentSDOFieldWithDocumentFromSDOWhenRouterIsUpload() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(buildCaseDetailsWithUploadedDocument());

        DocumentReference doc = mapper.convertValue(response.getData().get("currentSDO"), DocumentReference.class);

        assertThat(doc).isEqualTo(SDO);
    }

    @Test
    void shouldPopulateServiceRoutingPageConditionVariableWhenRouterIsService() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(
            buildCaseDetailsWithDateOfIssueAndRoute("13 March 2020", SERVICE)
        );

        assertThat(response.getData())
            .doesNotContainKey("useUploadRoute")
            .containsEntry("useServiceRoute", "YES");
    }

    @Test
    void shouldPopulateUploadRoutingPageConditionVariableWhenRouterIsUpload() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(buildCaseDetailsWithUploadedDocument());

        assertThat(response.getData())
            .doesNotContainKey("useServiceRoute")
            .containsEntry("useUploadRoute", "YES");
    }

    @Test
    void shouldPopulateJudgeAndLegalAdvisorInUploadRoute() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor();

        CaseData caseData = CaseData.builder()
            .sdoRouter(UPLOAD)
            .standardDirectionOrder(StandardDirectionOrder.builder().judgeAndLegalAdvisor(judgeAndLegalAdvisor).build())
            .build();

        CaseData responseCaseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
    }

    @Test
    void shouldPopulateSDODirectionsWhenDirectionsAreEmpty() {
        CaseData originalCaseData = CaseData.builder().build();

        CaseData actualCaseData = extractCaseData(postAboutToStartEvent(originalCaseData));

        assertThat(actualCaseData.getAllParties()).hasSize(5);
        assertThat(actualCaseData.getLocalAuthorityDirections()).hasSize(7);
        assertThat(actualCaseData.getRespondentDirections()).hasSize(1);
        assertThat(actualCaseData.getOtherPartiesDirections()).hasSize(1);
        assertThat(actualCaseData.getCafcassDirections()).hasSize(3);
        assertThat(actualCaseData.getCourtDirections()).hasSize(1);
    }

    @Test
    void shouldNotOverwriteSDODirectionsWhenDirectionsAreNotEmpty() {
        CaseData originalCaseData = CaseData.builder()
            .localAuthorityDirections(wrapElements(Direction.builder().assignee(LOCAL_AUTHORITY).build()))
            .build();

        CaseData actualCaseData = extractCaseData(postAboutToStartEvent(originalCaseData));

        assertThat(actualCaseData.getAllParties()).isEqualTo(originalCaseData.getAllParties());
        assertThat(actualCaseData.getLocalAuthorityDirections()).isEqualTo(originalCaseData
            .getLocalAuthorityDirections());
        assertThat(actualCaseData.getRespondentDirections()).isEqualTo(originalCaseData.getRespondentDirections());
        assertThat(actualCaseData.getOtherPartiesDirections()).isEqualTo(originalCaseData.getOtherPartiesDirections());
        assertThat(actualCaseData.getCafcassDirections()).isEqualTo(originalCaseData.getCafcassDirections());
        assertThat(actualCaseData.getCourtDirections()).isEqualTo(originalCaseData.getCourtDirections());
    }

    private CaseData buildCaseDetailsWithDateOfIssueAndRoute(String date, SDORoute route) {
        return buildCaseDetails(date, null, route);
    }

    private CaseData buildCaseDetailsWithUploadedDocument() {
        return buildCaseDetails(null, SDO, UPLOAD);
    }

    private CaseData buildCaseDetails(String date, DocumentReference doc, SDORoute route) {
        return CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().dateOfIssue(date).orderDoc(doc).build())
            .sdoRouter(route)
            .build();
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .build();
    }
}
