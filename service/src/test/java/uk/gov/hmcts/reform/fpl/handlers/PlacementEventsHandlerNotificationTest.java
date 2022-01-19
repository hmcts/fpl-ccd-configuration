package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementContentProvider;

import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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

    @InjectMocks
    private PlacementEventsHandler underTest;

    private final Placement placement = Placement.builder()
        .childId(randomUUID())
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

        private final PlacementNoticeDocument notice = PlacementNoticeDocument.builder()
            .type(PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY)
            .build();

        private final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .placementEventData(placementEventData)
            .build();

        @Test
        void shouldSendNotificationToLocalAuthority() {

            final PlacementNoticeChanged event = new PlacementNoticeChanged(caseData, placement, notice);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .build();

            final Set<String> recipients = Set.of("email1@test.com", "email2@test.com");

            when(contentProvider.getNoticeChangedData(caseData, placement)).thenReturn(notifyData);
            when(localAuthorityRecipients.getRecipients(recipientsRequest)).thenReturn(recipients);

            underTest.notifyParties(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, recipients, notifyData, CASE_ID);
        }

    }

    @Nested
    class CafcassNotification {

        private final PlacementNoticeDocument notice = PlacementNoticeDocument.builder()
            .type(PlacementNoticeDocument.RecipientType.CAFCASS)
            .build();

        private final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority("LA1")
            .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
            .placementEventData(placementEventData)
            .build();

        @Test
        void shouldSendNotificationToCafcass() {

            final PlacementNoticeChanged event = new PlacementNoticeChanged(caseData, placement, notice);
            final Cafcass cafcass = new Cafcass("Cafcass", "cafcass@test.com");

            when(contentProvider.getNoticeChangedCafcassData(caseData, placement)).thenReturn(notifyData);
            when(cafcassLookupConfiguration.getCafcass("LA1")).thenReturn(cafcass);

            underTest.notifyParties(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE, cafcass.getEmail(), notifyData, CASE_ID);
        }

    }

    @Nested
    class ParentsNotification {

        private final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        private final RespondentSolicitor parentSolicitor = RespondentSolicitor.builder()
            .email("solicitor@test.com")
            .build();

        @Test
        void shouldSendNotificationToFirstParentSolicitor() {

            final Element<Respondent> respondent = element(Respondent.builder()
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

            when(contentProvider.getNoticeChangedData(caseData, placement)).thenReturn(notifyData);

            final PlacementNoticeDocument notice = PlacementNoticeDocument.builder()
                .type(PlacementNoticeDocument.RecipientType.PARENT_FIRST)
                .recipientName("Alex Smith")
                .respondentId(respondent.getId())
                .build();

            final PlacementNoticeChanged event = new PlacementNoticeChanged(caseData, placement, notice);

            underTest.notifyParties(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, parentSolicitor.getEmail(), notifyData, CASE_ID);
        }

        @Test
        void shouldSendNotificationToSecondParentSolicitor() {

            final Element<Respondent> respondent = element(Respondent.builder()
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

            when(contentProvider.getNoticeChangedData(caseData, placement)).thenReturn(notifyData);

            final PlacementNoticeDocument notice = PlacementNoticeDocument.builder()
                .type(PlacementNoticeDocument.RecipientType.PARENT_SECOND)
                .recipientName("Alex Smith")
                .respondentId(respondent.getId())
                .build();

            final PlacementNoticeChanged event = new PlacementNoticeChanged(caseData, placement, notice);

            underTest.notifyParties(event);

            verify(notificationService)
                .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, parentSolicitor.getEmail(), notifyData, CASE_ID);
        }

        @Test
        void shouldSendLetterWhenParentNotRepresented() {

            final Element<Respondent> respondent = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Alex")
                    .lastName("Smith")
                    .build())
                .build());

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .respondents1(List.of(respondent))
                .caseLocalAuthorityName(LOCAL_AUTHORITY_NAME)
                .placementEventData(placementEventData)
                .build();

            final PlacementNoticeDocument notice = PlacementNoticeDocument.builder()
                .type(PlacementNoticeDocument.RecipientType.PARENT_FIRST)
                .recipientName("Alex Smith")
                .respondentId(respondent.getId())
                .build();

            final PlacementNoticeChanged event = new PlacementNoticeChanged(caseData, placement, notice);

            underTest.notifyParties(event);

            verifyNoInteractions(notificationService, contentProvider);
            verify(sendDocumentService).sendDocuments(
                caseData, List.of(placement.getPlacementNotice()), List.of(respondent.getValue().getParty()));
        }
    }
}
