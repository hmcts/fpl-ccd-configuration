package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DraftsOrdersUploadedEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DraftOrdersUploadedContentProvider contentProvider;

    @InjectMocks
    private DraftOrdersUploadedEventHandler eventHandler;

    @Test
    void shouldSendNotificationToHearingJudge() {

        final Element<HearingBooking> hearing1 = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle bundle1 = ordersBundle(hearing1.getId(), AGREED_CMO, C21);
        final HearingOrdersBundle selectedHearingBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21, C21);

        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing1, selectedHearing))
            .hearingOrdersBundlesDrafts(wrapElements(bundle1, selectedHearingBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId())
            .build();

        final DraftOrdersUploadedTemplate emailCustomization = emailCustomization();

        when(contentProvider.buildContent(any(), any(), any(), any())).thenReturn(emailCustomization);

        eventHandler.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            selectedHearing.getValue().getJudgeAndLegalAdvisor().getJudgeEmailAddress(),
            emailCustomization,
            caseData.getId().toString()
        );

        verifyNoMoreInteractions(notificationService);

        verify(contentProvider).buildContent(
            caseData,
            selectedHearing.getValue(),
            selectedHearing.getValue().getJudgeAndLegalAdvisor(),
            unwrapElements(selectedHearingBundle.getOrders()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldSendNotificationToAllocatedJudgeIfNoHearingJudgeEmailPresent(String hearingJudgeEmail) {

        final Element<HearingBooking> hearing1 = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail(hearingJudgeEmail);

        final HearingOrdersBundle bundle1 = ordersBundle(hearing1.getId(), AGREED_CMO, C21);
        final HearingOrdersBundle selectedHearingBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21, C21);

        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing1, selectedHearing))
            .hearingOrdersBundlesDrafts(wrapElements(bundle1, selectedHearingBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId())
            .build();

        final DraftOrdersUploadedTemplate emailCustomization = emailCustomization();

        when(contentProvider.buildContent(any(), any(), any(), any())).thenReturn(emailCustomization);

        eventHandler.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            caseData.getAllocatedJudge().getJudgeEmailAddress(),
            emailCustomization,
            caseData.getId().toString()
        );

        verifyNoMoreInteractions(notificationService);

        verify(contentProvider).buildContent(
            caseData,
            selectedHearing.getValue(),
            caseData.getAllocatedJudge(),
            unwrapElements(selectedHearingBundle.getOrders()));
    }

    @Test
    void shouldNotSendEmailIfNoHearingOrders() {

        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .allocatedJudge(allocatedJudge())
            .hearingOrdersBundlesDrafts(null)
            .build();

        eventHandler.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailIfNoJudgeEmail() {

        final Element<HearingBooking> hearing1 = hearingWithJudgeEmail(null);

        final HearingOrdersBundle bundle = ordersBundle(hearing1.getId(), C21);

        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .hearingDetails(List.of(hearing1))
            .hearingOrdersBundlesDrafts(ElementUtils.wrapElements(bundle))
            .lastHearingOrderDraftsHearingId(hearing1.getId())
            .allocatedJudge(null)
            .build();

        eventHandler.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verifyNoInteractions(notificationService);
    }

    private static Element<HearingBooking> hearingWithJudgeEmail(String email) {

        return element(HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(hearingJudge(email))
            .build());
    }

    private static HearingOrdersBundle ordersBundle(UUID hearingId, HearingOrderType... hearingOrderTypes) {

        List<HearingOrder> hearingOrders = Stream.of(hearingOrderTypes).map(hearingOrderType -> HearingOrder.builder()
            .type(hearingOrderType)
            .title(hearingOrderType.toString())
            .order(TestDataHelper.testDocumentReference())
            .build())
            .collect(Collectors.toList());

        return HearingOrdersBundle.builder()
            .hearingId(hearingId)
            .orders(ElementUtils.wrapElements(hearingOrders))
            .build();
    }

    private static JudgeAndLegalAdvisor hearingJudge(String email) {

        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Matthews")
            .judgeEmailAddress(email)
            .build();
    }

    private static Judge allocatedJudge() {

        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .judgeEmailAddress("allocated@test.com")
            .build();
    }

    private static DraftOrdersUploadedTemplate emailCustomization() {

        return DraftOrdersUploadedTemplate.builder()
            .judgeName(randomAlphanumeric(10))
            .caseUrl(randomAlphanumeric(10))
            .respondentLastName(randomAlphanumeric(10))
            .draftOrders(randomAlphanumeric(10))
            .build();
    }
}
