package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {CafcassEmailContentProviderSDOIssued.class, LookupTestConfig.class})
class CafcassEmailContentProviderSDOIssuedTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProviderSDOIssued contentProviderSDOIssued;

    @Test
    void shouldReturnNotifyData() {
        CaseData caseData = populatedCaseData();

        NotifyData expectedParameters = SDONotifyData.builder()
            .title(CAFCASS_NAME)
            .familyManCaseNumber("12345,")
            .leadRespondentsName("Smith")
            .hearingDate("1 January 2020")
            .reference(caseData.getId().toString())
            .caseUrl(getCaseUrl(caseData.getId().toString()))
            .build();

        NotifyData actualParameters = contentProviderSDOIssued
            .getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

}
