package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
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
    NoticeOfProceedingsEmailContentProvider.class,
    EmailNotificationHelper.class,
    HearingBookingService.class,
    FixedTimeConfiguration.class,
    LookupTestConfig.class,
    CaseDataExtractionService.class,
    NoticeOfHearingGenerationService.class,
    HearingVenueLookUpService.class
})
class NoticeOfProceedingsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldBuildNoticeOfProceedingsParametersForAllocatedJudge() {
        AllocatedJudgeTemplateForNoticeOfProceedings expectedMap = getExpectedAllocatedJudgeParameters();

        assertThat(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(populatedCaseDetails()))
            .isEqualToComparingFieldByField(expectedMap);
    }

    private AllocatedJudgeTemplateForNoticeOfProceedings getExpectedAllocatedJudgeParameters() {
        AllocatedJudgeTemplateForNoticeOfProceedings allocatedJudgeTemplate
            = new AllocatedJudgeTemplateForNoticeOfProceedings();
        allocatedJudgeTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        allocatedJudgeTemplate.setFamilyManCaseNumber("12345");
        allocatedJudgeTemplate.setHearingDate("1 January 2020");
        allocatedJudgeTemplate.setJudgeName("Moley");
        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setRespondentLastName("Smith");

        return allocatedJudgeTemplate;
    }
}
