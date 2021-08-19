package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
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
    private InboxLookupService inboxLookupService;
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
    private FeatureToggleService toggleService;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private TranslationRequestService translationRequestService;

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

    @Test
    void shouldNotifyLocalAuthority() {
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()
        )).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyLocalAuthority(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            DIGITAL_REP_CMO_TEMPLATE_DATA,
            String.valueOf(CASE_ID)
        );
    }

    @Test
    void shouldNotifyCafcass() {
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, EMAIL))
            .willReturn(EMAIL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyCafcass(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "FamilyPublicLaw+cafcass@gmail.com",
            EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldNotifyEmailRepresentativesExcludingUnselectedOthersWhenServingOthersIsEnabled(boolean toggle) {
        given(toggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(toggle);
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL))
            .willReturn(newHashSet("barney@rubble.com", "andrew@rubble.com"));
        if (toggle) {
            given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(CASE_DATA), any(), any()))
                .willReturn((Set) Set.of("andrew@rubble.com"));
        }
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, EMAIL))
            .willReturn(EMAIL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyEmailRepresentatives(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "barney@rubble.com", EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID);
        if (!toggle) {
            verify(notificationService).sendEmail(
                CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "barney@rubble.com", EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID);
            verify(notificationService).sendEmail(
                CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "andrew@rubble.com", EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldNotifyDigitalRepresentativesAndExcludeUnselectedOthersWhenServingOthersIsEnabled(boolean toggle) {
        given(toggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(toggle);
        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE))
            .willReturn(newHashSet("fred@flinstone.com", "barney@rubble.com"));
        given(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_CMO_TEMPLATE_DATA);
        if (toggle) {
            given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE), eq(CASE_DATA), any(), any()))
                .willReturn((Set) Set.of("barney@rubble.com"));
        }

        underTest.notifyDigitalRepresentatives(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "fred@flinstone.com", DIGITAL_REP_CMO_TEMPLATE_DATA, CASE_ID);
        if (!toggle) {
            verify(notificationService).sendEmail(
                CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "barney@rubble.com", DIGITAL_REP_CMO_TEMPLATE_DATA, CASE_ID);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyPostRepresentativesWhenServingOthersIsEnabled() {
        Other other1 = Other.builder().name("other1")
            .address(Address.builder().postcode("SW1").build()).build();
        Recipient otherRecipient1 = other1.toParty();
        Recipient otherRecipient2 = Other.builder().name("other2")
            .address(Address.builder().postcode("SW2").build()).build().toParty();
        Recipient otherRecipient3 = Other.builder().name("other3")
            .address(Address.builder().postcode("SW3").build()).build().toParty();

        given(toggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(sendDocumentService.getStandardRecipients(CASE_DATA))
            .willReturn(newArrayList(otherRecipient1, otherRecipient2));

        List<Element<Other>> selectedOthers = wrapElements(other1);
        given(CMO.getSelectedOthers()).willReturn(selectedOthers);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), any(), any()))
            .willReturn((Set) Set.of(otherRecipient1));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers))
            .willReturn(Set.of(otherRecipient3));

        underTest.sendDocumentToPostRepresentatives(EVENT);

        verify(sendDocumentService).sendDocuments(
            CASE_DATA, List.of(ORDER), newArrayList(otherRecipient2, otherRecipient3));
    }

    @Test
    void shouldNotifyPostRepresentativesWhenServingOthersIsDisabled() {
        underTest.sendDocumentToPostRepresentatives(EVENT);

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION, CASE_TYPE, CASE_ID, SEND_DOCUMENT_EVENT, Map.of("documentToBeSent", ORDER)
        );
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
}
