package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerServiceRouteMidEventTest extends AbstractCallbackTest {
    private static final byte[] PDF = testDocumentBinaries();
    private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
    private static final String DRAFT_ORDER_FILE_NAME = "draft-standard-directions-order.pdf";
    private static final long CASE_NUMBER = 1234123412341234L;
    private final Document document = document();

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    StandardDirectionsOrderControllerServiceRouteMidEventTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument(SEALED_ORDER_FILE_NAME, PDF));

        given(uploadDocumentService.uploadPDF(PDF, DRAFT_ORDER_FILE_NAME)).willReturn(document);
    }

    @Test
    void shouldGenerateDraftStandardDirectionDocumentWhenMinimumViableData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(buildTestDirections())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .build())DocmosisStandardDirectionOrderTest
            .id(CASE_NUMBER)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "service-route");

        assertThat(callbackResponse.getData().get("standardDirectionOrder")).extracting("orderDoc")
            .isEqualTo(Map.of(
                "document_binary_url", document().links.binary.href,
                "document_filename", document().originalDocumentName,
                "document_url", document().links.self.href
            ));
    }

    @Test
    void shouldMigrateJudgeAndLegalAdvisorWhenUsingAllocatedJudge() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(buildTestDirections())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .put("allocatedJudge", getJudgeAndLegalAdvisor())
                .build())
            .id(CASE_NUMBER)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "service-route");

        assertThat(callbackResponse.getData().get("judgeAndLegalAdvisor"))
            .isEqualToComparingOnlyGivenFields(Map.of("judgeTitle", HIS_HONOUR_JUDGE, "JudgeLastName", "Davidson"));
    }

    @Test
    void shouldReturnEmptyDirectionsListInStandardDirectionOrder() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(buildTestDirections())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .build())
            .id(CASE_NUMBER)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "service-route");

        assertThat(callbackResponse.getData().get("standardDirectionOrder")).extracting("directions")
            .isEqualTo(List.of());
    }

    private ImmutableMap.Builder<String, Object> createCaseDataMap(List<Element<Direction>> directions) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), directions)
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()))
            .put("dateOfIssue", dateNow())
            .put("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE)
            .put("dateSubmitted", dateNow())
            .put("applicants", getApplicant());
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

    private Judge getJudgeAndLegalAdvisor() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .judgeEmailAddress("davidson@example.com")
            .build();
    }
}
