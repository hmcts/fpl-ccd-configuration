package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {
    CafcassEmailContentProviderSDOIssued.class,
    LookupTestConfig.class,
    HearingBookingService.class,
    FixedTimeConfiguration.class,
    CaseDataExtractionService.class,
    NoticeOfHearingGenerationService.class,
    HearingVenueLookUpService.class
})
class CafcassEmailContentProviderSDOIssuedTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProviderSDOIssued contentProviderSDOIssued;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        Map<String, Object> expectedMap = getStandardDirectionTemplateParameters();

        assertThat(contentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {

        return ImmutableMap.<String, Object>builder()
            .put("title", CAFCASS_NAME)
            .put("familyManCaseNumber", "12345,")
            .put("leadRespondentsName", "Smith")
            .put("hearingDate", "1 January 2020")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .build();
    }
}
