package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersRejectedEventHandler;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;

@ExtendWith(SpringExtension.class)
class DraftOrdersRejectedEventHandlerTest {

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReviewDraftOrdersEmailContentProvider reviewDraftOrdersEmailContentProvider;

    @InjectMocks
    private DraftOrdersRejectedEventHandler draftOrdersRejectedEventHandler;

//    TODO
//     @Test
//    void shouldNotifyLocalAuthorityOfRejectedOrders() {
//        UUID hearingId = randomUUID();
//        Element<HearingBooking> hearing = element(hearingId, HearingBooking.builder().build());
//
//        CaseData caseData = CaseData.builder()
//            .id(12345L)
//            .hearingDetails(List.of(hearing))
//            .lastHearingOrderDraftsHearingId(hearingId)
//            .build();
//
//        List<HearingOrder> orders = List.of();
//
//        RejectedOrdersTemplate expectedTemplate = RejectedOrdersTemplate.builder().build();
//
//        given(localAuthorityRecipients.getRecipients(any())).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
//
//        given(reviewDraftOrdersEmailContentProvider.buildOrdersRejectedContent(caseData, hearing.getValue(), orders))
//            .willReturn(expectedTemplate);
//
//        draftOrdersRejectedEventHandler.sendNotificationToLA(
//            new DraftOrdersRejected(caseData, orders));
//
//        final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
//            .caseData(caseData)
//            .secondaryLocalAuthorityExcluded(true)
//            .build();
//
//        verify(notificationService).sendEmail(
//            JUDGE_REJECTS_DRAFT_ORDERS,
//            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
//            expectedTemplate,
//            caseData.getId());
//
//        verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
//    }
}
