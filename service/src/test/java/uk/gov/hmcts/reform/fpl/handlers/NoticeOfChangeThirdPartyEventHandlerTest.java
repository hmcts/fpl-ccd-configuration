package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeThirdPartyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class NoticeOfChangeThirdPartyEventHandlerTest {

    private final CaseData caseData = mock(CaseData.class);

    private static final NoticeOfChangeRespondentSolicitorTemplate EXPECTED_TEMPLATE =
        mock(NoticeOfChangeRespondentSolicitorTemplate.class);

    private final LocalAuthority oldThirdPartyOrg = LocalAuthority.builder().build();
    private final LocalAuthority newThirdPartyOrg = LocalAuthority.builder().build();

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @InjectMocks
    private NoticeOfChangeThirdPartyEventHandler underTest;

    @Test
    void shouldSendEmailToSolicitorAccessGranted() {
        given(noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(caseData))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifyThirdPartySolicitorAccessGranted(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrg,
            newThirdPartyOrg, caseData));
    }

    @Test
    void shouldSendEmailToSolicitorAccessRemoved() {
        given(noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(caseData))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifyThirdPartySolicitorAccessRemoved(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrg,
            newThirdPartyOrg, caseData));
    }
}
