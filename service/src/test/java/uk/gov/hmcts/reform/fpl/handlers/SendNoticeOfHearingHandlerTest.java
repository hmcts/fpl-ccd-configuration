package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SendNoticeOfHearingHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    FixedTimeConfiguration.class})
class SendNoticeOfHearingHandlerTest {

    @Autowired
    private Time time;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

    @MockBean
    private SendDocumentService sendDocumentService;

    @Autowired
    private SendNoticeOfHearingHandler underTest;

    private NoticeOfHearingTemplate noticeOfHearingTemplate = NoticeOfHearingTemplate.builder().build();

    private HearingBooking hearing;

    @BeforeEach
    void setUp() {
        hearing = createHearingBooking(time.now().plusDays(1).plusDays(5), time.now().plusDays(1).plusDays(6));

        given(noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            any(CaseData.class), any(HearingBooking.class), any()))
            .willReturn(noticeOfHearingTemplate);
    }

    @Test
    void shouldSendNotificationToLAWhenNewHearingIsAdded() {
        final CaseData caseData = caseData();

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        underTest.notifyLocalAuthority(new SendNoticeOfHearing(caseData, hearing));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            noticeOfHearingTemplate,
            caseData.getId().toString());
    }

    @Test
    void shouldSendNotificationToCafcassWhenNewHearingIsAdded() {
        final CaseData caseData = caseData();

        underTest.notifyCafcass(new SendNoticeOfHearing(caseData, hearing));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING,
            CAFCASS_EMAIL_ADDRESS,
            noticeOfHearingTemplate,
            caseData.getId());
    }

    @Test
    void shouldSendNotificationToRepresentativesWhenNewHearingIsAdded() {
        final CaseData caseData = caseData();

        underTest.notifyRepresentatives(new SendNoticeOfHearing(caseData, hearing));

        verify(representativeNotificationService)
            .sendToRepresentativesByServedPreference(
                RepresentativeServingPreferences.EMAIL,
                NOTICE_OF_NEW_HEARING,
                noticeOfHearingTemplate,
                caseData
            );

        verify(representativeNotificationService)
            .sendToRepresentativesByServedPreference(
                RepresentativeServingPreferences.DIGITAL_SERVICE,
                NOTICE_OF_NEW_HEARING,
                noticeOfHearingTemplate,
                caseData
            );
    }

    @Test
    void shouldSendNoticeOfHearingToRepresentativesAndNotRepresentedRespondentsByPost() {
        final Representative representative = Representative.builder()
            .fullName("First Representative")
            .servingPreferences(POST)
            .address(testAddress())
            .build();

        final RespondentParty respondent = RespondentParty.builder()
            .firstName("First")
            .lastName("Respondent")
            .address(testAddress())
            .build();

        final CaseData caseData = caseData().toBuilder()
            .representatives(wrapElements(representative))
            .respondents1(wrapElements(Respondent.builder().party(respondent).build()))
            .hearingDetails(wrapElements(hearing))
            .build();

        final SendNoticeOfHearing event = new SendNoticeOfHearing(caseData, hearing);

        List<Recipient> recipients = List.of(representative, respondent);
        given(sendDocumentService.getStandardRecipients(caseData)).willReturn(recipients);

        underTest.sendNoticeOfHearingByPost(event);

        verify(sendDocumentService)
            .sendDocuments(caseData, List.of(hearing.getNoticeOfHearing()), recipients);
        verify(sendDocumentService)
            .getStandardRecipients(caseData);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }
}
