package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DraftOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DraftsOrdersUploadedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final DraftOrdersUploadedTemplate DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA = mock(
        DraftOrdersUploadedTemplate.class
    );

    @Mock
    private NotificationService notificationService;

    @Mock
    private DraftOrdersUploadedContentProvider draftOrdersContentProvider;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @InjectMocks
    private DraftOrdersUploadedEventHandler underTest;

    @Test
    void shouldSendNotificationToCafcass() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                    Optional.of(
                            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                    )
            );
        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle bundle = ordersBundle(hearing.getId(), AGREED_CMO, C21);
        final HearingOrdersBundle selectedHearingBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21, C21);

        Set<DocumentReference> documentReferences = unwrapElements(selectedHearingBundle.getOrders()).stream()
                .map(HearingOrder::getOrder)
                .collect(Collectors.toSet());

        final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .allocatedJudge(allocatedJudge())
                .hearingDetails(List.of(hearing, selectedHearing))
                .hearingOrdersBundlesDrafts(wrapElements(bundle, selectedHearingBundle))
                .lastHearingOrderDraftsHearingId(selectedHearing.getId())
                .build();

        underTest.sendNotificationToCafcass(new DraftOrdersUploaded(caseData));

        verify(cafcassNotificationService).sendEmail(
            caseData,
            documentReferences,
            ORDER,
            OrderCafcassData.builder()
                .documentName("draft order")
                .hearingDate(selectedHearing.getValue().getStartDate())
                .build()
        );
    }

    @Test
    void shouldNotNotifyCafcassWhenHearingOrdersBundlesDraftReviewIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );
        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle bundle = ordersBundle(hearing.getId(), AGREED_CMO, C21);
        final HearingOrdersBundle selectedHearingBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21, C21);

        Set<DocumentReference> documentReferences = unwrapElements(selectedHearingBundle.getOrders()).stream()
            .map(HearingOrder::getOrder)
            .collect(Collectors.toSet());

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing, selectedHearing))
            .hearingOrdersBundlesDraftReview(wrapElements(bundle, selectedHearingBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId())
            .build();

        underTest.sendNotificationToCafcass(new DraftOrdersUploaded(caseData));

        verify(cafcassNotificationService, never()).sendEmail(
            same(caseData),
            any(),
            same(ORDER),
            any()
        );
    }

    @Test
    void shouldNotNotifyCafcassWhenHearingOrderIsNotCurrent() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                    Optional.of(
                            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                    )
            );

        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle bundle = ordersBundle(hearing.getId(), AGREED_CMO, C21);
        final HearingOrdersBundle selectedHearingBundleTemp = ordersBundle(
                selectedHearing.getId(),
                DRAFT_CMO,
                C21,
                C21
        );

        List<Element<HearingOrder>> collect = unwrapElements(selectedHearingBundleTemp.getOrders())
                .stream()
                .map(hearingOrder -> hearingOrder.toBuilder()
                        .dateSent(LocalDate.now().minusDays(2))
                        .build()
                )
                .map(Element::newElement)
                .collect(Collectors.toList());

        HearingOrdersBundle selectedHearingBundle = selectedHearingBundleTemp.toBuilder()
                .orders(collect)
                .build();

        final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .allocatedJudge(allocatedJudge())
                .hearingDetails(List.of(hearing, selectedHearing))
                .hearingOrdersBundlesDrafts(wrapElements(bundle, selectedHearingBundle))
                .lastHearingOrderDraftsHearingId(selectedHearing.getId())
                .build();

        underTest.sendNotificationToCafcass(new DraftOrdersUploaded(caseData));

        verify(cafcassNotificationService, never()).sendEmail(
            same(caseData),
            any(),
            same(ORDER),
            any()
        );
    }

    @Test
    void shouldNotNotifyCafcassWhenLAisNonEnglish() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                    Optional.empty()
            );

        final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .build();

        underTest.sendNotificationToCafcass(new DraftOrdersUploaded(caseData));

        verify(cafcassNotificationService, never()).sendEmail(
                same(caseData),
                any(),
                same(ORDER),
                any()
        );
    }

    static Stream<Boolean> provideBooleanValues() {
        return Stream.of(true, false, null);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldSendNotificationToHearingJudgeWhenDraftCMOUploaded(Boolean urgency) {
        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle draftCMOBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21);

        final JudgeAndLegalAdvisor judge = selectedHearing.getValue().getJudgeAndLegalAdvisor();

        final CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing, selectedHearing))
            .hearingOrdersBundlesDraftReview(wrapElements(draftCMOBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId());
        if (urgency != null) {
            builder.draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YesNo.from(urgency))).build());
        }
        CaseData caseData = builder.build();

        when(draftOrdersContentProvider.buildContent(
            caseData, selectedHearing.getValue(), judge,
            unwrapElements(draftCMOBundle.getOrders()),
            DRAFT_CMO
        )).thenReturn(DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA);

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            Boolean.TRUE.equals(urgency) ? URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE
                : DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA,
            CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldSendNotificationToHearingJudgeWhenDraftCMOUploadedWithAgreedCMOExist(Boolean urgency) {
        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle draftCMOBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21);
        final HearingOrdersBundle agreedCmoBundle = ordersBundle(hearing.getId(), AGREED_CMO, C21, C21);

        final JudgeAndLegalAdvisor judge = selectedHearing.getValue().getJudgeAndLegalAdvisor();

        final CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing, selectedHearing))
            .hearingOrdersBundlesDrafts(wrapElements(agreedCmoBundle))
            .hearingOrdersBundlesDraftReview(wrapElements(draftCMOBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId());
        if (urgency != null) {
            builder.draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YesNo.from(urgency))).build());
        }
        CaseData caseData = builder.build();

        when(draftOrdersContentProvider.buildContent(
            caseData, selectedHearing.getValue(), judge,
            unwrapElements(draftCMOBundle.getOrders()),
            DRAFT_CMO
        )).thenReturn(DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA);

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            Boolean.TRUE.equals(urgency) ? URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE
                : DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA,
            CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldSendNotificationToHearingJudgeWhenAgreedCMOUploaded(Boolean urgency) {
        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle agreedCmoBundle = ordersBundle(selectedHearing.getId(), AGREED_CMO, C21, C21);

        final JudgeAndLegalAdvisor judge = selectedHearing.getValue().getJudgeAndLegalAdvisor();

        final CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing, selectedHearing))
            .hearingOrdersBundlesDrafts(wrapElements(agreedCmoBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId());
        if (urgency != null) {
            builder.draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YesNo.from(urgency))).build());
        }
        CaseData caseData = builder.build();

        when(draftOrdersContentProvider.buildContent(
            caseData, selectedHearing.getValue(), judge,
            unwrapElements(agreedCmoBundle.getOrders()),
            AGREED_CMO
        )).thenReturn(DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA);

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            Boolean.TRUE.equals(urgency) ? URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE
                : DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA,
            CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldSendNotificationToHearingJudgeWhenAgreedCMOUploadedWithDraftCMOExist(Boolean urgency) {
        final Element<HearingBooking> hearing = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail("judge2@test.com");

        final HearingOrdersBundle draftCMOBundle = ordersBundle(hearing.getId(), DRAFT_CMO, C21);
        final HearingOrdersBundle agreedCmoBundle = ordersBundle(selectedHearing.getId(), AGREED_CMO, C21, C21);

        final JudgeAndLegalAdvisor judge = selectedHearing.getValue().getJudgeAndLegalAdvisor();

        final CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(allocatedJudge())
            .hearingDetails(List.of(hearing, selectedHearing))
            .hearingOrdersBundlesDrafts(wrapElements(agreedCmoBundle))
            .hearingOrdersBundlesDraftReview(wrapElements(draftCMOBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId());
        if (urgency != null) {
            builder.draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YesNo.from(urgency))).build());
        }
        CaseData caseData = builder.build();

        when(draftOrdersContentProvider.buildContent(
            caseData, selectedHearing.getValue(), judge,
            unwrapElements(agreedCmoBundle.getOrders()),
            AGREED_CMO
        )).thenReturn(DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA);

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            Boolean.TRUE.equals(urgency) ? URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE
                : DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA,
            CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    static Stream<Arguments> provideBooleanAndStringValues() {
        List<Arguments> args = new ArrayList<>();
        Stream.of(true, false, null).forEach(urgency -> {
            args.add(Arguments.of(urgency, null));
            args.add(Arguments.of(urgency, ""));
        });
        return args.stream();
    }
    @ParameterizedTest
    @MethodSource("provideBooleanAndStringValues")
    void shouldSendNotificationToAllocatedJudgeIfNoHearingJudgeEmailPresent(Boolean urgency, String hearingJudgeEmail) {
        final Element<HearingBooking> hearing1 = hearingWithJudgeEmail("judge1@test.com");
        final Element<HearingBooking> selectedHearing = hearingWithJudgeEmail(hearingJudgeEmail);

        final HearingOrdersBundle bundle1 = ordersBundle(hearing1.getId(), AGREED_CMO, C21);
        final HearingOrdersBundle selectedHearingBundle = ordersBundle(selectedHearing.getId(), DRAFT_CMO, C21, C21);

        final Judge judge = allocatedJudge();

        final CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(judge)
            .hearingDetails(List.of(hearing1, selectedHearing))
            .hearingOrdersBundlesDraftReview(wrapElements(bundle1, selectedHearingBundle))
            .lastHearingOrderDraftsHearingId(selectedHearing.getId());
        if (urgency != null) {
            builder.draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YesNo.from(urgency))).build());
        }
        CaseData caseData = builder.build();

        when(draftOrdersContentProvider.buildContent(
            caseData, selectedHearing.getValue(), judge, unwrapElements(selectedHearingBundle.getOrders()), DRAFT_CMO
        )).thenReturn(DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA);

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verify(notificationService).sendEmail(
            Boolean.TRUE.equals(urgency) ? URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE
                : DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            DRAFT_ORDERS_UPLOADED_TEMPLATE_DATA,
            CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailIfNoHearingOrders() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .allocatedJudge(allocatedJudge())
            .hearingOrdersBundlesDrafts(null)
            .build();

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailIfNoJudgeEmail() {

        final Element<HearingBooking> hearing1 = hearingWithJudgeEmail(null);

        final HearingOrdersBundle bundle = ordersBundle(hearing1.getId(), C21);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDetails(List.of(hearing1))
            .hearingOrdersBundlesDrafts(ElementUtils.wrapElements(bundle))
            .lastHearingOrderDraftsHearingId(hearing1.getId())
            .allocatedJudge(null)
            .build();

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        verifyNoInteractions(notificationService);
    }

    private Element<HearingBooking> hearingWithJudgeEmail(String email) {
        return element(HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(hearingJudge(email))
            .build());
    }

    private HearingOrdersBundle ordersBundle(UUID hearingId, HearingOrderType... hearingOrderTypes) {
        List<HearingOrder> hearingOrders = Stream.of(hearingOrderTypes).map(hearingOrderType -> HearingOrder.builder()
            .type(hearingOrderType)
            .title(hearingOrderType.toString())
            .order(TestDataHelper.testDocumentReference())
            .dateSent(LocalDate.now())
            .build())
            .collect(Collectors.toList());

        return HearingOrdersBundle.builder()
            .hearingId(hearingId)
            .orders(ElementUtils.wrapElements(hearingOrders))
            .build();
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
