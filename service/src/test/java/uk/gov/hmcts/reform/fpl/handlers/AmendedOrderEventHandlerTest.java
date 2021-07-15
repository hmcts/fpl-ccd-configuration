package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AmendedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;
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
    private static final String AMENDED_ORDER_TYPE = "case management order";
    private static final Set<String> DIGITAL_REPS = new HashSet<>(Arrays.asList(DIGITAL_REP_1, DIGITAL_REP_2));
    private static final Long CASE_ID = 12345L;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocumentReference TEST_DOCUMENT = mock(DocumentReference.class);
    private static final OrderAmendedNotifyData NOTIFY_DATA = mock(OrderAmendedNotifyData.class);
    private static final List<Element<Other>> SELECTED_OTHERS = List.of(element(mock(Other.class)));
    private static final AmendedOrderEvent EVENT = new AmendedOrderEvent(CASE_DATA, TEST_DOCUMENT, AMENDED_ORDER_TYPE,
        SELECTED_OTHERS);
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
    private OtherRecipientsInbox otherRecipientsInbox;

    @InjectMocks
    private AmendedOrderEventHandler underTest;

    @BeforeEach
    void before() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()
        )).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(amendedOrderEmailContentProvider.getNotifyData(CASE_DATA, TEST_DOCUMENT, "case management order"))
            .willReturn(NOTIFY_DATA);

        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(
            DIGITAL_REPS);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyPartiesOnOrderSubmission() {
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL),
            eq(CASE_DATA),
            eq(SELECTED_OTHERS),
            any()))
            .willReturn((Set) Set.of(EMAIL_REP_1));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE),
            eq(CASE_DATA),
            eq(SELECTED_OTHERS),
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
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPost() {
        final Representative representative = mock(Representative.class);
        final Representative representative2 = mock(Representative.class);
        final RespondentParty otherRespondent = mock(RespondentParty.class);

        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(List.of(representative,representative2));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST),
            eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .willReturn((Set) Set.of(representative));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS))
            .willReturn(Set.of(otherRespondent));

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA,
            List.of(TEST_DOCUMENT),
            List.of(representative2, otherRespondent));

        verifyNoInteractions(notificationService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendOrderToLAOnlyWhenOrderTypeIsSDO() {
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL),
            eq(CASE_DATA),
            eq(SELECTED_OTHERS),
            any()))
            .willReturn((Set) Set.of(EMAIL_REP_1));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE),
            eq(CASE_DATA),
            eq(SELECTED_OTHERS),
            any()))
            .willReturn((Set) Set.of(DIGITAL_REP_1));
        given(amendedOrderEmailContentProvider.getNotifyData(CASE_DATA, TEST_DOCUMENT, "standard direction order"))
            .willReturn(NOTIFY_DATA);

        AmendedOrderEvent event = new AmendedOrderEvent(CASE_DATA, TEST_DOCUMENT, "standard direction order",
            SELECTED_OTHERS);

        underTest.notifyParties(event);

        verify(notificationService).sendEmail(
            ORDER_AMENDED_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA,
            CASE_ID.toString()
        );

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA,
            Set.of(EMAIL_REP_2),
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA,
            Set.of(DIGITAL_REP_2),
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );
    }
}
