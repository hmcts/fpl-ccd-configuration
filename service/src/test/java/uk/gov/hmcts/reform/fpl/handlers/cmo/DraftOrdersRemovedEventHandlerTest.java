package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRemovedEvent;
import uk.gov.hmcts.reform.fpl.handlers.DraftOrdersRemovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderRemovedCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersRemovedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
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
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class DraftOrdersRemovedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final UUID REMOVED_ORDER_ID = UUID.randomUUID();
    private static final UUID OTHER_ORDER_ID = UUID.randomUUID();
    private static final UUID HEARING_ORDER_BUNDLE_ID = UUID.randomUUID();
    private static final String REMOVAL_REASON = "Removal reason";
    private static final Element<HearingBooking> HEARING = hearingWithJudgeEmail(ALLOCATED_JUDGE_EMAIL_ADDRESS);
    private static final JudgeAndLegalAdvisor JUDGE = HEARING.getValue().getJudgeAndLegalAdvisor();
    private static final Element<HearingOrder> ORDER_TO_BE_REMOVED = element(REMOVED_ORDER_ID, HearingOrder.builder()
        .status(DRAFT)
        .type(HearingOrderType.DRAFT_CMO)
        .build());
    private static final DraftOrdersRemovedTemplate DRAFT_ORDERS_REMOVED_TEMPLATE_DATA =
        DraftOrdersRemovedTemplate.builder().removalReason(REMOVAL_REASON).build();

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

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @InjectMocks
    private DraftOrdersRemovedEventHandler underTest;

    @Test
    void shouldSendNotification() {
        HearingOrder additionalOrder = HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build();

        List<Element<HearingOrder>> caseManagementOrdersBefore = Stream.of(ORDER_TO_BE_REMOVED,
                element(OTHER_ORDER_ID, additionalOrder))
            .collect(Collectors.toList());

        HearingOrdersBundle hearingOrdersBundleBefore = HearingOrdersBundle.builder()
            .hearingId(HEARING.getId())
            .orders(caseManagementOrdersBefore).build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .allocatedJudge(allocatedJudge())
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_ORDER_BUNDLE_ID, hearingOrdersBundleBefore)
            ))
            .hearingDetails(List.of(HEARING))
            .build();

        List<Element<HearingOrder>> caseManagementOrdersAfter = List.of(element(OTHER_ORDER_ID, additionalOrder));

        CaseData caseDataAfter = caseDataBefore.toBuilder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_ORDER_BUNDLE_ID,
                    hearingOrdersBundleBefore.toBuilder().orders(caseManagementOrdersAfter).build())
            ))
            .build();

        when(draftOrdersRemovedContentProvider.buildContent(
            caseDataBefore, Optional.of(HEARING.getValue()), JUDGE, ORDER_TO_BE_REMOVED.getValue(), REMOVAL_REASON)
        ).thenReturn(DRAFT_ORDERS_REMOVED_TEMPLATE_DATA);
        when(courtService.getCourtEmail(any())).thenReturn(COURT_EMAIL_ADDRESS);
        when(localAuthorityRecipients.getRecipients(any())).thenReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        underTest.sendNotification(new DraftOrdersRemovedEvent(caseDataAfter, caseDataBefore, ORDER_TO_BE_REMOVED,
            REMOVAL_REASON));

        // send to Judge
        verify(notificationService).sendEmail(DRAFT_ORDER_REMOVED_TEMPLATE_FOR_JUDGES,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
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
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, COURT_EMAIL_ADDRESS),
            DRAFT_ORDERS_REMOVED_TEMPLATE_DATA,
            CASE_ID);
    }

    @Test
    void shouldSendNotificationToCafcass() {
        when(cafcassLookupConfiguration.getCafcassEngland(any())).thenReturn(
            Optional.of(
                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
            ));
        underTest.sendNotificationToCafcass(new DraftOrdersRemovedEvent(CaseData.builder().build(),
            CaseData.builder().build(), ORDER_TO_BE_REMOVED, REMOVAL_REASON));
        verify(cafcassNotificationService).sendEmail(
            CaseData.builder().build(),
            ORDER,
            OrderRemovedCafcassData.builder().documentName("draft order").removalReason(REMOVAL_REASON).build());
    }

    private static Element<HearingBooking> hearingWithJudgeEmail(String email) {
        return element(HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(hearingJudge(email))
            .build());
    }

    private static JudgeAndLegalAdvisor hearingJudge(String email) {
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
