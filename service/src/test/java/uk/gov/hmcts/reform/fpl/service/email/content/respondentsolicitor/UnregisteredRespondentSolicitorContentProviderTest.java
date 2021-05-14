package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.UnregisteredRespondentSolicitorTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnregisteredRespondentSolicitorContentProviderTest {

    private static final String LOCAL_AUTHORITY_CODE = "LA_CODE";
    private static final String LOCAL_AUTHORITY_NAME = "LA name";
    private static final long CCD_NUMBER = 1234567890123456L;
    private static final String FORMATTED_CCD_NUMBER = "1234-5678-9012-3456";

    private final LocalAuthorityNameLookupConfiguration lookup = mock(LocalAuthorityNameLookupConfiguration.class);
    private final UnregisteredRespondentSolicitorContentProvider underTest =
        new UnregisteredRespondentSolicitorContentProvider(lookup);

    @Test
    void buildContent() {
        when(lookup.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn(LOCAL_AUTHORITY_NAME);

        CaseData caseData = CaseData.builder()
            .id(CCD_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .build();

        NotifyData expectedTemplateData = UnregisteredRespondentSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .ccdNumber(FORMATTED_CCD_NUMBER)
            .build();

        assertThat(underTest.buildContent(caseData)).isEqualTo(expectedTemplateData);
    }
}
