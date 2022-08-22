package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersApprovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DraftOrdersApprovedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final UUID HEARING_ID = randomUUID();
    private static final Element<HearingBooking> HEARING = element(HEARING_ID, HearingBooking.builder().build());
    private static final ApprovedOrdersTemplate EXPECTED_TEMPLATE = ApprovedOrdersTemplate.builder().build();
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS = ENGLISH_TO_WELSH;
    private static final DocumentReference ORDER = testDocumentReference();
    private static final DocumentReference ORDER_2 = testDocumentReference();

    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private CourtService courtService;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;
    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReviewDraftOrdersEmailContentProvider reviewDraftOrdersEmailContentProvider;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;
    @Mock
    private TranslationRequestService translationRequestService;
    @Mock
    private CafcassNotificationService cafcassNotificationService;
    @Captor
    private ArgumentCaptor<OrderCafcassData> orderCafcassDataArgumentCaptor;
    @Captor
    private ArgumentCaptor<Set<DocumentReference>> documentRefArgumentCaptor;

    @InjectMocks
    private DraftOrdersApprovedEventHandler underTest;

    @Test
    void shouldNotifyAdminAndLAOfApprovedOrders() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .hearingDetails(List.of(HEARING))
            .lastHearingOrderDraftsHearingId(HEARING_ID)
            .build();

        List<HearingOrder> orders = List.of();

        given(courtService.getCourtEmail(caseData)).willReturn(CTSC_INBOX);
        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, HEARING.getValue(), orders, DIGITAL_SERVICE)).willReturn(EXPECTED_TEMPLATE);

        underTest.sendNotificationToAdminAndLA(new DraftOrdersApproved(caseData, orders));

        verify(notificationService).sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            EXPECTED_TEMPLATE,
            caseData.getId());

        verify(notificationService).sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            CTSC_INBOX,
            EXPECTED_TEMPLATE,
            caseData.getId());
    }

    @Test
    void shouldGovNotifyCafcassWelsh() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDetails(List.of(HEARING))
            .lastHearingOrderDraftsHearingId(HEARING_ID)
            .build();

        List<HearingOrder> orders = List.of(hearingOrder());
        CafcassLookupConfiguration.Cafcass cafcass =
            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS);

        given(cafcassLookupConfiguration.getCafcassWelsh(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.of(cafcass));
        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, HEARING.getValue(), orders, DIGITAL_SERVICE)).willReturn(EXPECTED_TEMPLATE);

        underTest.sendNotificationToCafcass(new DraftOrdersApproved(caseData, orders));

        verify(notificationService).sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            CAFCASS_EMAIL_ADDRESS,
            EXPECTED_TEMPLATE,
            caseData.getId());
    }

    @Test
    void shouldNotGovNotifyCafcassWhenCafcassIsEngland() {
        CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .hearingDetails(List.of(HEARING))
                .lastHearingOrderDraftsHearingId(HEARING_ID)
                .build();

        List<HearingOrder> orders = List.of(hearingOrder());

        given(cafcassLookupConfiguration.getCafcassWelsh(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.empty());

        underTest.sendNotificationToCafcass(new DraftOrdersApproved(caseData, orders));

        verify(notificationService, never()).sendEmail(
                JUDGE_APPROVES_DRAFT_ORDERS,
                CAFCASS_EMAIL_ADDRESS,
                EXPECTED_TEMPLATE,
                caseData.getId());
    }

    @Test
    void shouldSendGridNotifyToCafcassEngland() {
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
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                        .startDate(hearingDateTime.minusDays(10))
                        .build())
                .build();

        CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .lastHearingOrderDraftsHearingId(selectedHearingId)
                .hearingDetails(List.of(
                        hearingBookingElementOne,
                        hearingBookingElementTwo
                ))
                .build();


        LocalDate now = LocalDate.now();
        List<HearingOrder> orders = List.of(
                HearingOrder.builder()
                    .order(TestDataHelper.testDocumentReference())
                    .title("Test 1")
                    .dateIssued(now)
                    .build(),
                HearingOrder.builder()
                    .order(TestDataHelper.testDocumentReference())
                    .title("Test 2")
                    .dateIssued(now)
                    .build());

        CafcassLookupConfiguration.Cafcass cafcass =
                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS);

        given(cafcassLookupConfiguration.getCafcassEngland(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.of(cafcass));


        underTest.sendNotificationToCafcassViaSendGrid(new DraftOrdersApproved(caseData, orders));

        verify(cafcassNotificationService, times(2)).sendEmail(
                eq(caseData),
                documentRefArgumentCaptor.capture(),
                eq(CafcassRequestEmailContentProvider.ORDER),
                orderCafcassDataArgumentCaptor.capture()
        );

        Set<DocumentReference> documentReferences = documentRefArgumentCaptor.getAllValues().stream()
            .flatMap(Set::stream)
            .collect(toSet());

        assertThat(documentReferences)
            .containsAll(
                Set.of(
                    orders.get(0).getOrder(),
                    orders.get(1).getOrder()
                ));

        List<OrderCafcassData> allCafCassOrders = orderCafcassDataArgumentCaptor.getAllValues();

        assertThat(allCafCassOrders)
            .extracting("documentName", "orderApprovalDate", "hearingDate")
            .contains(
                tuple(orders.get(0).getTitle(), now, hearingDateTime),
                tuple(orders.get(1).getTitle(), now, hearingDateTime)
            );
    }

    @Test
    void shouldNotSendGridNotifyToCafcassWhenCafcassIsNotEngland() {
        CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .hearingDetails(List.of(HEARING))
                .lastHearingOrderDraftsHearingId(HEARING_ID)
                .build();

        List<HearingOrder> orders = List.of(hearingOrder());

        given(cafcassLookupConfiguration.getCafcassEngland(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.empty());

        underTest.sendNotificationToCafcassViaSendGrid(new DraftOrdersApproved(caseData, orders));

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldNotifyDigitalRepresentativesExcludingUnselectedOthersWhenServingOthersIsEnabled() {
        List<Representative> digitalReps = unwrapElements(createRepresentatives(DIGITAL_SERVICE));
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDetails(List.of(HEARING))
            .representatives(wrapElements(digitalReps))
            .lastHearingOrderDraftsHearingId(HEARING_ID)
            .build();

        given(representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE))
            .willReturn(newHashSet("digital-rep1@test.com", "digital-rep2@test.com"));

        given(otherRecipientsInbox.getNonSelectedRecipients(eq(DIGITAL_SERVICE), eq(caseData), any(), any()))
            .willReturn(Set.of("digital-rep1@test.com"));

        List<HearingOrder> orders = List.of(hearingOrder());
        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, HEARING.getValue(), orders, DIGITAL_SERVICE)).willReturn(EXPECTED_TEMPLATE);

        underTest.sendNotificationToDigitalRepresentatives(new DraftOrdersApproved(caseData, orders));

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            EXPECTED_TEMPLATE,
            Set.of("digital-rep2@test.com"),
            JUDGE_APPROVES_DRAFT_ORDERS
        );
    }

    @Test
    void shouldNotifyEmailRepresentativesExcludingUnselectedOthers() {
        List<Representative> emailReps = unwrapElements(createRepresentatives(EMAIL));
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDetails(List.of(HEARING))
            .representatives(wrapElements(emailReps))
            .lastHearingOrderDraftsHearingId(HEARING_ID)
            .build();

        given(representativesInbox.getEmailsByPreference(caseData, EMAIL))
            .willReturn(newHashSet("rep1@test.com", "rep2@test.com"));

        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(caseData), any(), any()))
            .willReturn(Set.of("rep2@test.com"));

        List<HearingOrder> orders = List.of(hearingOrder());
        given(reviewDraftOrdersEmailContentProvider.buildOrdersApprovedContent(
            caseData, HEARING.getValue(), orders, EMAIL)).willReturn(EXPECTED_TEMPLATE);

        underTest.sendNotificationToEmailRepresentatives(new DraftOrdersApproved(caseData, orders));
        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            EXPECTED_TEMPLATE,
            Set.of("rep1@test.com"),
            JUDGE_APPROVES_DRAFT_ORDERS
        );
    }

    @Test
    void shouldNotNotifyDigitalRepresentativesWhenNotPresent() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .representatives(emptyList())
            .build();

        final List<HearingOrder> orders = List.of(hearingOrder());
        given(representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE)).willReturn(Set.of());

        underTest.sendNotificationToDigitalRepresentatives(new DraftOrdersApproved(caseData, orders));

        verifyNoInteractions(representativeNotificationService);
    }

    @Test
    void shouldNotNotifyEmailRepresentativesWhenNotPresent() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .representatives(emptyList())
            .build();

        final List<HearingOrder> orders = List.of(hearingOrder());
        given(representativesInbox.getEmailsByPreference(caseData, EMAIL)).willReturn(Set.of());

        underTest.sendNotificationToEmailRepresentatives(new DraftOrdersApproved(caseData, orders));

        verifyNoInteractions(representativeNotificationService);
    }

    @Test
    void shouldPostOrderDocumentToRecipients() {
        final Other firstOther = Other.builder().name("other1")
            .address(Address.builder().postcode("SE1").build()).build();

        final HearingOrder hearingOrder1 = hearingOrder(wrapElements(firstOther));
        final HearingOrder hearingOrder2 = hearingOrder();

        final Representative representative = Representative.builder()
            .fullName("Postal Rep")
            .servingPreferences(POST)
            .address(testAddress())
            .build();

        final RespondentParty respondent = RespondentParty.builder()
            .firstName("Postal")
            .lastName("Person")
            .address(testAddress())
            .build();

        final List<HearingOrder> orders = List.of(hearingOrder1, hearingOrder2);

        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .representatives(wrapElements(representative))
            .respondents1(wrapElements(Respondent.builder().party(respondent).build()))
            .others(Others.builder().firstOther(firstOther).build())
            .build();

        Party otherParty = firstOther.toParty();
        given(sendDocumentService.getStandardRecipients(caseData))
            .willReturn(newArrayList(representative, respondent, otherParty));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(caseData), any(), any()))
            .willReturn(Set.of());
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(any())).willReturn(Set.of());

        underTest.sendDocumentToPostRecipients(new DraftOrdersApproved(caseData, orders));

        verify(sendDocumentService).getStandardRecipients(caseData);
        verify(sendDocumentService).sendDocuments(caseData,
            List.of(hearingOrder1.getOrder(), hearingOrder2.getOrder()),
            List.of(representative, respondent, otherParty));
    }

    @Test
    void shouldPostOrderDocumentToRecipientsWhenServingOthersIsEnabledFilterIfTranslationNeeded() {
        final Other firstOther = Other.builder().name("other1")
            .address(Address.builder().postcode("SE1").build()).build();

        final HearingOrder hearingOrder1 = hearingOrder(wrapElements(firstOther));
        final HearingOrder hearingOrder2 = hearingOrder().toBuilder()
            .translationRequirements(WELSH_TO_ENGLISH)
            .build();

        final Representative representative = Representative.builder()
            .fullName("Postal Rep")
            .servingPreferences(POST)
            .address(testAddress())
            .build();

        final RespondentParty respondent = RespondentParty.builder()
            .firstName("Postal")
            .lastName("Person")
            .address(testAddress())
            .build();

        final List<HearingOrder> orders = List.of(hearingOrder1, hearingOrder2);

        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .representatives(wrapElements(representative))
            .respondents1(wrapElements(Respondent.builder().party(respondent).build()))
            .others(Others.builder().firstOther(firstOther).build())
            .build();

        Party otherParty = firstOther.toParty();
        given(sendDocumentService.getStandardRecipients(caseData))
            .willReturn(newArrayList(representative, respondent, otherParty));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(caseData), any(), any()))
            .willReturn(Set.of());
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(any())).willReturn(Set.of());

        underTest.sendDocumentToPostRecipients(new DraftOrdersApproved(caseData, orders));

        verify(sendDocumentService).getStandardRecipients(caseData);
        verify(sendDocumentService).sendDocuments(caseData,
            List.of(hearingOrder1.getOrder()),
            List.of(representative, respondent, otherParty));
    }

    @Test
    void shouldNotifyTranslationTeamIfEmpty() {
        underTest.notifyTranslationTeam(
            new DraftOrdersApproved(CASE_DATA, List.of())
        );

        verifyNoInteractions(translationRequestService);
    }

    @Test
    void shouldNotifyTranslationTeamIfTranslationRequired() {
        underTest.notifyTranslationTeam(
            new DraftOrdersApproved(CASE_DATA, List.of(HearingOrder.builder()
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .title("Title")
                .dateIssued(LocalDate.of(2020, 1, 2))
                .order(ORDER)
                .build()))
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            ORDER, "Title - 2 January 2020");
        verifyNoMoreInteractions(translationRequestService);

    }

    @Test
    void shouldNotifyTranslationTeamIfNoTranslationRequired() {
        underTest.notifyTranslationTeam(
            new DraftOrdersApproved(CASE_DATA, List.of(HearingOrder.builder()
                .title("Title")
                .dateIssued(LocalDate.of(2020, 1, 2))
                .order(ORDER)
                .build()))
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(NO),
            ORDER, "Title - 2 January 2020");
        verifyNoMoreInteractions(translationRequestService);

    }

    @Test
    void shouldNotifyTranslationTeamIfTranslationRequiredMultipleOrders() {
        underTest.notifyTranslationTeam(
            new DraftOrdersApproved(CASE_DATA, List.of(HearingOrder.builder()
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .title("Title")
                    .dateIssued(LocalDate.of(2020, 1, 2))
                    .order(ORDER)
                    .build(),
                HearingOrder.builder()
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .title("Title 2")
                    .dateIssued(LocalDate.of(2020, 2, 3))
                    .order(ORDER_2)
                    .build()))
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            ORDER, "Title - 2 January 2020");
        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            ORDER_2, "Title 2 - 3 February 2020");
        verifyNoMoreInteractions(translationRequestService);
    }

    private HearingOrder hearingOrder() {
        return HearingOrder.builder()
            .order(TestDataHelper.testDocumentReference())
            .title("Test")
            .build();
    }

    private HearingOrder hearingOrder(List<Element<Other>> selectedOthers) {
        return HearingOrder.builder()
            .order(TestDataHelper.testDocumentReference())
            .others(selectedOthers)
            .build();
    }
}
