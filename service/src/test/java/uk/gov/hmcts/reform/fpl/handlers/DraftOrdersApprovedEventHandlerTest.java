package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersApprovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class DraftOrdersApprovedEventHandlerTest {

    @Mock
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @Mock
    private RepresentativeNotificationService representativeNotificationService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReviewDraftOrdersEmailContentProvider reviewDraftOrdersEmailContentProvider;

    @InjectMocks
    private DraftOrdersApprovedEventHandler draftOrdersApprovedEventHandler;

    @Test
    void shouldNotifyAdminAndLAOfApprovedOrders() {
        UUID hearingId = randomUUID();
        Element<HearingBooking> hearing = element(hearingId, HearingBooking.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(hearing))
            .lastHearingOrderDraftsHearingId(hearingId)
            .build();

        List<HearingOrder> orders = List.of();
        ApprovedOrdersTemplate expectedTemplate = ApprovedOrdersTemplate.builder().build();

        when(adminNotificationHandler.getHmctsAdminEmail(caseData))
            .thenReturn(CTSC_INBOX);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, hearing.getValue(), orders, DIGITAL_SERVICE)).willReturn(expectedTemplate);

        draftOrdersApprovedEventHandler.sendNotificationToAdminAndLA(new DraftOrdersApproved(caseData, orders));

        verify(notificationService).sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            caseData.getId().toString());

        verify(notificationService).sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            CTSC_INBOX,
            expectedTemplate,
            caseData.getId().toString());

        verifyNoMoreInteractions(notificationService, representativeNotificationService);
    }

    @Test
    void shouldNotifyCafcassAndRepresentatives() {
        UUID hearingId = randomUUID();
        Element<HearingBooking> hearing = element(hearingId, HearingBooking.builder().build());

        List<Representative> digitalReps = unwrapElements(createRepresentatives(DIGITAL_SERVICE));
        List<Representative> emailReps = unwrapElements(createRepresentatives(EMAIL));
        List<Representative> reps = Stream.of(digitalReps, emailReps)
            .flatMap(Collection::stream).collect(Collectors.toList());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDetails(List.of(hearing))
            .representatives(wrapElements(reps))
            .lastHearingOrderDraftsHearingId(hearingId)
            .build();

        List<HearingOrder> orders = List.of();
        ApprovedOrdersTemplate expectedTemplate = ApprovedOrdersTemplate.builder().build();

        CafcassLookupConfiguration.Cafcass cafcass =
            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS);

        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE)).thenReturn(cafcass);

        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, hearing.getValue(), orders, DIGITAL_SERVICE)).willReturn(expectedTemplate);

        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, hearing.getValue(), orders, EMAIL)).willReturn(expectedTemplate);

        draftOrdersApprovedEventHandler.sendNotificationToCafcassAndRepresentatives(
            new DraftOrdersApproved(caseData, orders));

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            12345L,
            expectedTemplate,
            digitalReps,
            JUDGE_APPROVES_DRAFT_ORDERS
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            12345L,
            expectedTemplate,
            emailReps,
            JUDGE_APPROVES_DRAFT_ORDERS
        );

        verify(notificationService).sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            CAFCASS_EMAIL_ADDRESS,
            expectedTemplate,
            caseData.getId().toString());

        verifyNoMoreInteractions(notificationService, representativeNotificationService);
    }
}
