package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
public class DraftOrdersControllerMidEventTest extends AbstractControllerTest {
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
    private static final String DRAFT_ORDER_FILE_NAME = "draft-standard-directions-order.pdf";

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private Time time;

    DraftOrdersControllerMidEventTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument(SEALED_ORDER_FILE_NAME, PDF);
        Document document = document();

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(docmosisDocument);

        given(uploadDocumentService.uploadPDF(PDF, DRAFT_ORDER_FILE_NAME))
            .willReturn(document);
    }

    @Test
    void midEventShouldGenerateDraftStandardDirectionDocument() {
        List<Element<Direction>> directions = buildDirections(
            List.of(Direction.builder()
                .directionType("direction 1")
                .directionText("example")
                .assignee(LOCAL_AUTHORITY)
                .readOnly("No")
                .build()));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(directions)
                .put("dateOfIssue", time.now().toLocalDate().toString())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .put("caseLocalAuthority", "example")
                .put("dateSubmitted", time.now().toLocalDate().toString())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getData().get("standardDirectionOrder"))
            .extracting("orderDoc").isEqualTo(Map.of(
            "document_binary_url", document().links.binary.href,
            "document_filename", document().originalDocumentName,
            "document_url", document().links.self.href
        ));
    }

    @Test
    void midEventShouldMigrateJudgeAndLegalAdvisorWhenUsingAllocatedJudge() {
        List<Element<Direction>> directions = buildDirections(
            List.of(Direction.builder()
                .directionType("direction 1")
                .directionText("example")
                .assignee(LOCAL_AUTHORITY)
                .readOnly("No")
                .build()));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(directions)
                .put("dateOfIssue", time.now().toLocalDate().toString())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder()
                    .useAllocatedJudge("Yes")
                    .build())
                .put("allocatedJudge", Judge.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Davidson")
                    .build())
                .put("caseLocalAuthority", "example")
                .put("dateSubmitted", time.now().toLocalDate().toString())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getData().get("judgeAndLegalAdvisor")).isEqualToComparingOnlyGivenFields(Map.of(
            "judgeTitle", HIS_HONOUR_JUDGE,
            "JudgeLastName", "Davidson"
        ));
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
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(ElementUtils::element).collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction.toBuilder().directionType("Direction").build());
    }

}
