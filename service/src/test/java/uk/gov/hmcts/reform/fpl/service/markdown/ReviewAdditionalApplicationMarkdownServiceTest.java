package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPLICANT_CHANGE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPROVE_APPLICATION_AND_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.LIST;

class ReviewAdditionalApplicationMarkdownServiceTest {

    private static final String CTSC_TEXT =
        "A task for the courts and tribunals service centre (CTSC) to review the order will be created.";

    private final ReviewAdditionalApplicationMarkdownService service =
        new ReviewAdditionalApplicationMarkdownService(new ObjectMapper());

    @Test
    void shouldIncludeCtscTextForApproveAndOrder() {
        MarkdownData markdownData = service.getMarkdownData("case", false, APPROVE_APPLICATION_AND_ORDER);

        assertThat(markdownData.getBody()).contains(CTSC_TEXT);
    }

    @Test
    void shouldHideCtscTextForApplicantChangeOrder() {
        MarkdownData markdownData = service.getMarkdownData("case", false, APPLICANT_CHANGE_ORDER);

        assertThat(markdownData.getBody()).doesNotContain(CTSC_TEXT);
    }

    @Test
    void shouldUseConfidentialNoCtscTemplateForApplicantChangeOrder() {
        MarkdownData markdownData = service.getMarkdownData("case", true, APPLICANT_CHANGE_ORDER);

        assertThat(markdownData.getBody())
            .contains("The applicant will be notified.")
            .doesNotContain("The applicant and the other parties will be notified.")
            .doesNotContain(CTSC_TEXT);
    }

    @Test
    void shouldUseListTemplateForNonConfidentialListDecision() {
        MarkdownData markdownData = service.getMarkdownData("case", false, LIST);

        assertThat(markdownData.getBody())
            .contains("A new order with your decision to list the application at the next hearing will be created.")
            .contains("The applicant and the other parties will be notified.")
            .doesNotContain(CTSC_TEXT);
    }

    @Test
    void shouldUseConfidentialListTemplateForConfidentialListDecision() {
        MarkdownData markdownData = service.getMarkdownData("case", true, LIST);

        assertThat(markdownData.getBody())
            .contains("A new order with your decision to list the application at the next hearing will be created.")
            .contains("The applicant will be notified.")
            .doesNotContain("The applicant and the other parties will be notified.")
            .doesNotContain(CTSC_TEXT);
    }
}

