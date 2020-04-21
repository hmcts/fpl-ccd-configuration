package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseManagementOrderEmailContentProvider.class})
class CaseManagementOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;

    @Autowired
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(caseManagementOrderEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldBuildCMOIssuedCaseLinkNotificationParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", "testName")
            .put("caseUrl", buildCaseUrl("12345"))
            .put("subjectLineWithHearingDate", "11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(createCase(), "testName")).isEqualTo(expectedMap);
    }

    @Test
    void shouldBuildCMORejectedByJudgeNotificationParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("requestedChanges", "change it")
            .put("caseUrl", buildCaseUrl("12345"))
            .put("subjectLineWithHearingDate", "11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(createCase())).isEqualTo(expectedMap);
    }

    String buildCaseUrl(String caseId) {
        return formatCaseUrl(BASE_URL, Long.parseLong(caseId));
    }

    private CaseDetails createCase() {
        final Map<String, Object> data = new HashMap<>();
        data.put("familyManCaseNumber", "11");
        data.put("caseName", "case1");
        data.put("cmoToAction",
                CaseManagementOrder.builder()
                    .action(OrderAction.builder()
                        .changeRequestedByJudge("change it")
                        .build())
                    .status(SEND_TO_JUDGE)
                    .build());

        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }
}
