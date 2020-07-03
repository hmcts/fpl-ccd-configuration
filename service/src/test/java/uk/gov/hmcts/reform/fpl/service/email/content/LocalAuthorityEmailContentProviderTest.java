package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {LocalAuthorityEmailContentProvider.class, LookupTestConfig.class,
    HearingBookingService.class, FixedTimeConfiguration.class
})
class LocalAuthorityEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        Map<String, Object> expectedMap = standardDirectionTemplateParameters();

        assertThat(localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(populatedCaseDetails(),
                LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapWithNullSDODetails() {
        assertThat(localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseDetails(), LOCAL_AUTHORITY_CODE))
            .isEqualTo(emptyTemplateParameters());
    }

    private Map<String, Object> standardDirectionTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", LOCAL_AUTHORITY_NAME)
            .put("familyManCaseNumber", "12345,")
            .put("leadRespondentsName", "Smith")
            .put("hearingDate", "1 January 2020")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .build();
    }

    private Map<String, Object> emptyTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", LOCAL_AUTHORITY_NAME)
            .put("familyManCaseNumber", "")
            .put("hearingDate", "")
            .put("leadRespondentsName", "Moley")
            .put("reference", "1")
            .put("caseUrl", String.format("http://fake-url/cases/case-details/%s", 1L))
            .build();
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder()
            .id(1L)
            .data(Map.of("respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Moley").build())
                .build())))
            .build();
    }
}
