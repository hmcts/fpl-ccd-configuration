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
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
class DraftOrdersRejectedEventHandlerTest {

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReviewDraftOrdersEmailContentProvider reviewDraftOrdersEmailContentProvider;

    @InjectMocks
    private DraftOrdersRejectedEventHandler draftOrdersRejectedEventHandler;

    private static final LocalDate SOME_DATE = LocalDate.of(2020, 2, 20);

    @Test
    void shouldNotifyLocalAuthorityOfRejectedOrders() {
        UUID hearingId = randomUUID();
        Element<HearingBooking> hearing = element(hearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(hearing))
            .respondents1(createRespondents())
            .lastHearingOrderDraftsHearingId(hearingId)
            .build();

        List<HearingOrder> orders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .build());

        RejectedOrdersTemplate expectedTemplate = RejectedOrdersTemplate.builder().build();

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(reviewDraftOrdersEmailContentProvider.buildOrdersRejectedContent(caseData, hearing.getValue(), orders))
            .willReturn(expectedTemplate);

        draftOrdersRejectedEventHandler.sendNotificationToLA(
            new DraftOrdersRejected(caseData, orders));

        verify(notificationService).sendEmail(
            JUDGE_REJECTS_DRAFT_ORDERS,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            caseData.getId().toString());
    }
}
