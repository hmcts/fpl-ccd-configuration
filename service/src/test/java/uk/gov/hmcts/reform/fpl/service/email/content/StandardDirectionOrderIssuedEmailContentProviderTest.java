package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {
    StandardDirectionOrderIssuedEmailContentProvider.class,
    LookupTestConfig.class,
    HearingBookingService.class,
    FixedTimeConfiguration.class,
    LookupTestConfig.class,
    EmailNotificationHelper.class,
    CaseDataExtractionService.class,
    NoticeOfHearingGenerationService.class,
    HearingVenueLookUpService.class
})
class StandardDirectionOrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        AllocatedJudgeTemplateForSDO expectedMap = allocatedJudgeSDOTemplateParameters();

        assertThat(standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForAllocatedJudge(populatedCaseDetails()))
            .isEqualToComparingFieldByField(expectedMap);
    }

    private AllocatedJudgeTemplateForSDO allocatedJudgeSDOTemplateParameters() {
        AllocatedJudgeTemplateForSDO allocatedJudgeTemplate = new AllocatedJudgeTemplateForSDO();
        allocatedJudgeTemplate.setFamilyManCaseNumber("12345,");
        allocatedJudgeTemplate.setLeadRespondentsName("Smith");
        allocatedJudgeTemplate.setHearingDate("1 January 2020");
        allocatedJudgeTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setJudgeName("Byrne");

        return allocatedJudgeTemplate;
    }
}
