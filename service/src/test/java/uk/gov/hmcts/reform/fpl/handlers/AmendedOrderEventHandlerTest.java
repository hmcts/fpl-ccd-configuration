package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.events.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AmendedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
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
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AmendedOrderEventHandlerTest {

    private static final String EMAIL_REP_1 = "barney@rubble.com";
    private static final String EMAIL_REP_2 = "barney2@rubble.com";
    private static final Set<String> EMAIL_REPS = new HashSet<>(Arrays.asList(EMAIL_REP_1, EMAIL_REP_2));
    private static final String DIGITAL_REP_1 = "fred@flinstones.com";
    private static final String DIGITAL_REP_2 = "fred2@flinstones.com";
    private static final Set<String> DIGITAL_REPS = new HashSet<>(Arrays.asList(DIGITAL_REP_1, DIGITAL_REP_2));
    private static final Long CASE_ID = 12345L;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocumentReference TEST_DOCUMENT = mock(DocumentReference.class);
    private static final AmendedOrderEvent EVENT = new AmendedOrderEvent(CASE_DATA, TEST_DOCUMENT);
    private static final OrderAmendedNotifyData NOTIFY_DATA = mock(OrderAmendedNotifyData.class);
    private static final List<Element<Other>> NO_RECIPIENTS = Collections.emptyList();
    private static final List<Element<Other>> LAST_GENERATED_ORDER_OTHERS = List.of(element(mock(Other.class)));

    @Mock
    private GeneratedOrder lastGeneratedOrder;
    @Mock
    private AmendedOrderEmailContentProvider amendedOrderEmailContentProvider;
    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private SealedOrderHistoryService sealedOrderHistoryService;
    @Mock
    private OthersService othersService;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;

    @InjectMocks
    private AmendedOrderEventHandler underTest;

    @BeforeEach
    void before() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()
        )).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(amendedOrderEmailContentProvider.getNotifyData(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER))
            .willReturn(NOTIFY_DATA);

        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(
            DIGITAL_REPS);
        given(othersService.getSelectedOthers(CASE_DATA)).willReturn(Collections.emptyList());
        given(sealedOrderHistoryService.lastGeneratedOrder(any())).willReturn(lastGeneratedOrder);
    }


    @Test
    void shouldNotifyPartiesOnOrderSubmissionWhenOldOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(false);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(CASE_DATA), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());
        given(otherRecipientsInbox.getNonSelectedRecipients(
            eq(DIGITAL_SERVICE), eq(CASE_DATA), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyParties(EVENT);

        verify(notificationService).sendEmail(
            ORDER_AMENDED_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA,
            CASE_ID.toString()
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA,
            DIGITAL_REPS,
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA,
            EMAIL_REPS,
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyPartiesOnOrderSubmissionWhenNewOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(true);
        given(lastGeneratedOrder.getOthers()).willReturn(LAST_GENERATED_ORDER_OTHERS);

        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL),
            eq(CASE_DATA),
            eq(LAST_GENERATED_ORDER_OTHERS),
            any()))
            .willReturn((Set) Set.of(EMAIL_REP_1));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE),
            eq(CASE_DATA),
            eq(LAST_GENERATED_ORDER_OTHERS),
            any()))
            .willReturn((Set) Set.of(DIGITAL_REP_1));

        underTest.notifyParties(EVENT);

        verify(notificationService).sendEmail(
            ORDER_AMENDED_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA,
            CASE_ID.toString()
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA,
            Set.of(DIGITAL_REP_2),
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA,
            Set.of(EMAIL_REP_2),
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPostAndNewOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(true);
        given(lastGeneratedOrder.getOthers()).willReturn(LAST_GENERATED_ORDER_OTHERS);
        final Representative representative = mock(Representative.class);
        final Representative representative2 = mock(Representative.class);
        final RespondentParty otherRespondent = mock(RespondentParty.class);

        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(List.of(representative,representative2));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST),
            eq(CASE_DATA), eq(LAST_GENERATED_ORDER_OTHERS), any()))
            .willReturn((Set) Set.of(representative));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(LAST_GENERATED_ORDER_OTHERS))
            .willReturn(Set.of(otherRespondent));

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA,
            List.of(TEST_DOCUMENT),
            List.of(representative2, otherRespondent));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPostAndOldOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(false);
        final Representative representative = mock(Representative.class);
        final RespondentParty respondent = mock(RespondentParty.class);
        final List<Recipient> recipients = List.of(representative, respondent);

        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(recipients);

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(TEST_DOCUMENT), recipients);
        verify(sendDocumentService).getStandardRecipients(CASE_DATA);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }
}
