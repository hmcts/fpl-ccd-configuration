package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRemovedEvent;
import uk.gov.hmcts.reform.fpl.handlers.DraftOrdersRemovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersRemovedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersRemovedContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDER_REMOVED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDER_REMOVED_TEMPLATE_FOR_JUDGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class DraftOrdersRemovedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final DraftOrdersRemovedTemplate DRAFT_ORDERS_REMOVED_TEMPLATE_DATA = mock(
        DraftOrdersRemovedTemplate.class
    );
    private static final String REMOVAL_REASON = "Removal reason";

    @Mock
        private DraftOrdersRemovedContentProvider draftOrdersRemovedContentProvider;

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    private CourtService courtService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RepresentativeNotificationService representativeNotificationService;

    @InjectMocks
    private DraftOrdersRemovedEventHandler underTest;

    @Test
    void shouldSendNotification() {
        UUID removedOrderId = UUID.randomUUID();
        UUID additionalOrderId = UUID.randomUUID();
        UUID hearingOrderBundleId = UUID.randomUUID();

        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");

        final JudgeAndLegalAdvisor judge = hearing.getValue().getJudgeAndLegalAdvisor();

        Element<HearingOrder> orderToBeRemoved = element(removedOrderId, HearingOrder.builder()
            .status(DRAFT)
            .type(HearingOrderType.DRAFT_CMO)
            .build());

        HearingOrder additionalOrder = HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build();

        List<Element<HearingOrder>> caseManagementOrdersBefore = Stream.of(orderToBeRemoved,
                element(additionalOrderId, additionalOrder))
            .collect(Collectors.toList());

        HearingOrdersBundle hearingOrdersBundleBefore = HearingOrdersBundle.builder()
            .hearingId(hearing.getId())
            .orders(caseManagementOrdersBefore).build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .allocatedJudge(allocatedJudge())
            .hearingOrdersBundlesDrafts(List.of(
                element(hearingOrderBundleId, hearingOrdersBundleBefore)
            ))
            .hearingDetails(List.of(hearing))
            .build();

        List<Element<HearingOrder>> caseManagementOrdersAfter = List.of(element(additionalOrderId, additionalOrder));

        CaseData caseDataAfter = caseDataBefore.toBuilder()
            .hearingOrdersBundlesDrafts(List.of(
                element(hearingOrderBundleId,
                    hearingOrdersBundleBefore.toBuilder().orders(caseManagementOrdersAfter).build())
            ))
            .build();

        when(draftOrdersRemovedContentProvider.buildContent(
            caseDataBefore, Optional.of(hearing.getValue()), judge, orderToBeRemoved.getValue(), REMOVAL_REASON)
        ).thenReturn(DRAFT_ORDERS_REMOVED_TEMPLATE_DATA);
        when(courtService.getCourtEmail(any())).thenReturn("cort@email.com");
        when(localAuthorityRecipients.getRecipients(any())).thenReturn(Set.of("la@email.com"));

        underTest.sendNotification(new DraftOrdersRemovedEvent(caseDataAfter, caseDataBefore, orderToBeRemoved,
            REMOVAL_REASON));

        // send to Judge
        verify(notificationService).sendEmail(DRAFT_ORDER_REMOVED_TEMPLATE_FOR_JUDGES,
            "judge1@test.com",
            DRAFT_ORDERS_REMOVED_TEMPLATE_DATA,
            CASE_ID);
        // send to representatives
        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            DIGITAL_SERVICE,
            DRAFT_ORDER_REMOVED_TEMPLATE,
            DRAFT_ORDERS_REMOVED_TEMPLATE_DATA,
            caseDataBefore);
        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            EMAIL,
            DRAFT_ORDER_REMOVED_TEMPLATE,
            DRAFT_ORDERS_REMOVED_TEMPLATE_DATA,
            caseDataBefore);
        // send to admin and LA
        verify(notificationService).sendEmail(
            DRAFT_ORDER_REMOVED_TEMPLATE,
            Set.of("la@email.com", "cort@email.com"),
            DRAFT_ORDERS_REMOVED_TEMPLATE_DATA,
            CASE_ID);
    }

    @Test
    void shouldSendNotificationToCafcass() {
        // TBC
    }

    private Element<HearingBooking> hearingWithJudgeEmail(String email) {
        return element(HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(hearingJudge(email))
            .build());
    }

    private JudgeAndLegalAdvisor hearingJudge(String email) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Matthews")
            .judgeEmailAddress(email)
            .build();
    }

    private Judge allocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .judgeEmailAddress("allocated@test.com")
            .build();
    }
}
