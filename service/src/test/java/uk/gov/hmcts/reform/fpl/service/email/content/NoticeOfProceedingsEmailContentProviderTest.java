package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {NoticeOfProceedingsEmailContentProvider.class, EmailNotificationHelper.class,
    HearingBookingService.class, FixedTimeConfiguration.class})
class NoticeOfProceedingsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

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
