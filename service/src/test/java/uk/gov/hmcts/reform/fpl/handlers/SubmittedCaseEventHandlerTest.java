package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.exception.RetryablePaymentException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_APPLICATION;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(MockitoExtension.class)
class SubmittedCaseEventHandlerTest {

    public static final String EMAIL = "test@test.com";
    private static final long CASE_ID = 12345L;
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;
    private static final DocumentReference SUBMITTED_FORM = mock(DocumentReference.class);

    @Mock
    private NotificationService notificationService;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private CourtService courtService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Mock
    private EventService eventService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TranslationRequestService translationRequestService;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler;

    @Test
    void shouldSendEmailToHmctsAdminIfToggleOn() {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);

        final String email = "test@test.com";
        final SubmitCaseHmctsTemplate parameters = mock(SubmitCaseHmctsTemplate.class);

        when(caseData.getId()).thenReturn(CASE_ID);
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);
        when(courtService.getCourtEmail(caseData)).thenReturn(email);
        when(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData)).thenReturn(parameters);

        submittedCaseEventHandler.notifyAdmin(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(notificationService).sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, CASE_ID);
    }

    @Test
    void shouldNotSendEmailToHmctsAdminIfToggleOff() {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);

        final String email = "test@test.com";
        final SubmitCaseHmctsTemplate parameters = mock(SubmitCaseHmctsTemplate.class);

        when(caseData.getId()).thenReturn(CASE_ID);
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(false);

        submittedCaseEventHandler.notifyAdmin(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(notificationService, never()).sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, CASE_ID);
    }

    @Test
    void shouldSendEmailToCafcass() {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);

        final SubmitCaseCafcassTemplate parameters = mock(SubmitCaseCafcassTemplate.class);

        when(caseData.getCaseLaOrRelatingLa()).thenReturn(LOCAL_AUTHORITY_CODE);
        when(caseData.getId()).thenReturn(CASE_ID);
        when(cafcassLookupConfiguration.getCafcassWelsh(LOCAL_AUTHORITY_CODE))
            .thenReturn(Optional.of(new Cafcass(LOCAL_AUTHORITY_CODE, EMAIL)));
        when(cafcassEmailContentProvider.buildCafcassSubmissionNotification(caseData)).thenReturn(parameters);

        submittedCaseEventHandler.notifyCafcass(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(notificationService).sendEmail(CAFCASS_SUBMISSION_TEMPLATE, EMAIL, parameters, CASE_ID);
    }

    @Test
    void shouldSendEmailToCafcassFromSendGrid() {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);
        final DocumentReference documentReference = DocumentReference.builder()
                .type(NEW_APPLICATION.getLabel())
                .build();
        C110A c110A = C110A.builder()
            .submittedForm(documentReference)
            .build();

        final NewApplicationCafcassData parameters = mock(NewApplicationCafcassData.class);

        when(caseData.getCaseLaOrRelatingLa()).thenReturn(LOCAL_AUTHORITY_CODE);
        when(cafcassEmailContentProvider.buildCafcassSubmissionSendGridData(caseData)).thenReturn(parameters);
        when(caseData.getC110A()).thenReturn(c110A);
        when(cafcassLookupConfiguration.getCafcassEngland(LOCAL_AUTHORITY_CODE))
                .thenReturn(Optional.of(new Cafcass(LOCAL_AUTHORITY_CODE, EMAIL)));

        submittedCaseEventHandler.notifyCafcassSendGrid(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(cafcassNotificationService).sendEmail(caseData,
            Set.of(documentReference),
            CafcassRequestEmailContentProvider.NEW_APPLICATION, parameters);
    }

    @Test
    void shouldNotSendEmailToCafcassFromSendGridWhenRepresentativeIsNotLA() {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);
        final DocumentReference documentReference = DocumentReference.builder()
                .type(NEW_APPLICATION.getLabel())
                .build();

        final NewApplicationCafcassData parameters = mock(NewApplicationCafcassData.class);

        submittedCaseEventHandler.notifyCafcassSendGrid(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(cafcassNotificationService, never()).sendEmail(caseData,
            Set.of(documentReference),
            CafcassRequestEmailContentProvider.NEW_APPLICATION, parameters);
    }

    @Test
    void shouldNotifyTranslationTeam() {
        final CaseData caseData = CaseData.builder()
            .c110A(C110A.builder()
                .submittedFormTranslationRequirements(TRANSLATION_REQUIREMENTS)
                .submittedForm(SUBMITTED_FORM)
                .build())
            .build();
        final CaseData caseDataBefore = mock(CaseData.class);

        submittedCaseEventHandler.notifyTranslationTeam(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(translationRequestService).sendRequest(caseData,
            Optional.of(TRANSLATION_REQUIREMENTS),
            SUBMITTED_FORM, "Application (C110A)");
        verifyNoMoreInteractions(translationRequestService);

    }

    @Test
    void shouldNotifyNotifyTranslationTeamIfNoLanguageRequirementDefaultsToEmpty() {
        final CaseData caseData = CaseData.builder()
            .c110A(C110A.builder()
                .submittedForm(SUBMITTED_FORM)
                .build())
            .build();
        final CaseData caseDataBefore = mock(CaseData.class);

        submittedCaseEventHandler.notifyTranslationTeam(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(translationRequestService).sendRequest(caseData,
            Optional.of(LanguageTranslationRequirement.NO),
            SUBMITTED_FORM, "Application (C110A)");

        verifyNoMoreInteractions(translationRequestService);
    }


    @Test
    void shouldExecuteAsynchronously() {
        assertClass(SubmittedCaseEventHandler.class).hasAsyncMethods(
            "notifyAdmin",
            "notifyCafcass",
            "makePayment",
            "notifyTranslationTeam",
            "notifyCafcassSendGrid"
        );
    }

    @Nested
    class Payment {

        @Test
        void shouldNotPayIfCaseIsReturned() {
            final CaseData caseData = CaseData.builder()
                .state(SUBMITTED)
                .build();
            final CaseData caseDataBefore = CaseData.builder()
                .state(RETURNED)
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(paymentService, eventService);
        }

        @Test
        void shouldNotPayAndEmitFailureEventIfPaymentDecisionsIsNotPresent() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
            verifyNoMoreInteractions(paymentService, eventService);
        }

        @Test
        void shouldNotPayAndEmitFailureEventIfPaymentDecisionsIsNo() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("No")
                .build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
            verifyNoMoreInteractions(paymentService, eventService);
        }

        @Test
        void shouldEmitFailureEventWhenPaymentFailed() {
            CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            final Exception exception = new PaymentsApiException("", new RuntimeException());

            doThrow(exception).when(paymentService).makePaymentForCaseOrders(caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
        }

        @Test
        void shouldEmitFailureEventWhenPaymentFailedOnRetryablePaymentException() {
            CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            final Exception exception = new RetryablePaymentException("", new RuntimeException());

            doThrow(exception).when(paymentService).makePaymentForCaseOrders(caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
        }

        @Test
        void shouldPayWhenCaseIsOpenedAndPaymentDecisionIsYes() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(eventService);
        }
    }

}
