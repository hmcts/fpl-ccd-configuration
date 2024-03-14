package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_OR_UDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_OR_UDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_OR_UDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_OR_UDO_AND_NOP;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class GatekeepingOrderEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final SDONotifyData NOTIFY_DATA = mock(SDONotifyData.class);
    private static final DocumentReference ORDER = testDocumentReference();
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENT = ENGLISH_TO_WELSH;
    private static final String ORDER_TITLE = "Document Description";
    private static final DocumentReference DOCUMENT_C6 = DocumentReference.builder()
        .filename("notice_of_proceedings_c6.pdf")
        .build();
    private static final DocumentReference DOCUMENT_C6A = DocumentReference.builder()
        .filename("notice_of_proceedings_c6a.pdf")
        .build();

    @Mock
    private NotificationService notificationService;
    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private CafcassLookupConfiguration cafcassLookup;
    @Mock
    private CtscEmailLookupConfiguration ctscLookup;
    @Mock
    private SDOIssuedContentProvider standardContentProvider;
    @Mock
    private SDOIssuedCafcassContentProvider cafcassContentProvider;
    @Mock
    private TranslationRequestService translationRequestService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private GatekeepingOrderEventHandler underTest;

    @Test
    void shouldNotifyCafcassOfIssuedSDO() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .build();

        final GatekeepingOrderEvent event = gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, caseData);

        given(cafcassLookup.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassContentProvider.getNotifyData(caseData, ORDER, SDO)).willReturn(NOTIFY_DATA);

        underTest.notifyCafcass(event);

        verify(notificationService).sendEmail(
            SDO_OR_UDO_AND_NOP_ISSUED_CAFCASS,
            CAFCASS_EMAIL_ADDRESS,
            NOTIFY_DATA,
            CASE_ID
        );
    }

    @Test
    void shouldNotifyCafcassOfIssuedStandaloneSDO() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .relatingLA(LOCAL_AUTHORITY_CODE)
            .build();

        final GatekeepingOrderEvent event = gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, caseData);

        given(cafcassLookup.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassContentProvider.getNotifyData(caseData, ORDER, SDO)).willReturn(NOTIFY_DATA);

        underTest.notifyCafcass(event);

        verify(notificationService).sendEmail(
            SDO_OR_UDO_AND_NOP_ISSUED_CAFCASS,
            CAFCASS_EMAIL_ADDRESS,
            NOTIFY_DATA,
            CASE_ID
        );
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedSDO() {
        final CaseData caseData = CaseData.builder().id(CASE_ID).build();

        final GatekeepingOrderEvent event = gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, caseData);

        given(standardContentProvider.buildNotificationParameters(caseData, SDO)).willReturn(NOTIFY_DATA);

        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder()
                .caseData(caseData)
                .build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        underTest.notifyLocalAuthority(event);

        verify(notificationService).sendEmail(
            SDO_OR_UDO_AND_NOP_ISSUED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA,
            CASE_ID);
    }

    @Test
    void shouldNotifyCTSCOfIssuedSDO() {
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);

        final CaseData caseData = CaseData.builder().id(CASE_ID).build();

        final GatekeepingOrderEvent event = gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, caseData);

        given(standardContentProvider.buildNotificationParameters(caseData, SDO)).willReturn(NOTIFY_DATA);

        given(ctscLookup.getEmail()).willReturn(CTSC_INBOX);

        underTest.notifyCTSC(event);

        verify(notificationService).sendEmail(
            SDO_OR_UDO_AND_NOP_ISSUED_CTSC,
            CTSC_INBOX,
            NOTIFY_DATA,
            CASE_ID
        );
    }

    @Test
    void shouldNotNotifyCTSCOfIssuedSDOIfToggleOff() {
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(false);

        final CaseData caseData = CaseData.builder().id(CASE_ID).build();

        final GatekeepingOrderEvent event = gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, caseData);

        underTest.notifyCTSC(event);

        verify(notificationService, never()).sendEmail(
            SDO_OR_UDO_AND_NOP_ISSUED_CTSC,
            CTSC_INBOX,
            NOTIFY_DATA,
            CASE_ID
        );
    }


    @Test
    void shouldNotifyTranslationTeam() {
        underTest.notifyTranslationTeam(
            gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, CASE_DATA).toBuilder()
                .languageTranslationRequirement(TRANSLATION_REQUIREMENT)
                .build()
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENT),
            ORDER, ORDER_TITLE);
    }

    @Test
    void shouldNotifyNotifyTranslationTeamIfNoLanguageRequirementDefaultsToEmpty() {
        underTest.notifyTranslationTeam(
            gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, CASE_DATA)
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.empty(),
            ORDER, ORDER_TITLE);

        verifyNoMoreInteractions(translationRequestService);
    }

    @Test
    void shouldNotifyNotifyTranslationTeamIfNoticeOfProceedingsAreAttached() {
        CaseData caseData = CaseData.builder()
            .noticeOfProceedingsBundle(List.of(
                element(DocumentBundle.builder().document(DOCUMENT_C6).build()),
                element(DocumentBundle.builder().document(DOCUMENT_C6A).build())
            )).build();

        underTest.notifyTranslationTeam(
            gatekeepingOrderEvent(SDO_OR_UDO_AND_NOP, caseData)
                .toBuilder()
                .languageTranslationRequirement(TRANSLATION_REQUIREMENT)
                .build()
        );

        verify(translationRequestService).sendRequest(caseData,
            Optional.of(TRANSLATION_REQUIREMENT),
            ORDER, ORDER_TITLE);

        verify(translationRequestService).sendRequest(caseData,
            Optional.of(TRANSLATION_REQUIREMENT),
            DOCUMENT_C6, "Notice of proceedings (C6)");

        verify(translationRequestService).sendRequest(caseData,
            Optional.of(TRANSLATION_REQUIREMENT),
            DOCUMENT_C6A, "Notice of proceedings (C6A)");

        verifyNoMoreInteractions(translationRequestService);
    }

    private GatekeepingOrderEvent gatekeepingOrderEvent(GatekeepingOrderNotificationGroup group, CaseData caseData) {
        return GatekeepingOrderEvent.builder()
            .order(ORDER)
            .orderTitle(ORDER_TITLE)
            .notificationGroup(group)
            .caseData(caseData)
            .directionsOrderType(SDO)
            .build();
    }
}
