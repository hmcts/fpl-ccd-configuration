package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.exception.RetryablePaymentException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class SubmittedCaseEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Mock
    private RespondentSolicitorContentProvider respondentSolicitorContentProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler;

    @Test
    void shouldSendEmailToHmctsAdmin() {
        final String expectedEmail = "test@test.com";
        final CaseData caseData = caseData().toBuilder()
            .submittedForm(DocumentReference.builder().binaryUrl("testUrl").build())
            .build();
        final CaseData caseDataBefore = caseData();
        final SubmitCaseHmctsTemplate expectedTemplate = SubmitCaseHmctsTemplate.builder().build();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        when(adminNotificationHandler.getHmctsAdminEmail(caseData)).thenReturn(expectedEmail);
        when(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData))
            .thenReturn(expectedTemplate);

        submittedCaseEventHandler.notifyAdmin(submittedCaseEvent);

        verify(notificationService).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }

    @Test
    void shouldSendEmailToCafcass() {
        final String expectedEmail = "test@test.com";
        final CaseData caseData = caseData();
        final CaseData caseDataBefore = caseData();
        final CafcassLookupConfiguration.Cafcass cafcass =
            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, expectedEmail);
        final SubmitCaseCafcassTemplate expectedTemplate = SubmitCaseCafcassTemplate.builder().build();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);
        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE)).thenReturn(cafcass);
        when(cafcassEmailContentProvider.buildCafcassSubmissionNotification(any(CaseData.class)))
            .thenReturn(expectedTemplate);

        submittedCaseEventHandler.notifyCafcass(submittedCaseEvent);

        verify(notificationService).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }

    @Nested
    class RespondentSolicitorNotification {

        @Test
        void shouldSendEmailToRegisteredSolicitors() {
            final String recipient1 = "solicitor1@test.com";
            final String recipient2 = "solicitor2@test.com";

            final RespondentSolicitor solicitor1 = RespondentSolicitor.builder()
                .email(recipient1)
                .firstName("First")
                .lastName("Respondent")
                .organisation(Organisation.builder().organisationID("111").organisationName("org1").build())
                .build();

            final RespondentSolicitor solicitor2 = RespondentSolicitor.builder()
                .email(recipient2)
                .firstName("Second")
                .lastName("Respondent")
                .organisation(Organisation.builder().organisationID("222").organisationName("org2").build())
                .build();

            final CaseData caseData = caseData().toBuilder()
                .respondents1(wrapElements(
                    Respondent.builder().legalRepresentation("Yes").solicitor(solicitor1).build(),
                    Respondent.builder().legalRepresentation("Yes").solicitor(solicitor2).build()))
                .build();

            final CaseData caseDataBefore = caseData();
            final RespondentSolicitorTemplate expectedTemplate = RespondentSolicitorTemplate.builder().build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
                caseData, solicitor1)).thenReturn(expectedTemplate);

            when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
                caseData, solicitor2)).thenReturn(expectedTemplate);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verify(notificationService).sendEmail(
                REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
                recipient1,
                expectedTemplate,
                caseData.getId());

            verify(notificationService).sendEmail(
                REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
                recipient2,
                expectedTemplate,
                caseData.getId());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"No"})
        void shouldNotSendEmailWhenLegalRepresentationIsNotSetToTheRespondent(String legalRepresentation) {
            final Respondent respondent = Respondent.builder().legalRepresentation(legalRepresentation).build();

            final CaseData caseData = caseData().toBuilder()
                .respondents1(wrapElements(respondent)).build();

            final CaseData caseDataBefore = caseData();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verifyNoInteractions(respondentSolicitorContentProvider);
        }

        @Test
        void shouldNotSendEmailWhenRespondentsWithSolicitorAreEmpty() {
            final Respondent respondentWithoutSolicitor = Respondent.builder().build();

            final CaseData caseData = caseData().toBuilder()
                .respondents1(wrapElements(respondentWithoutSolicitor)).build();

            final CaseData caseDataBefore = caseData();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verifyNoInteractions(respondentSolicitorContentProvider);
        }

        @Test
        void shouldNotSendEmailWhenRegisteredSolicitorsAreEmpty() {
            final Respondent respondentWithUnregisteredSolicitor = Respondent.builder().solicitor(
                RespondentSolicitor.builder()
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name("org name").build()).build())
                .build();

            final CaseData caseData = caseData().toBuilder()
                .respondents1(wrapElements(respondentWithUnregisteredSolicitor)).build();

            final CaseData caseDataBefore = caseData();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verifyNoInteractions(respondentSolicitorContentProvider);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotSendEmailWhenRegisteredSolicitorsEmailsAreEmpty(String solicitorEmail) {
            final Respondent respondent1 = Respondent.builder()
                .solicitor(
                    RespondentSolicitor.builder()
                        .firstName("First")
                        .lastName("Respondent1")
                        .email(solicitorEmail)
                        .organisation(Organisation.builder().organisationID("123").build())
                        .build())
                .build();

            final CaseData caseData = caseData().toBuilder().respondents1(wrapElements(respondent1)).build();

            final CaseData caseDataBefore = caseData();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verifyNoInteractions(respondentSolicitorContentProvider);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotSendEmailWhenRegisteredSolicitorsOrganisationIdIsEmpty(String organisationId) {
            final Respondent respondent = Respondent.builder()
                .solicitor(
                    RespondentSolicitor.builder()
                        .firstName("First")
                        .lastName("Respondent1")
                        .email("test@test.com")
                        .organisation(Organisation.builder().organisationID(organisationId).build())
                        .build())
                .build();

            final CaseData caseData = caseData().toBuilder().respondents1(wrapElements(respondent)).build();

            final CaseData caseDataBefore = caseData();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verifyNoInteractions(respondentSolicitorContentProvider);
        }

        @Test
        void shouldNotSendEmailWhenNoRespondentsExist() {
            final CaseData caseData = caseData().toBuilder().respondents1(emptyList()).build();

            final CaseData caseDataBefore = caseData();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

            verifyNoInteractions(respondentSolicitorContentProvider);
        }

    }

    @Nested
    class Payment {

        @ParameterizedTest
        @EnumSource(value = State.class, mode = EXCLUDE, names = {"OPEN"})
        void shouldNotPayIfCaseStateIsDifferentThanOpen(State state) {
            final CaseData caseData = CaseData.builder()
                .state(RETURNED)
                .build();
            final CaseData caseDataBefore = CaseData.builder()
                .state(SUBMITTED)
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(paymentService, applicationEventPublisher);
        }

        @Test
        void shouldNotPayAndEmitFailureEventIfPaymentDecisionsIsNotPresent() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(applicationEventPublisher).publishEvent(new FailedPBAPaymentEvent(caseData, C110A_APPLICATION));
            verifyNoMoreInteractions(paymentService, applicationEventPublisher);
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

            verify(applicationEventPublisher)
                .publishEvent(new FailedPBAPaymentEvent(caseData, C110A_APPLICATION));
            verifyNoMoreInteractions(paymentService, applicationEventPublisher);
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

            verify(applicationEventPublisher)
                .publishEvent(new FailedPBAPaymentEvent(caseData, C110A_APPLICATION));
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

            verify(applicationEventPublisher)
                .publishEvent(new FailedPBAPaymentEvent(caseData, C110A_APPLICATION));
        }

        @Test
        void shouldPayWhenCaseIsOpenedAndPaymentDesicionIsYes() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(applicationEventPublisher);
        }
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(SubmittedCaseEventHandler.class).hasAsyncMethods(
            "notifyAdmin",
            "notifyCafcass",
            "makePayment",
            "notifyRegisteredRespondentSolicitors");
    }

}
