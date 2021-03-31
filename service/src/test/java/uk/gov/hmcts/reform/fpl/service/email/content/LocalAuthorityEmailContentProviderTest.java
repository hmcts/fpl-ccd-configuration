package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
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
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .callout("Smith, 12345, hearing 1 Jan 2020")
            .build();

        NotifyData actualData = localAuthorityEmailContentProvider
            .buildStandardDirectionOrderIssuedNotification(populatedCaseData());

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    @Test
    void shouldReturnExpectedMapWithNullSDODetails() {

        SDONotifyData expectedData = SDONotifyData.builder()
            .title(LOCAL_AUTHORITY_NAME)
            .familyManCaseNumber("")
            .hearingDate("")
            .leadRespondentsName("Smith")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .callout("Smith")
            .build();

        SDONotifyData actualData = localAuthorityEmailContentProvider
            .buildStandardDirectionOrderIssuedNotification(getCaseData());

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    private CaseData getCaseData() {
        return CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .build();

    }
}
