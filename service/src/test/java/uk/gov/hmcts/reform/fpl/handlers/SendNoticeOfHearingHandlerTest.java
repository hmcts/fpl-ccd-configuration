package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SendNoticeOfHearingHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    FixedTimeConfiguration.class})
class SendNoticeOfHearingHandlerTest {

    @Autowired
    private SendNoticeOfHearingHandler sendNoticeOfHearingHandler;

    @Autowired
    private Time time;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

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

        sendNoticeOfHearingHandler.sendEmailToLA(new SendNoticeOfHearing(caseData, hearing));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            noticeOfHearingTemplate,
            caseData.getId().toString());
    }

    @Test
    void shouldSendNotificationToCafcassWhenNewHearingIsAdded() {
        final CaseData caseData = caseData();

        sendNoticeOfHearingHandler.sendEmailToCafcass(new SendNoticeOfHearing(caseData, hearing));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING,
            CAFCASS_EMAIL_ADDRESS,
            noticeOfHearingTemplate,
            caseData.getId().toString());
    }

    @Test
    void shouldSendNotificationToRepresentativesWhenNewHearingIsAdded() {
        final CaseData caseData = caseData();

        sendNoticeOfHearingHandler.sendEmailToRepresentatives(new SendNoticeOfHearing(caseData, hearing));

        verify(representativeNotificationService)
            .sendToRepresentativesByServedPreference(
                RepresentativeServingPreferences.EMAIL,
                NOTICE_OF_NEW_HEARING,
                noticeOfHearingTemplate.toMap(mapper),
                caseData
            );

        verify(representativeNotificationService)
            .sendToRepresentativesByServedPreference(
                RepresentativeServingPreferences.DIGITAL_SERVICE,
                NOTICE_OF_NEW_HEARING,
                noticeOfHearingTemplate.toMap(mapper),
                caseData
            );
    }

    @Test
    void shouldSendDocumentToRepresentativesServedByPostWhenNewHearingIsAdded() {
        final DocumentReference noticeOfHearing = DocumentReference.builder()
            .filename("fileName")
            .url("www.url.com")
            .binaryUrl("binary_url")
            .build();

        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .build();

        sendNoticeOfHearingHandler.sendDocumentToRepresentatives(new SendNoticeOfHearing(caseData, hearing));

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, caseData.getId(),
            "internal-change-SEND_DOCUMENT", Map.of("documentToBeSent", noticeOfHearing));
    }
}
