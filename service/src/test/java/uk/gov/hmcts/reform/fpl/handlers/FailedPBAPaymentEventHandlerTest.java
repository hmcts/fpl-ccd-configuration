package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType.FAILED_PAYMENT;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest.builder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FailedPBAPaymentEventHandler.class, LookupTestConfig.class, WorkAllocationTaskService.class})
class FailedPBAPaymentEventHandlerTest {

    @MockBean
    private RequestData requestData;

    @MockBean
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;
    @MockBean
    private WorkAllocationTaskService workAllocationTaskService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private FailedPBAPaymentEventHandler failedPBAPaymentEventHandler;

    private CaseData caseData;

    private static final Long CASE_ID = 12345L;
    private static final String RESPONDENT_EMAIL = "john.smith@test.com";
    private static final String RESPONDENT_1_NAME = "John Smith, Respondent 1";
    private static final String DESIGNATED_LA_EMAIL_1 = "designated1@test.com";
    private static final String DESIGNATED_LA_EMAIL_2 = "designated2@test.com";
    private static final String SECONDARY_LA_EMAIL_1 = "secondary1@test.com";
    private static final String SECONDARY_LA_EMAIL_2 = "secondary2@test.com";

    @BeforeEach
    void before() {
        caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .respondents1(wrapElements(
                Respondent.builder().party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                    .solicitor(RespondentSolicitor.builder().email(RESPONDENT_EMAIL).build()).build(),
                Respondent.builder().party(RespondentParty.builder().firstName("Ross").lastName("Bob").build())
                    .solicitor(RespondentSolicitor.builder().build()).build(),
                Respondent.builder().party(RespondentParty.builder().firstName("Timothy").lastName("Jones").build())
                    .build()))
            .othersV2(wrapElements(Other.builder().firstName("Joe Bloggs").build()))
            .build();

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);

        given(localAuthorityRecipients.getRecipients(
            builder()
                .caseData(caseData)
                .legalRepresentativesExcluded(true)
                .build()))
            .willReturn(
                Set.of(DESIGNATED_LA_EMAIL_1, DESIGNATED_LA_EMAIL_2, SECONDARY_LA_EMAIL_1, SECONDARY_LA_EMAIL_2));

        given(localAuthorityRecipients.getRecipients(
            builder()
                .caseData(caseData)
                .legalRepresentativesExcluded(true)
                .secondaryLocalAuthorityExcluded(true)
                .build()))
            .willReturn(Set.of(DESIGNATED_LA_EMAIL_1, DESIGNATED_LA_EMAIL_2));

