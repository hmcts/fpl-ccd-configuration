package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest.builder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FailedPBAPaymentEventHandler.class, LookupTestConfig.class})
class FailedPBAPaymentEventHandlerTest {

    @MockBean
    private RequestData requestData;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

    @Autowired
    private FailedPBAPaymentEventHandler failedPBAPaymentEventHandler;

    private CaseData caseData;

    private static final Long CASE_ID = 12345L;
    private static final String RESPONDENT_EMAIL = "john.smith@test.com";
    public static final String RESPONDENT_1_NAME = "John Smith, Respondent 1";

    @BeforeEach
    void before() {
        caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                .solicitor(RespondentSolicitor.builder().email(RESPONDENT_EMAIL).build()).build()))
            .others(Others.builder().firstOther(Other.builder().name("Joe Bloggs").build()).build())
            .build();

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        given(inboxLookupService.getRecipients(
            builder()
                .caseData(caseData)
                .excludeLegalRepresentatives(true)
                .build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
    }

    @Test
    void shouldNotifyLAWhenApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .caseUrl("caseUrl")
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C110A_APPLICATION), 12345L))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION), ""));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyLAWhenInterlocutoryApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME + ", Applicant")
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C2_APPLICATION), 12345L))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION), LOCAL_AUTHORITY_NAME + ", Applicant"));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyRespondentWhenInterlocutoryApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(RESPONDENT_1_NAME)
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C2_APPLICATION), 12345L))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION), RESPONDENT_1_NAME));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            Set.of(RESPONDENT_EMAIL),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyCtscWhenInterlocutoryApplicationPBAPaymentFails() {
        String applicant = LOCAL_AUTHORITY_NAME + ", Applicant";
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(applicant)
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(caseData, List.of(C2_APPLICATION), applicant))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION), applicant));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Joe Bloggs, Other to be given notice 1", "David Smith"})
    void shouldNotNotifyApplicantWhenInterlocutoryApplicationPBAPaymentFailsAndApplicantIsOthers(String applicant) {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(applicant)
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(caseData, List.of(C2_APPLICATION), applicant))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION), applicant));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyCtscWhenApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .caseUrl("caseUrl")
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(caseData, List.of(C110A_APPLICATION), ""))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION), ""));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

}
