package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.AddressNotKnowReason;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.PlacementApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementContentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.PLACEMENT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.PLACEMENT_NOTICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class PlacementEventsHandlerNotificationTest {

    private static final Long CASE_ID = 100L;
    private static final String LOCAL_AUTHORITY_NAME = "Local Authority One";

    @Mock
    private CourtService courtService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PlacementContentProvider contentProvider;

    @Mock
    private SendDocumentService sendDocumentService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    CafcassNotificationService cafcassNotificationService;

    @Mock
    RepresentativesInbox representativesInbox;

    @InjectMocks
    private PlacementEventsHandler underTest;

    private final DocumentReference placementApplication = testDocumentReference();
    private final DocumentReference placementNotice = testDocumentReference();

    private final Placement placement = Placement.builder()
        .childId(randomUUID())
        .application(placementApplication)
        .placementNotice(placementNotice)
        .build();

    private final PlacementEventData placementEventData = PlacementEventData.builder()
        .placementPaymentRequired(YES)
        .placement(placement)
        .build();

    private final PlacementNotifyData notifyData = PlacementNotifyData.builder()
        .childName("Alex Brown")
        .localAuthority(LOCAL_AUTHORITY_NAME)
        .caseUrl("http://test.com")
        .build();

    private final PlacementApplicationCafcassData cafcassData = PlacementApplicationCafcassData.builder()
        .placementChildName("Alex Brown")
        .build();

    @Nested
    class CourtNotification {

        private final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .placementEventData(placementEventData)
            .build();

        private final String courtEmail = "court@test.com";

        @BeforeEach
        void init() {
            when(contentProvider.getApplicationChangedCourtData(caseData, placement)).thenReturn(notifyData);
            when(courtService.getCourtEmail(caseData)).thenReturn(courtEmail);
        }

        @Test
        void shouldSendNotificationToCourtWhenNewPlacementApplicationSubmitted() {

            final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

            underTest.notifyCourt(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE, courtEmail, notifyData, CASE_ID);
        }

        @Test
        void shouldSendNotificationToCourtWhenPlacementApplicationChanged() {

            final PlacementApplicationChanged event = new PlacementApplicationChanged(caseData, placement);

            underTest.notifyCourt(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE, courtEmail, notifyData, CASE_ID);
        }
    }

    @Nested
    class LocalAuthorityNotification {

        private final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .placementEventData(placementEventData)
            .build();

        @Test
        void shouldSendNotificationToLocalAuthority() {

            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placement);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .build();

            final Set<String> recipients = Set.of("email1@test.com", "email2@test.com");

            when(contentProvider.getNoticeChangedData(caseData, placement)).thenReturn(notifyData);
            when(localAuthorityRecipients.getRecipients(recipientsRequest)).thenReturn(recipients);

            underTest.notifyLocalAuthorityOfNewNotice(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, recipients, notifyData, CASE_ID);
        }

    }

    @Nested
    class CafcassNotification {

        private final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority("LA1")
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .placementEventData(placementEventData)
            .build();

        @Test
        void shouldNotifyCafcassWalesAboutNoticeViaNotify() {

            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placement);
            final Cafcass cafcass = new Cafcass("Cafcass", "cafcass@test.com");

            when(contentProvider.getNoticeChangedCafcassData(caseData, placement)).thenReturn(notifyData);
            when(cafcassLookupConfiguration.getCafcassWelsh("LA1")).thenReturn(Optional.of(cafcass));

            underTest.notifyCafcassOfNewNotice(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE, cafcass.getEmail(), notifyData, CASE_ID);
        }

        @Test
        void shouldNotifyCafcassEnglandAboutNoticeViaSendGrid() {
            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placement);
            final Cafcass cafcass = new Cafcass("Cafcass", "cafcass@test.com");

            when(contentProvider.buildNewPlacementApplicationNotificationCafcassData(caseData, placement))
                .thenReturn(cafcassData);
            when(cafcassLookupConfiguration.getCafcassEngland("LA1")).thenReturn(Optional.of(cafcass));

            underTest.notifyCafcassOfNewNoticeSendGrid(event);

            verify(cafcassNotificationService)
                .sendEmail(caseData, Set.of(placementNotice), PLACEMENT_NOTICE, cafcassData);
        }

        @Test
        void shouldNotifyCafcassWalesAboutApplicationViaNotify() {

            final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);
            final Cafcass cafcass = new Cafcass("Cafcass", "cafcass@test.com");

            when(contentProvider.getApplicationChangedCourtData(caseData, placement)).thenReturn(notifyData);
            when(cafcassLookupConfiguration.getCafcassWelsh("LA1")).thenReturn(Optional.of(cafcass));

            underTest.notifyCafcassOfNewApplicationGovNotify(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE, cafcass.getEmail(), notifyData, CASE_ID);
        }


        @Test
        void shouldNotifyCafcassEnglandAboutApplicationViaSendGrid() {
            final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);
            final Cafcass cafcass = new Cafcass("Cafcass", "cafcass@test.com");

            when(contentProvider.buildNewPlacementApplicationNotificationCafcassData(caseData, placement))
                .thenReturn(cafcassData);
            when(cafcassLookupConfiguration.getCafcassEngland("LA1")).thenReturn(Optional.of(cafcass));

            underTest.notifyCafcassOfNewApplicationSendGrid(event);

            verify(cafcassNotificationService)
                .sendEmail(caseData, Set.of(placementApplication), PLACEMENT_APPLICATION, cafcassData);
        }

    }

    @Nested
    class ParentsNotification {

        private final RespondentSolicitor parentSolicitor = RespondentSolicitor.builder()
            .email("solicitor@test.com")
            .build();

        private final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        @Test
        void shouldSendNotificationToRespondentSolicitor() {
            Element<Respondent> respondent = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Alex")
                    .lastName("Smith")
                    .build())
                .solicitor(parentSolicitor)
                .build());

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .respondents1(List.of(respondent))
                .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
                .placementEventData(placementEventData)
                .build();

            Placement placementNotifyParent = placement.toBuilder()
                .placementRespondentsToNotify(List.of(respondent))
                .build();

            when(contentProvider.getNoticeChangedData(caseData, placementNotifyParent)).thenReturn(notifyData);

            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placementNotifyParent);

            underTest.notifyRespondentsOfNewNotice(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, parentSolicitor.getEmail(), notifyData, CASE_ID);
        }

        @Test
        void shouldSendLetterWhenParentNotRepresented() {
            Element<Respondent> respondent = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Jodie")
                    .lastName("Smith")
                    .build())
                .build());

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .respondents1(List.of(respondent))
                .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
                .placementEventData(placementEventData)
                .build();

            var supportingDocumentReference = testDocumentReference();

            final PlacementSupportingDocument birthCertificate = PlacementSupportingDocument.builder()
                    .type(BIRTH_ADOPTION_CERTIFICATE)
                    .document(supportingDocumentReference)
                    .build();

            List<Element<PlacementSupportingDocument>> supportingDocuments = new ArrayList<>();

            supportingDocuments.add(Element.newElement(birthCertificate));

            Placement placementNotifyParent = placement.toBuilder()
                .placementRespondentsToNotify(List.of(respondent))
                .supportingDocuments(supportingDocuments)
                .application(placementApplication)
                .build();

            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placementNotifyParent);

            underTest.notifyRespondentsOfNewNotice(event);

            verifyNoInteractions(notificationService, contentProvider);
            verify(sendDocumentService)
                .sendDocuments(caseData, List.of(supportingDocumentReference,
                        placementNotifyParent.getPlacementNotice(),
                        placementApplication), List.of(respondent.getValue().getParty()));
        }

        @Test
        void shouldNotSendLetterWhenParentMarkedNfaOrDeceased() {
            Element<Respondent> respondent = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Jodie")
                    .lastName("Smith")
                    .addressKnow(YesNo.NO.getValue())
                    .addressNotKnowReason(AddressNotKnowReason.DECEASED.getType())
                    .build())
                .build());
            Element<Respondent> respondent2 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Marry")
                    .lastName("Smith")
                    .addressKnow(YesNo.NO.getValue())
                    .addressNotKnowReason(AddressNotKnowReason.NO_FIXED_ABODE.getType())
                    .build())
                .build());

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .respondents1(List.of(respondent, respondent2))
                .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
                .placementEventData(placementEventData)
                .build();

            Placement placementNotifyParent = placement.toBuilder()
                .placementRespondentsToNotify(List.of(respondent))
                .build();

            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placementNotifyParent);

            underTest.notifyRespondentsOfNewNotice(event);

            verifyNoInteractions(sendDocumentService);
        }
    }


    @Nested
    class ChildSolicitorNotification {

        private final String childSolicitorEmail = "jack@test.com";

        private Child child = Child.builder()
            .party(ChildParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .solicitor(RespondentSolicitor.builder()
                .firstName("Jack")
                .lastName("Jackson")
                .email(childSolicitorEmail)
                .build())
            .build();

        @Test
        void shouldSendNotificationToChildRepresentative() {
            Element<Respondent> respondent = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Alex")
                    .lastName("Smith")
                    .build())
                .build());

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .respondents1(List.of(respondent))
                .children1(wrapElements(child))
                .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
                .placementEventData(placementEventData)
                .build();

            Placement placementNotifyParent = placement.toBuilder()
                .placementRespondentsToNotify(List.of(respondent))
                .build();

            when(contentProvider.getNoticeChangedData(caseData, placementNotifyParent)).thenReturn(notifyData);
            when(representativesInbox.getChildrenSolicitorEmails(eq(caseData), any()))
                .thenReturn(newHashSet(childSolicitorEmail));

            final PlacementNoticeAdded event = new PlacementNoticeAdded(caseData, placementNotifyParent);

            underTest.notifyChildSolicitorsOfNewNotice(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, Set.of(childSolicitorEmail), notifyData, CASE_ID);

        }
    }
}
