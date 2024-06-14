package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.ApproveOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseManagementOrderIssuedEventHandlerTest {

    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final long CASE_ID = 12345L;
    private static final IssuedCMOTemplate DIGITAL_REP_CMO_TEMPLATE_DATA = mock(IssuedCMOTemplate.class);
    private static final IssuedCMOTemplate EMAIL_REP_CMO_TEMPLATE_DATA = mock(IssuedCMOTemplate.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final HearingOrder CMO = mock(HearingOrder.class);
    private static final DocumentReference ORDER = mock(DocumentReference.class);
    private static final CaseManagementOrderIssuedEvent EVENT = new CaseManagementOrderIssuedEvent(CASE_DATA, CMO);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CaseManagementOrderEmailContentProvider cmoContentProvider;
    @Mock
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private TranslationRequestService translationRequestService;
    @Mock
    private CafcassNotificationService cafcassNotificationService;
    @Mock
    private WorkAllocationTaskService workAllocationTaskService;


    @InjectMocks
    private CaseManagementOrderIssuedEventHandler underTest;

    @BeforeEach
    void init() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(CMO.getOrder()).willReturn(ORDER);
    }

    @Test
    void shouldNotifyAdmin() {
        underTest.notifyAdmin(EVENT);
        verify(issuedOrderAdminNotificationHandler).notifyAdmin(CASE_DATA, ORDER, IssuedOrderType.CMO);
    }

    static Stream<Boolean> provideBooleanValues() {
        return Stream.of(true, false, null);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldNotifyLocalAuthority(Boolean urgency) {
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        if (urgency != null) {
            given(CASE_DATA.getOrderReviewUrgency()).willReturn(ApproveOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.from(urgency)))
                .build());
        }
        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(CASE_DATA).build()
        )).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyLocalAuthority(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            DIGITAL_REP_CMO_TEMPLATE_DATA,
            CASE_ID
        );
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldGovNotifyCafcassWelsh(Boolean urgency) {
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(CASE_DATA.getCaseLaOrRelatingLa()).willReturn(LOCAL_AUTHORITY_CODE);
        if (urgency != null) {
            given(CASE_DATA.getOrderReviewUrgency()).willReturn(ApproveOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.from(urgency)))
                .build());
        }
        given(cafcassLookupConfiguration.getCafcassWelsh(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, DIGITAL_SERVICE))
            .willReturn(EMAIL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyCafcass(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "FamilyPublicLaw+cafcass@gmail.com",
            EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID
        );
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldNotGovNotifyCafcassWhenCafcassIsEngland(Boolean urgency) {
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(CASE_DATA.getCaseLaOrRelatingLa()).willReturn(LOCAL_AUTHORITY_CODE);
        if (urgency != null) {
            given(CASE_DATA.getOrderReviewUrgency()).willReturn(ApproveOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.from(urgency)))
                .build());
        }
        given(cafcassLookupConfiguration.getCafcassWelsh(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.empty());

        underTest.notifyCafcass(EVENT);

        verify(notificationService, never()).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "FamilyPublicLaw+cafcass@gmail.com",
            EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID
        );
    }

    @Test
    void shouldSendGridNotifyToCafcassEngland() {
        given(CASE_DATA.getCaseLaOrRelatingLa()).willReturn(LOCAL_AUTHORITY_CODE);
        given(cafcassLookupConfiguration.getCafcassEngland(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));

        underTest.notifyCafcassViaSendGrid(EVENT);

        verify(cafcassNotificationService).sendEmail(
            isA(CaseData.class),
                any(),
                same(CafcassRequestEmailContentProvider.ORDER),
            isA(OrderCafcassData.class)
        );
    }

    @Test
    void shouldNotSendGridNotifyToCafcassWhenCafcassIsNotEngland() {
        given(CASE_DATA.getCaseLaOrRelatingLa()).willReturn(LOCAL_AUTHORITY_CODE);
        given(cafcassLookupConfiguration.getCafcassEngland(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.empty());

        underTest.notifyCafcassViaSendGrid(EVENT);

        verify(cafcassNotificationService, never()).sendEmail(
                isA(CaseData.class),
                any(),
                same(CafcassRequestEmailContentProvider.ORDER),
                isA(OrderCafcassData.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldNotifyEmailRepresentativesExcludingUnselectedOthersWhenServingOthersIsEnabled(Boolean urgency) {
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        if (urgency != null) {
            given(CASE_DATA.getOrderReviewUrgency()).willReturn(ApproveOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.from(urgency)))
                .build());
        }
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL))
            .willReturn(newHashSet("barney@rubble.com", "andrew@rubble.com"));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(CASE_DATA), any(), any()))
            .willReturn(newHashSet("andrew@rubble.com"));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, EMAIL))
            .willReturn(EMAIL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyEmailRepresentatives(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "barney@rubble.com", EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldNotifyDigitalRepresentativesAndExcludeUnselectedOthersWhenServingOthersIsEnabled(Boolean urgency) {
        if (urgency != null) {
            given(CASE_DATA.getOrderReviewUrgency()).willReturn(ApproveOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.from(urgency)))
                .build());
        }
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE))
            .willReturn(newHashSet("fred@flinstone.com", "barney@rubble.com"));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_CMO_TEMPLATE_DATA);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE), eq(CASE_DATA), any(), any()))
            .willReturn(newHashSet("barney@rubble.com"));

        underTest.notifyDigitalRepresentatives(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "fred@flinstone.com", DIGITAL_REP_CMO_TEMPLATE_DATA, CASE_ID);
    }

    @Test
    void shouldNotifyPostRepresentativesWhenServingOthersIsEnabled() {
        Other other1 = Other.builder().name("other1")
            .address(Address.builder().postcode("SW1").build()).build();
        Recipient otherRecipient1 = other1.toParty();
        Recipient otherRecipient2 = Other.builder().name("other2")
            .address(Address.builder().postcode("SW2").build()).build().toParty();
        Recipient otherRecipient3 = Other.builder().name("other3")
            .address(Address.builder().postcode("SW3").build()).build().toParty();

        given(sendDocumentService.getStandardRecipients(CASE_DATA))
            .willReturn(newArrayList(otherRecipient1, otherRecipient2));

        List<Element<Other>> selectedOthers = wrapElements(other1);
        given(CMO.getSelectedOthers()).willReturn(selectedOthers);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), any(), any()))
            .willReturn(newHashSet(otherRecipient1));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers))
            .willReturn(Set.of(otherRecipient3));

        underTest.sendDocumentToPostRepresentatives(EVENT);

        verify(sendDocumentService).sendDocuments(
            CASE_DATA, List.of(ORDER), newArrayList(otherRecipient2, otherRecipient3));
    }

    @Test
    void shouldNotNotifyPostRepresentativesWhenTranslationIsRequired() {
        underTest.sendDocumentToPostRepresentatives(new CaseManagementOrderIssuedEvent(CASE_DATA, HearingOrder.builder()
            .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
            .build()));

        verifyNoInteractions(coreCaseDataService, sendDocumentService);
    }

    @Test
    void shouldNotifyTranslationTeam() {
        underTest.notifyTranslationTeam(
            CaseManagementOrderIssuedEvent.builder()
                .caseData(CASE_DATA)
                .cmo(HearingOrder.builder()
                    .status(CMOStatus.APPROVED)
                    .order(ORDER)
                    .dateIssued(LocalDate.of(2020, 1, 2))
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .build()
                ).build()
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            ORDER, "Sealed case management order issued on 2 January 2020");
    }

    @Test
    void shouldNotifyTranslationTeamIfNoTranslationRequirements() {
        underTest.notifyTranslationTeam(
            CaseManagementOrderIssuedEvent.builder()
                .caseData(CASE_DATA)
                .cmo(HearingOrder.builder()
                    .status(CMOStatus.APPROVED)
                    .order(ORDER)
                    .dateIssued(LocalDate.of(2020, 1, 2))
                    .translationRequirements(null)
                    .build()
                ).build()
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(LanguageTranslationRequirement.NO),
            ORDER, "Sealed case management order issued on 2 January 2020");
    }

    @Test
    void shouldCreateWorkAllocationTaskWhenCMOApproved() {
        underTest.createWorkAllocationTask(EVENT);

        verify(workAllocationTaskService).createWorkAllocationTask(CASE_DATA,
            WorkAllocationTaskType.CMO_REVIEWED);
    }
}
