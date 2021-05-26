package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerServiceRouteMidEventTest extends AbstractCallbackTest {
    private static final long CASE_NUMBER = 1234123412341234L;
    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.buildFromDocument(DOCUMENT);

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    StandardDirectionsOrderControllerServiceRouteMidEventTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        final byte[] pdf = testDocumentBinaries();
        final String sealedOrderFileName = "standard-directions-order.pdf";
        final String draftOrderFileName = "draft-standard-directions-order.pdf";

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument(sealedOrderFileName, pdf));

        given(uploadDocumentService.uploadPDF(pdf, draftOrderFileName)).willReturn(DOCUMENT);
    }

    @Test
    void shouldGenerateDraftStandardDirectionDocumentWhenMinimumViableData() {
        CaseData caseData = populateCaseData(buildTestDirections()).build();

        CaseData response = extractCaseData(postMidEvent(caseData, "service-route"));

        assertThat(response.getStandardDirectionOrder().getOrderDoc()).isEqualTo(DOCUMENT_REFERENCE);
    }

    @Test
    void shouldMigrateJudgeAndLegalAdvisorWhenUsingAllocatedJudge() {
        CaseData caseDAta = populateCaseData(buildTestDirections())
            .allocatedJudge(getJudge())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
            .build();

        CaseData response = extractCaseData(postMidEvent(caseDAta, "service-route"));

        assertThat(response.getStandardDirectionOrder().getJudgeAndLegalAdvisor()).isEqualTo(
            JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .judgeEmailAddress("davidson@example.com")
                .useAllocatedJudge("Yes")
                .build()
        );
    }

    @Test
    void shouldReturnEmptyDirectionsListInStandardDirectionOrder() {
        CaseData caseData = populateCaseData(buildTestDirections()).build();

        CaseData response = extractCaseData(postMidEvent(caseData, "service-route"));

        assertThat(response.getStandardDirectionOrder().getDirections()).isEmpty();
    }

    @Test
    void shouldPopulateShowNoticeOfProceedingsWhenInGatekeepingState() {
        CaseData caseData = populateCaseData(buildTestDirections()).state(State.GATEKEEPING).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "service-route");

        assertThat(response.getData().get("showNoticeOfProceedings")).isEqualTo("YES");
    }

    @Test
    void shouldPopulateShowNoticeOfProceedingsWhenNotInGatekeepingState() {
        CaseData caseData = populateCaseData(buildTestDirections()).state(State.CASE_MANAGEMENT).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "service-route");

        assertThat(response.getData().get("showNoticeOfProceedings")).isEqualTo("NO");
    }

    private CaseData.CaseDataBuilder populateCaseData(List<Element<Direction>> directions) {
        return CaseData.builder()
            .localAuthorityDirections(directions)
            .allParties(buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .respondentDirections(buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .cafcassDirections(buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .otherPartiesDirections(buildDirections(Direction.builder().assignee(OTHERS).build()))
            .courtDirections(buildDirections(Direction.builder().assignee(COURT).build()))
            .dateOfIssue(dateNow())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .dateSubmitted(dateNow())
            .applicants(getApplicant())
            .id(CASE_NUMBER)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build());
    }

    private List<Element<Direction>> buildTestDirections() {
        return List.of(element(Direction.builder()
            .directionType("direction 1")
            .directionText("example")
            .assignee(LOCAL_AUTHORITY)
            .readOnly("No")
            .build()));
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction.toBuilder().directionType("Direction").build());
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("")
                .build())
            .build());
    }

    private Judge getJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .judgeEmailAddress("davidson@example.com")
            .build();
    }
}
