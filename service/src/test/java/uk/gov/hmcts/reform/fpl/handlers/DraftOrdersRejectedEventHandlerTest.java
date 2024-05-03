package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersRejectedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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

//    @Test
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
