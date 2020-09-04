package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {LocalAuthorityEmailContentProvider.class, LookupTestConfig.class})
class LocalAuthorityEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        NotifyData expectedData = SDONotifyData.builder()
            .title(LOCAL_AUTHORITY_NAME)
            .familyManCaseNumber("12345,")
            .leadRespondentsName("Smith")
            .hearingDate("1 January 2020")
            .reference(CASE_REFERENCE)
            .caseUrl(getCaseUrl(CASE_REFERENCE))
            .build();

        NotifyData actualData = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(populatedCaseData());

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnExpectedMapWithNullSDODetails() {

        SDONotifyData expectedData = SDONotifyData.builder()
            .title(LOCAL_AUTHORITY_NAME)
            .familyManCaseNumber("")
            .hearingDate("")
            .leadRespondentsName("Moley")
            .reference("1")
            .caseUrl(getCaseUrl("1"))
            .build();

        SDONotifyData actualData = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(getCaseData());

        assertThat(actualData).isEqualTo(expectedData);
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

    private CaseData getCaseData() {
        return CaseData.builder()
            .id(1L)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Moley").build())
                .build()))
            .build();

    }
}
