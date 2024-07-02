package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.order.NonMolestationOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class NonMolestationOrderEventHandlerTest {
    @Mock
    private TranslationRequestService translationRequestService;
    @Mock
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NonMolestationOrderEventHandler underTest;

    private final DocumentReference orderDocument = mock(DocumentReference.class);
    private final OrderIssuedNotifyData notifyData = mock(OrderIssuedNotifyData.class);

    private void init() {

    }

    @Test
    void shouldSendTranslationRequest() {
        CaseData caseData = CaseData.builder().build();
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder().build();
        NonMolestationOrderEvent event = new NonMolestationOrderEvent(caseData, manageOrdersEventData,
            "Non molestation order",
            orderDocument,
            LanguageTranslationRequirement.ENGLISH_TO_WELSH
        );

        underTest.notifyTranslationTeam(event);

        verify(translationRequestService).sendRequest(caseData,
            Optional.of(LanguageTranslationRequirement.ENGLISH_TO_WELSH),
            orderDocument, "Non molestation order");
    }

    @Test
    void shouldNotifyDesignatedLaIfSelectedAsApplicant() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getId()).thenReturn(1L);

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersNonMolestationOrderApplicant(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code("applicant")
                    .build()).build())
            .build();
        NonMolestationOrderEvent event = new NonMolestationOrderEvent(caseData, manageOrdersEventData,
            "Non molestation order",
            orderDocument,
            LanguageTranslationRequirement.ENGLISH_TO_WELSH
        );

        when(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build())).thenReturn(Set.of("la@test.com"));

        when(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
            orderDocument, GENERATED_ORDER)).thenReturn(notifyData);

        underTest.notifyParties(event);

        verify(notificationService).sendEmail(NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE, Set.of("la@test.com"),
            notifyData,caseData.getId());
    }

    @Test
    void shouldNotifySecondaryLaIfSelectedAsApplicant() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getId()).thenReturn(1L);

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersNonMolestationOrderApplicant(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code("secondaryLocalAuthority")
                    .build()).build())
            .build();
        NonMolestationOrderEvent event = new NonMolestationOrderEvent(caseData, manageOrdersEventData,
            "Non molestation order",
            orderDocument,
            LanguageTranslationRequirement.ENGLISH_TO_WELSH
        );

        when(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
            .caseData(caseData)
            .designatedLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build())).thenReturn(Set.of("la2@test.com"));

        when(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
            orderDocument, GENERATED_ORDER)).thenReturn(notifyData);

        underTest.notifyParties(event);

        verify(notificationService).sendEmail(NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE, Set.of("la2@test.com"),
            notifyData,caseData.getId());
    }

    @Test
    void shouldNotifyRespondentSolicitorIfSelectedAsApplicant() {
        UUID selectedUuid = UUID.randomUUID();
        Respondent selectedRespondent = mock(Respondent.class);
        List<Element<Respondent>> respondents = List.of(element(selectedUuid, selectedRespondent));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseData.getAllRespondents()).thenReturn(respondents);

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersNonMolestationOrderApplicant(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(selectedUuid.toString())
                    .build()).build())
            .build();

        when(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
            orderDocument, GENERATED_ORDER)).thenReturn(notifyData);
        when(representativesInbox.getRespondentSolicitorEmailsFromList(respondents,
            RepresentativeServingPreferences.DIGITAL_SERVICE))
            .thenReturn(new HashSet<>(Set.of("respondent_digital@test.com")));
        when(representativesInbox.getRespondentSolicitorEmailsFromList(respondents,
            RepresentativeServingPreferences.EMAIL))
            .thenReturn(new HashSet<>(Set.of("respondent_email@test.com")));

        NonMolestationOrderEvent event = new NonMolestationOrderEvent(caseData, manageOrdersEventData,
            "Non molestation order",
            orderDocument,
            LanguageTranslationRequirement.ENGLISH_TO_WELSH
        );

        underTest.notifyParties(event);

        verify(notificationService).sendEmail(NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE,
            Set.of("respondent_digital@test.com", "respondent_email@test.com"),
            notifyData,caseData.getId());
    }

    @Test
    void shouldNotifyChildSolicitorIfSelectedAsApplicant() {
        UUID selectedUuid = UUID.randomUUID();
        Child selectedChild = mock(Child.class);
        List<Element<Child>> children = List.of(element(selectedUuid, selectedChild));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseData.getAllChildren()).thenReturn(children);

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersNonMolestationOrderApplicant(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(selectedUuid.toString())
                    .build()).build())
            .build();

        when(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
            orderDocument, GENERATED_ORDER)).thenReturn(notifyData);
        when(representativesInbox.getChildrenSolicitorEmailsFromList(children,
            RepresentativeServingPreferences.DIGITAL_SERVICE))
            .thenReturn(new HashSet<>(Set.of("child_digital@test.com")));
        when(representativesInbox.getChildrenSolicitorEmailsFromList(children,
            RepresentativeServingPreferences.EMAIL))
            .thenReturn(new HashSet<>(Set.of("child_email@test.com")));

        NonMolestationOrderEvent event = new NonMolestationOrderEvent(caseData, manageOrdersEventData,
            "Non molestation order",
            orderDocument,
            LanguageTranslationRequirement.ENGLISH_TO_WELSH
        );

        underTest.notifyParties(event);

        verify(notificationService).sendEmail(NON_MOLESTATION_ORDER_NOTIFICATION_TEMPLATE,
            Set.of("child_digital@test.com", "child_email@test.com"),
            notifyData,caseData.getId());
    }
}
