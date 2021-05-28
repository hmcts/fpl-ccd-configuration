package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersControllerInitialSectionMidEventTest extends AbstractCallbackTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "CASE_NUMBER";

    private static final long CCD_CASE_NUMBER = 1234123412341234L;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisGenerationService;

    @MockBean
    private DocumentMerger documentMerger;

    @MockBean
    private UploadDocumentService uploadService;

    ManageOrdersControllerInitialSectionMidEventTest() {
        super("manage-orders");
    }

    @Test
    void shouldPrepopulateIssueDetailsSectionDataWhenCreatingBlankOrderForClosedCase() {
        CaseData caseData = CaseData.builder()
            .id(CCD_CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .state(CLOSED)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "initial-selection");

        Map<String, String> expectedTempQuestions = Map.ofEntries(
            Map.entry("approver", "YES"),
            Map.entry("previewOrder", "YES"),
            Map.entry("furtherDirections", "NO"),
            Map.entry("orderDetails", "YES"),
            Map.entry("whichChildren", "YES"),
            Map.entry("hearingDetails", "YES"),
            Map.entry("approvalDate", "YES"),
            Map.entry("approvalDateTime", "NO"),
            Map.entry("epoIncludePhrase", "NO"),
            Map.entry("epoExpiryDate", "NO"),
            Map.entry("epoTypeAndPreventRemoval", "NO"),
            Map.entry("epoChildrenDescription", "NO"),
            Map.entry("cafcassJurisdictions", "NO"),
            Map.entry("supervisionOrderExpiryDate", "NO"),
            Map.entry("closeCase", "NO")
        );

        assertThat(response.getData()).containsAllEntriesOf(
            Map.of("orderTempQuestions", expectedTempQuestions,
                "manageOrdersState", "CLOSED",
                "manageOrdersType", "C21_BLANK_ORDER"));
    }

    @Test
    void shouldNotPopulateHiddenFieldValuesWhenCreatingBlankOrderForTheCaseNotInClosedState() {
        CaseData caseData = CaseData.builder()
            .id(CCD_CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .state(SUBMITTED)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "initial-selection");
        assertThat(response.getData()).doesNotContainKey("issuingDetailsSectionSubHeader");
        assertThat(response.getData()).containsEntry("orderTempQuestions", null);
    }

}
