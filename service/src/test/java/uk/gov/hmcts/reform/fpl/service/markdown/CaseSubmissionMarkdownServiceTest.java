package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;

class CaseSubmissionMarkdownServiceTest {
    private static final long CASE_NUMBER = 1234567890123456L;
    private static final String FORMATTED_CASE_NUMBER = "1234-5678-9012-3456";
    private static final String CASE_NAME = "Corona v World";
    private static final String SURVEY_URL = "https://fake.url";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CaseSubmissionMarkdownService service = new CaseSubmissionMarkdownService(objectMapper, SURVEY_URL);

    @Test
    void shouldSplitOnSeparator() {
        MarkdownData markdownData = service.getMarkdownData(buildCaseData(CARE_ORDER), CASE_NUMBER);

        assertThat(markdownData).isEqualTo(buildExpectedData("Care order"));
    }

    @Test
    void shouldFormatMultipleOrders() {
        MarkdownData markdownData = service.getMarkdownData(buildCaseData(CARE_ORDER, SUPERVISION_ORDER), CASE_NUMBER);

        assertThat(markdownData).isEqualTo(buildExpectedData("Care order, Supervision order"));
    }

    @Test
    void shouldRemoveInitialBlankLinesWhenCaseNameNotProvided() {
        MarkdownData markdownData = service.getMarkdownData(buildCaseDataWithoutName(CARE_ORDER), CASE_NUMBER);

        assertThat(markdownData).isEqualTo(buildExpectedData("Care order", ""));
    }

    private MarkdownData buildExpectedData(String orders) {
        return buildExpectedData(orders, CASE_NAME);
    }

    private MarkdownData buildExpectedData(String orders, String caseName) {
        return MarkdownData.builder()
            .header(String.format("%s%n%s%n%s", caseName, FORMATTED_CASE_NUMBER, orders).trim())
            .body(SURVEY_URL)
            .build();
    }

    private CaseData buildCaseData(OrderType... orderTypes) {
        return buildCaseData(CASE_NAME, orderTypes);
    }

    private CaseData buildCaseDataWithoutName(OrderType... orderTypes) {
        return buildCaseData(null, orderTypes);
    }

    private CaseData buildCaseData(String caseName, OrderType... orderTypes) {
        return CaseData.builder()
            .orders(Orders.builder()
                .orderType(Arrays.asList(orderTypes))
                .build())
            .caseName(caseName)
            .build();
    }
}
