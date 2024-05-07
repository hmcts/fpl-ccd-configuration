package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_2ND_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_CHILD_SOL;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_DESIGNATED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_RESP_SOL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.SOLICITOR_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CaseManagementOrderRejectedEventHandlerTest {

    private static final long CASE_ID = 12345L;
    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CaseManagementOrderEmailContentProvider contentProvider;

    @InjectMocks
    private CaseManagementOrderRejectedEventHandler underTest;

    @Test
    void shouldNotifyDesignatedLocalAuthorityIfCMORejected() {
        CaseData caseData = mock(CaseData.class);
        HearingOrder cmo = HearingOrder.builder().uploaderCaseRoles(List.of(CaseRole.LASOLICITOR)).build();
        RejectedCMOTemplate notifyData = mock(RejectedCMOTemplate.class);

        given(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(any()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(contentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(notifyData);

        given(caseData.getId()).willReturn(CASE_ID);

        underTest.sendNotifications(new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService)
            .sendEmail(CMO_REJECTED_BY_JUDGE_DESIGNATED_LA, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS), notifyData, CASE_ID);
    }

    @Test
    void shouldNotifyDesignatedLocalAuthorityIfCMORejectedIfCaseRoleNotGiven() {
        CaseData caseData = mock(CaseData.class);
        HearingOrder cmo = HearingOrder.builder().build();
        RejectedCMOTemplate notifyData = mock(RejectedCMOTemplate.class);

        given(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(any()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(contentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(notifyData);

        given(caseData.getId()).willReturn(CASE_ID);

        underTest.sendNotifications(new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService)
            .sendEmail(CMO_REJECTED_BY_JUDGE_DESIGNATED_LA, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS), notifyData, CASE_ID);
    }

    @Test
    void shouldNotifySecondaryLocalAuthorityIfCMORejected() {
        CaseData caseData = mock(CaseData.class);
        HearingOrder cmo = HearingOrder.builder().uploaderCaseRoles(List.of(CaseRole.LASHARED)).build();
        RejectedCMOTemplate notifyData = mock(RejectedCMOTemplate.class);

        given(furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(any()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(contentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(notifyData);

        given(caseData.getId()).willReturn(CASE_ID);

        underTest.sendNotifications(new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService)
            .sendEmail(CMO_REJECTED_BY_JUDGE_2ND_LA, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS), notifyData, CASE_ID);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "CHILDSOLICITORA", "CHILDSOLICITORB", "CHILDSOLICITORC", "CHILDSOLICITORD", "CHILDSOLICITORE",
        "CHILDSOLICITORF" ,"CHILDSOLICITORG", "CHILDSOLICITORH", "CHILDSOLICITORI", "CHILDSOLICITORJ",
        "CHILDSOLICITORK" ,"CHILDSOLICITORL", "CHILDSOLICITORM", "CHILDSOLICITORN", "CHILDSOLICITORO"
    })
    void shouldNotifyChildSolicitorIfCMORejected(CaseRole uploaderCaseRole) {
        CaseData caseData = mock(CaseData.class);
        HearingOrder cmo = HearingOrder.builder().uploaderCaseRoles(List.of(uploaderCaseRole)).build();
        RejectedCMOTemplate notifyData = mock(RejectedCMOTemplate.class);

        given(furtherEvidenceNotificationService.getChildSolicitorEmails(any(), eq(uploaderCaseRole)))
            .willReturn(Set.of(SOLICITOR_EMAIL_ADDRESS));
        given(contentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(notifyData);

        given(caseData.getId()).willReturn(CASE_ID);

        underTest.sendNotifications(new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService)
            .sendEmail(CMO_REJECTED_BY_JUDGE_CHILD_SOL, Set.of(SOLICITOR_EMAIL_ADDRESS), notifyData, CASE_ID);
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "SOLICITORA", "SOLICITORB", "SOLICITORC", "SOLICITORD", "SOLICITORE", "SOLICITORF" ,"SOLICITORG", "SOLICITORH",
        "SOLICITORI", "SOLICITORJ"
    })
    void shouldNotifyRespondentSolicitorIfCMORejected(CaseRole uploaderCaseRole) {
        CaseData caseData = mock(CaseData.class);
        HearingOrder cmo = HearingOrder.builder().uploaderCaseRoles(List.of(uploaderCaseRole)).build();
        RejectedCMOTemplate notifyData = mock(RejectedCMOTemplate.class);

        given(furtherEvidenceNotificationService.getRespondentSolicitorEmails(any(), eq(uploaderCaseRole)))
            .willReturn(Set.of(SOLICITOR_EMAIL_ADDRESS));
        given(contentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(notifyData);

        given(caseData.getId()).willReturn(CASE_ID);

        underTest.sendNotifications(new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService)
            .sendEmail(CMO_REJECTED_BY_JUDGE_RESP_SOL, Set.of(SOLICITOR_EMAIL_ADDRESS), notifyData, CASE_ID);
    }
}
