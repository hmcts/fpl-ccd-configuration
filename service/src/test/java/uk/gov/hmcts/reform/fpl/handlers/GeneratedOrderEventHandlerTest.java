package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeneratedOrderEventHandlerTest {

    private static final Set<String> EMAIL_REPS = new HashSet<>(Arrays.asList("barney@rubble.com"));
    private static final Set<String> DIGITAL_REPS = new HashSet<>(Arrays.asList("fred@flinstones.com"));
    private static final Long CASE_ID = 12345L;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocumentReference TEST_DOCUMENT = mock(DocumentReference.class);
    private static final GeneratedOrderEvent EVENT = new GeneratedOrderEvent(CASE_DATA, TEST_DOCUMENT);
    private static final OrderIssuedNotifyData NOTIFY_DATA_WITH_CASE_URL = mock(OrderIssuedNotifyData.class);
    private static final OrderIssuedNotifyData NOTIFY_DATA_WITHOUT_CASE_URL = mock(OrderIssuedNotifyData.class);

    @Mock
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private GeneratedOrderEventHandler underTest;

    @BeforeEach
    void before() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()
        )).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER))
            .willReturn(NOTIFY_DATA_WITH_CASE_URL);

        given(orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(
            CASE_DATA, EVENT.getOrderDocument(), GENERATED_ORDER
        )).willReturn(NOTIFY_DATA_WITHOUT_CASE_URL);

        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(representativesInbox.getEmailsByPreferenceExcludingOthers(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreferenceExcludingOthers(CASE_DATA, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REPS);
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmissionWhenSendOrderToOthersToggledOff() {
        given(featureToggleService.isSendOrderToOthersEnabled()).willReturn(false);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);

        underTest.notifyParties(EVENT);

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER);

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA_WITH_CASE_URL,
            CASE_ID.toString()
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITH_CASE_URL,
            DIGITAL_REPS,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITHOUT_CASE_URL,
            EMAIL_REPS,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES
        );
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmissionWhenSendOrderToOthersToggledOn() {
        given(featureToggleService.isSendOrderToOthersEnabled()).willReturn(true);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);

        underTest.notifyParties(EVENT);

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER);

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA_WITH_CASE_URL,
            CASE_ID.toString()
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITH_CASE_URL,
            DIGITAL_REPS,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITHOUT_CASE_URL,
            EMAIL_REPS,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES
        );
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenEmailRepsDoNotExistAndToggledOn() {
        given(featureToggleService.isSendOrderToOthersEnabled()).willReturn(true);
        given(representativesInbox.getEmailsByPreferenceExcludingOthers(CASE_DATA, EMAIL)).willReturn(new HashSet<>());

        underTest.notifyParties(EVENT);

        verify(orderIssuedEmailContentProvider, never()).getNotifyDataWithoutCaseUrl(any(), any(), any());

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            any(), any(), anySet(), eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES)
        );
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenEmailRepsDoNotExistAndToggledOff() {
        given(featureToggleService.isSendOrderToOthersEnabled()).willReturn(false);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(Set.of());

        underTest.notifyParties(EVENT);

        verify(orderIssuedEmailContentProvider, never()).getNotifyDataWithoutCaseUrl(any(), any(), any());

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            any(), any(), anySet(), eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES)
        );
    }

    @Test
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPostAndToggledOn() {
        final Representative representative = mock(Representative.class);
        final RespondentParty respondent = mock(RespondentParty.class);
        final List<Recipient> recipients = List.of(representative, respondent);

        given(featureToggleService.isSendOrderToOthersEnabled()).willReturn(true);
        given(sendDocumentService.getSelectedOtherRecipients(CASE_DATA, null)).willReturn(recipients);
        given(sendDocumentService.getRecipientsExcludingOthers(CASE_DATA)).willReturn(Arrays.asList(representative,
            respondent));

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(TEST_DOCUMENT), recipients);
        verify(sendDocumentService).getRecipientsExcludingOthers(CASE_DATA);
        verify(sendDocumentService).getSelectedOtherRecipients(CASE_DATA, Collections.emptyList());

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPostAndToggledOff() {
        final Representative representative = mock(Representative.class);
        final RespondentParty respondent = mock(RespondentParty.class);
        final List<Recipient> recipients = List.of(representative, respondent);

        given(featureToggleService.isSendOrderToOthersEnabled()).willReturn(false);
        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(recipients);

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(TEST_DOCUMENT), recipients);
        verify(sendDocumentService).getStandardRecipients(CASE_DATA);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }
}
