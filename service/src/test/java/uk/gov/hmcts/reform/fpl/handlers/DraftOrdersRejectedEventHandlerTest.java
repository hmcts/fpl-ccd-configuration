package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersRejectedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS_DESIGNATED_LA;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class DraftOrdersRejectedEventHandlerTest {

    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReviewDraftOrdersEmailContentProvider reviewDraftOrdersEmailContentProvider;

    @InjectMocks
    private DraftOrdersRejectedEventHandler underTest;

    @Test
    void shouldNotifyDesignatedLocalAuthorityIfOrderRejectedUploadedByAnonymous() {
        UUID hearingId = randomUUID();
        Element<HearingBooking> hearing = element(hearingId, HearingBooking.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(hearing))
            .lastHearingOrderDraftsHearingId(hearingId)
            .build();

        List<HearingOrder> orders = List.of(HearingOrder.builder().build());

        RejectedOrdersTemplate expectedTemplate = RejectedOrdersTemplate.builder().build();

        given(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(any()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(reviewDraftOrdersEmailContentProvider.buildOrdersRejectedContent(caseData, hearing.getValue(), orders))
            .willReturn(expectedTemplate);

        underTest.sendNotifications(new DraftOrdersRejected(caseData, orders));

        verify(notificationService).sendEmail(
            JUDGE_REJECTS_DRAFT_ORDERS_DESIGNATED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            caseData.getId());
    }

    @Test
    void shouldNotifyDesignatedLocalAuthorityIfOrderRejectedUploadedByDesignatedLA() {
        UUID hearingId = randomUUID();
        Element<HearingBooking> hearing = element(hearingId, HearingBooking.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(hearing))
            .lastHearingOrderDraftsHearingId(hearingId)
            .build();

        List<HearingOrder> orders = List.of(HearingOrder.builder().uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
            .build());

        RejectedOrdersTemplate expectedTemplate = RejectedOrdersTemplate.builder().build();

        given(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(any()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(reviewDraftOrdersEmailContentProvider.buildOrdersRejectedContent(caseData, hearing.getValue(), orders))
            .willReturn(expectedTemplate);

        underTest.sendNotifications(new DraftOrdersRejected(caseData, orders));

        verify(notificationService).sendEmail(
            JUDGE_REJECTS_DRAFT_ORDERS_DESIGNATED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            caseData.getId());
    }
}
