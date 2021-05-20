package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {LocalAuthorityEmailContentProvider.class})
class LocalAuthorityEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private LocalAuthorityEmailContentProvider underTest;

    @Test
    void shouldBuildPlacementNotificationData() {
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0, 0))
                .build()))
            .build();

        NotifyData actualData = underTest.buildNoticeOfPlacementOrderUploadedNotification(caseData);

        NotifyData expectedData = BaseCaseNotifyData.builder()
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, PLACEMENT))
            .build();

        assertThat(actualData).isEqualTo(expectedData);
    }

}
