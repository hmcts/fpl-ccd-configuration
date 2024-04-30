package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeneratedOrderEventHandlerTest {

    private static final String EMAIL_REP_1 = "barney@rubble.com";
    private static final String EMAIL_REP_2 = "barney2@rubble.com";
    private static final Set<String> EMAIL_REPS = new HashSet<>(Arrays.asList(EMAIL_REP_1, EMAIL_REP_2));
    private static final String DIGITAL_REP_1 = "fred@flinstones.com";
    private static final String DIGITAL_REP_2 = "fred2@flinstones.com";
    private static final Set<String> DIGITAL_REPS = new HashSet<>(Arrays.asList(DIGITAL_REP_1, DIGITAL_REP_2));
    private static final Long CASE_ID = 12345L;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocumentReference TEST_DOCUMENT = mock(DocumentReference.class);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENT = LanguageTranslationRequirement.NO;
    private static final String ORDER_TITLE = "orderTitle";
    private static final GeneratedOrderEvent EVENT = new GeneratedOrderEvent(CASE_DATA, TEST_DOCUMENT,
        TRANSLATION_REQUIREMENT, ORDER_TITLE, LocalDate.now());
    private static final OrderIssuedNotifyData NOTIFY_DATA_WITH_CASE_URL = mock(OrderIssuedNotifyData.class);
    private static final OrderIssuedNotifyData NOTIFY_DATA_WITHOUT_CASE_URL = mock(OrderIssuedNotifyData.class);
    private static final List<Element<Other>> NO_RECIPIENTS = Collections.emptyList();
    private static final List<Element<Other>> LAST_GENERATED_ORDER_OTHERS = List.of(element(mock(Other.class)));

    @Mock
    private GeneratedOrder lastGeneratedOrder;
    @Mock
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private SealedOrderHistoryService sealedOrderHistoryService;
    @Mock
    private OthersService othersService;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;
    @Mock
    private CafcassNotificationService cafcassNotificationService;
    @Captor
    private ArgumentCaptor<OrderCafcassData> orderCaptor;
    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;
    @Mock
    private UserService userService;
    @Mock
    private WorkAllocationTaskService workAllocationTaskService;
    @Mock
    private JudicialService judicialService;

    @InjectMocks
    private GeneratedOrderEventHandler underTest;

    @BeforeEach
    void before() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);

        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(CASE_DATA).build()
        )).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER))
            .willReturn(NOTIFY_DATA_WITH_CASE_URL);

        given(orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(
            CASE_DATA, EVENT.getOrderDocument(), GENERATED_ORDER
        )).willReturn(NOTIFY_DATA_WITHOUT_CASE_URL);

        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(
            DIGITAL_REPS);
        given(othersService.getSelectedOthers(CASE_DATA)).willReturn(Collections.emptyList());
        given(sealedOrderHistoryService.lastGeneratedOrder(any())).willReturn(lastGeneratedOrder);
    }

    @Test
    void shouldCleanupRolesIfCaseClosed() {
        given(CASE_DATA.getState()).willReturn(State.CLOSED);
        underTest.cleanupRoles(EVENT);
        verify(judicialService).deleteAllRolesOnCase(CASE_ID);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = { "CLOSED" }, mode = EnumSource.Mode.EXCLUDE)
    void shouldNotCleanupRolesIfCaseIsNotClosed(State state) {
        given(CASE_DATA.getState()).willReturn(state);
        underTest.cleanupRoles(EVENT);
        verify(judicialService, never()).deleteAllRolesOnCase(CASE_ID);
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmissionWhenOldOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(false);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(CASE_DATA), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());
        given(otherRecipientsInbox.getNonSelectedRecipients(
            eq(DIGITAL_SERVICE), eq(CASE_DATA), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyParties(EVENT);

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER);

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA_WITH_CASE_URL,
            CASE_ID
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITH_CASE_URL,
            DIGITAL_REPS,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITHOUT_CASE_URL,
            EMAIL_REPS,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES
        );
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmissionWhenNewOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(true);
        given(lastGeneratedOrder.getOthers()).willReturn(LAST_GENERATED_ORDER_OTHERS);

        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL),
            eq(CASE_DATA),
            eq(LAST_GENERATED_ORDER_OTHERS),
            any()))
            .willReturn(Set.of(EMAIL_REP_1));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE),
            eq(CASE_DATA),
            eq(LAST_GENERATED_ORDER_OTHERS),
            any()))
            .willReturn(Set.of(DIGITAL_REP_1));

        underTest.notifyParties(EVENT);

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(CASE_DATA, TEST_DOCUMENT, GENERATED_ORDER);

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA_WITH_CASE_URL,
            CASE_ID
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITH_CASE_URL,
            Set.of(DIGITAL_REP_2),
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            NOTIFY_DATA_WITHOUT_CASE_URL,
            Set.of(EMAIL_REP_2),
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES
        );
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenEmailRepsDoNotExistAndNewOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(true);

        underTest.notifyParties(EVENT);

        verify(orderIssuedEmailContentProvider, never()).getNotifyDataWithoutCaseUrl(any(), any(), any());

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            any(), any(), anySet(), eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES)
        );
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenEmailRepsDoNotExistAndOldOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(false);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(Sets.newHashSet());

        underTest.notifyParties(EVENT);

        verify(orderIssuedEmailContentProvider, never()).getNotifyDataWithoutCaseUrl(any(), any(), any());

        verify(representativeNotificationService, never()).sendNotificationToRepresentatives(
            any(), any(), anySet(), eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES)
        );
    }

    @Test
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPostAndNewOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(true);
        given(lastGeneratedOrder.getOthers()).willReturn(LAST_GENERATED_ORDER_OTHERS);
        final Representative representative = mock(Representative.class);
        final Representative representative2 = mock(Representative.class);
        final RespondentParty otherRespondent = mock(RespondentParty.class);

        given(sendDocumentService.getStandardRecipients(CASE_DATA))
            .willReturn(List.of(representative, representative2));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST),
            eq(CASE_DATA), eq(LAST_GENERATED_ORDER_OTHERS), any()))
            .willReturn(Set.of(representative));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(LAST_GENERATED_ORDER_OTHERS))
            .willReturn(Set.of(otherRespondent));

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA,
            List.of(TEST_DOCUMENT),
            List.of(representative2, otherRespondent));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendOrderToRepresentativesAndNotRepresentedRespondentsByPostAndOldOrdersEvent() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(false);
        final Representative representative = mock(Representative.class);
        final RespondentParty respondent = mock(RespondentParty.class);
        final List<Recipient> recipients = List.of(representative, respondent);

        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(recipients);

        underTest.sendOrderByPost(EVENT);

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(TEST_DOCUMENT), recipients);
        verify(sendDocumentService).getStandardRecipients(CASE_DATA);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendOrderDoNotSentIfNeedTranslation() {
        given(lastGeneratedOrder.isNewVersion()).willReturn(false);
        given(lastGeneratedOrder.getNeedTranslation()).willReturn(YesNo.YES);

        underTest.sendOrderByPost(EVENT);

        verifyNoInteractions(sendDocumentService,notificationService);
    }

    @Test
    void shouldSendNotificationToCafcass() {
        UUID selectedHearingId = UUID.randomUUID();
        LocalDateTime hearingDateTime = LocalDateTime.of(
                LocalDate.of(2022, 5, 18),
                LocalTime.of(10, 30)
        );


        Element<HearingBooking> hearingBookingElementOne = Element.<HearingBooking>builder()
                .id(selectedHearingId)
                .value(HearingBooking.builder()
                        .startDate(hearingDateTime)
                        .build())
                .build();

        Element<HearingBooking> hearingBookingElementTwo = Element.<HearingBooking>builder()
                .id(selectedHearingId)
                .value(HearingBooking.builder()
                        .startDate(hearingDateTime.minusDays(10))
                        .build())
                .build();

        CaseData caseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .selectedHearingId(selectedHearingId)
                .hearingDetails(List.of(
                        hearingBookingElementOne,
                        hearingBookingElementTwo
                ))
                .build();

        String fileName = "dummyFile.doc";
        when(TEST_DOCUMENT.getFilename()).thenReturn(fileName);
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        var orderApprovalDate = LocalDate.now();
        GeneratedOrderEvent event = new GeneratedOrderEvent(caseData, TEST_DOCUMENT,
                TRANSLATION_REQUIREMENT, ORDER_TITLE, orderApprovalDate);
        underTest.notifyCafcass(event);
        verify(cafcassNotificationService).sendEmail(
            eq(caseData),
            eq(Set.of(TEST_DOCUMENT)),
            eq(ORDER),
            orderCaptor.capture());

        OrderCafcassData orderCafcassData = orderCaptor.getValue();
        assertThat(orderCafcassData.getDocumentName()).isEqualTo(fileName);
        assertThat(orderCafcassData.getHearingDate()).isEqualTo(hearingDateTime);
        assertThat(orderCafcassData.getOrderApprovalDate()).isEqualTo(orderApprovalDate);
    }

    @Test
    void shouldNotSendNotificationWhenCafcassIsNotEngland() {
        String fileName = "dummyFile.doc";
        when(TEST_DOCUMENT.getFilename()).thenReturn(fileName);
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.empty()
            );
        underTest.notifyCafcass(EVENT);
        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldCreateWorkAllocationTaskWhenJudgeUploadsOrder() {
        given(userService.isJudiciaryUser()).willReturn(true);
        underTest.createWorkAllocationTask(EVENT);

        verify(workAllocationTaskService).createWorkAllocationTask(CASE_DATA,
            WorkAllocationTaskType.ORDER_UPLOADED);
    }

    @Test
    void shouldNotCreateWorkAllocationTaskWhenNonJudgeUserUploadsOrder() {
        given(userService.isJudiciaryUser()).willReturn(false);
        underTest.createWorkAllocationTask(EVENT);

        verify(workAllocationTaskService, never()).createWorkAllocationTask(any(),
            any());
    }
}
