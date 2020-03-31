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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, LocalAuthorityEmailContentProvider.class, LookupTestConfig.class
})
class LocalAuthorityEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(localAuthorityEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() throws IOException {
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
            .put("leadRespondentsName", "Smith,")
            .put("hearingDate", "1 January 2020")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", buildCaseUrl(CASE_REFERENCE))
            .build();
    }

    private Map<String, Object> emptyTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", LOCAL_AUTHORITY_NAME)
            .put("familyManCaseNumber", "")
            .put("hearingDate", "")
            .put("leadRespondentsName", "Moley,")
            .put("reference", "null")
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, "null"))
            .build();
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder()
            .data(Map.of("respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Moley").build())
                .build())))
            .build();
    }
}
