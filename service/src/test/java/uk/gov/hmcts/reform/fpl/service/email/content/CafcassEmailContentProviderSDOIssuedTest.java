package uk.gov.hmcts.reform.fpl.service.email.content;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

@ContextConfiguration(classes = {CafcassEmailContentProviderSDOIssued.class, LookupTestConfig.class})
class CafcassEmailContentProviderSDOIssuedTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProviderSDOIssued contentProviderSDOIssued;

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    @Test
    void shouldReturnNotifyData() {
        CaseData caseData = populatedCaseData();

        when(documentDownloadService.downloadDocument(any()))
            .thenReturn(APPLICATION_BINARY);

        NotifyData expectedParameters = SDONotifyData.builder()
            .title(CAFCASS_NAME)
            .familyManCaseNumber("12345,")
            .leadRespondentsName("Smith")
            .hearingDate("1 January 2020")
            .reference(caseData.getId().toString())
            .caseUrl(caseUrl(caseData.getId().toString(), ORDERS))
            .documentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
                .map(JSONObject::toMap)
                .orElse(null))
            .callout("Smith, 12345, hearing 1 Jan 2020")
            .build();

        NotifyData actualParameters = contentProviderSDOIssued.getNotifyData(caseData);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

}