        given(localAuthorityRecipients.getRecipients(
            builder()
                .caseData(caseData)
                .legalRepresentativesExcluded(true)
                .designatedLocalAuthorityExcluded(true)
                .build()))
            .willReturn(Set.of(SECONDARY_LA_EMAIL_1, SECONDARY_LA_EMAIL_2));
    }

    @Test
    void shouldNotifyLAWhenApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .caseUrl("caseUrl")
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C110A_APPLICATION), caseData))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            Set.of(DESIGNATED_LA_EMAIL_1, DESIGNATED_LA_EMAIL_2),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifySolicitorWhenApplicationPBAPaymentFails() {
        caseData = CaseData.builder()
            .id(CASE_ID)
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .localAuthorities(wrapElements(
                LocalAuthority.builder().name("Respondent Solicitor").email("test@test.com").build()
            ))
            .respondents1(wrapElements(
                Respondent.builder().party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                    .solicitor(RespondentSolicitor.builder().email(RESPONDENT_EMAIL).build()).build(),
                Respondent.builder().party(RespondentParty.builder().firstName("Ross").lastName("Bob").build())
                    .solicitor(RespondentSolicitor.builder().build()).build(),
                Respondent.builder().party(RespondentParty.builder().firstName("Timothy").lastName("Jones").build())
                    .build()))
            .othersV2(wrapElements(Other.builder().name("Joe Bloggs").build()))
            .build();

        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .caseUrl("caseUrl")
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C110A_APPLICATION), caseData))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "test@test.com",
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyDesignatedLAWhenTheirInterlocutoryApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME + ", Applicant")
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C2_APPLICATION), caseData))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            Set.of(DESIGNATED_LA_EMAIL_1, DESIGNATED_LA_EMAIL_2),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifySecondaryLAWhenTheirInterlocutoryApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME + ", Applicant")
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C2_APPLICATION), caseData))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData,
                List.of(C2_APPLICATION),
                OrderApplicant.builder()
                    .type(SECONDARY_LOCAL_AUTHORITY)
                    .name(caseData.getCaseLocalAuthorityName())
                    .build()));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            Set.of(SECONDARY_LA_EMAIL_1, SECONDARY_LA_EMAIL_2),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyRespondentWhenInterlocutoryApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN.getType())
            .caseUrl("caseUrl")
            .applicant(RESPONDENT_1_NAME)
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C1_APPOINTMENT_OF_A_GUARDIAN), caseData))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C1_APPOINTMENT_OF_A_GUARDIAN),
                OrderApplicant.builder().type(RESPONDENT).name("John Smith").build()));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            RESPONDENT_EMAIL,
            expectedParameters,
            caseData.getId().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Timothy Jones", "Ross Bob"})
    void shouldNotSendNotificationWhenRespondentSolicitorDetailsAreMissing(String respondentName) {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN.getType())
            .caseUrl("caseUrl")
            .applicant(RESPONDENT_1_NAME)
            .build();

        given(failedPBAPaymentContentProvider.getApplicantNotifyData(List.of(C1_APPOINTMENT_OF_A_GUARDIAN), caseData))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C1_APPOINTMENT_OF_A_GUARDIAN),
                OrderApplicant.builder().type(RESPONDENT).name(respondentName).build()));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyCtscWhenInterlocutoryApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME)
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(
            caseData, List.of(C2_APPLICATION), LOCAL_AUTHORITY_NAME))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyCtscWhenInterlocutoryApplicationPBAPaymentFailsAndToggledOff() {
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(false);

        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME)
            .build();

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService, never()).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyCtscWhenPBAPaymentFailsForOtherApplication() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME)
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(
            caseData, List.of(C1_APPOINTMENT_OF_A_GUARDIAN), LOCAL_AUTHORITY_NAME))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C1_APPOINTMENT_OF_A_GUARDIAN),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyCtscWhenPBAPaymentFailsForOtherApplicationAndToggledOff() {
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(false);

        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN.getType())
            .caseUrl("caseUrl")
            .applicant(LOCAL_AUTHORITY_NAME)
            .build();

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C1_APPOINTMENT_OF_A_GUARDIAN),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService, never()).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }


    @ParameterizedTest
    @MethodSource("otherApplicantsData")
    void shouldNotNotifyApplicantWhenInterlocutoryApplicationPBAPaymentFailsAndApplicantIsOthers(
        OrderApplicant applicant,
        String expectedApplicant) {

        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(expectedApplicant)
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(caseData, List.of(C2_APPLICATION), expectedApplicant))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, List.of(C2_APPLICATION), applicant));

        verifyNoInteractions(notificationService);
    }

    private static Stream<Arguments> otherApplicantsData() {
        return Stream.of(
            Arguments.of(OrderApplicant.builder().type(OTHER).name("Joe Bloggs").build(), "Joe Bloggs"),
            Arguments.of(OrderApplicant.builder().type(OTHER).name("David Smith").build(), "David Smith"));
    }

    @Test
    void shouldNotifyCtscWhenApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .caseUrl("caseUrl")
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(
            caseData, List.of(C110A_APPLICATION), LOCAL_AUTHORITY_NAME))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyCtscWhenApplicationPBAPaymentFailsAndToggledOff() {
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(false);

        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .caseUrl("caseUrl")
            .build();


        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(notificationService, never()).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }


    @Test
    void shouldCreateWorkAllocationTaskForFailedPBAPayment() {
        failedPBAPaymentEventHandler.createWorkAllocationTask(
            new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));

        verify(workAllocationTaskService).createWorkAllocationTask(caseData, FAILED_PAYMENT);
    }

}
