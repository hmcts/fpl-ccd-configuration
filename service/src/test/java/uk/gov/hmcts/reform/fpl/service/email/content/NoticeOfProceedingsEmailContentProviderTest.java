package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabLabel.HEARINGS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {NoticeOfProceedingsEmailContentProvider.class})
class NoticeOfProceedingsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @Test
    void shouldBuildNoticeOfProceedingsParametersForAllocatedJudge() {
        NotifyData expectedData = getExpectedAllocatedJudgeParameters();
        NotifyData actualData = noticeOfProceedingsEmailContentProvider
            .buildAllocatedJudgeNotification(populatedCaseData());

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    private AllocatedJudgeTemplateForNoticeOfProceedings getExpectedAllocatedJudgeParameters() {
        return AllocatedJudgeTemplateForNoticeOfProceedings.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, HEARINGS))
            .familyManCaseNumber("12345")
            .hearingDate("1 January 2020")
            .judgeName("Moley")
            .judgeTitle("Her Honour Judge")
            .respondentLastName("Smith")
            .build();
    }
}
