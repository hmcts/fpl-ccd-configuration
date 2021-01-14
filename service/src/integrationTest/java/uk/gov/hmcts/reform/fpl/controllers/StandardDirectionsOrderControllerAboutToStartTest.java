package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerAboutToStartTest extends AbstractControllerTest {
    private static final DocumentReference SDO = DocumentReference.builder().filename("sdo.pdf").build();

    private static final Element<Direction> ALL_PARTIES_DIRECTION =
        element(Direction.builder().assignee(ALL_PARTIES).build());
    private static final Element<Direction> LOCAL_AUTHORITY_DIRECTION =
        element(Direction.builder().assignee(LOCAL_AUTHORITY).build());
    private static final Element<Direction> RESPONDENT_DIRECTION =
        element(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build());
    private static final List<Element<Direction>> STANDARD_DIRECTIONS = List.of(
        ALL_PARTIES_DIRECTION, LOCAL_AUTHORITY_DIRECTION, RESPONDENT_DIRECTION);
    private static final Map<DirectionAssignee, List<Element<Direction>>> DIRECTIONS_SORTED_BY_ASSIGNEE = Map.of(
        ALL_PARTIES, List.of(ALL_PARTIES_DIRECTION),
        LOCAL_AUTHORITY, List.of(LOCAL_AUTHORITY_DIRECTION),
        PARENTS_AND_RESPONDENTS, List.of(RESPONDENT_DIRECTION)
    );


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
    void shouldPopulateJudgeAndLegalAdvisorInUploadRoute() {
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
        assertThat(actualCaseData.getLocalAuthorityDirections()).isEqualTo(originalCaseData.getLocalAuthorityDirections());
        assertThat(actualCaseData.getRespondentDirections()).isEqualTo(originalCaseData.getRespondentDirections());
        assertThat(actualCaseData.getOtherPartiesDirections()).isEqualTo(originalCaseData.getOtherPartiesDirections());
        assertThat(actualCaseData.getCafcassDirections()).isEqualTo(originalCaseData.getCafcassDirections());
        assertThat(actualCaseData.getCourtDirections()).isEqualTo(originalCaseData.getCourtDirections());
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

    private Map<String, List<Element<Direction>>> getExpectedDirections() {
        return Map.of(
            ALL_PARTIES.getValue(), DIRECTIONS_SORTED_BY_ASSIGNEE.get(ALL_PARTIES),
            LOCAL_AUTHORITY.getValue(), DIRECTIONS_SORTED_BY_ASSIGNEE.get(LOCAL_AUTHORITY),
            PARENTS_AND_RESPONDENTS.getValue(), DIRECTIONS_SORTED_BY_ASSIGNEE.get(PARENTS_AND_RESPONDENTS),
            CAFCASS.getValue(), emptyList(),
            COURT.getValue(), emptyList(),
            OTHERS.getValue(), emptyList());
    }
}
