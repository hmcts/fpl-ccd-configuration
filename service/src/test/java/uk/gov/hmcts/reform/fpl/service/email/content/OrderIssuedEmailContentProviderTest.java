package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    OrderIssuedEmailContentProvider.class,
    LookupTestConfig.class
})
class OrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;
    static final String LOCAL_AUTHORITY_CODE = "example";

    @MockBean
    private RepresentativeService service;

    @InjectMocks
    OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(orderIssuedEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldBuildNotificationParametersForHmctsAdmin() {

        when(service.getRepresentativesByServedPreference(any(),any())).thenReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("callout", "")
            .put("needsPosting","yes")
            .put("courtName","Family")
            .put("representatives","")
            .build();

        assertThat(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            buildCaseDetails(),
            LOCAL_AUTHORITY_CODE,
            "test".getBytes(),
            IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER
        )).isEqualTo(expectedMap);
    }

    String buildCaseUrl(String caseId) {
        return formatCaseUrl(BASE_URL, Long.parseLong(caseId));
    }

    private CaseDetails buildCaseDetails() {
        return CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();
    }

    private List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(POST)
                .build());
    }
}
