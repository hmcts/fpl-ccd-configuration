package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.NewHearingsAdded;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NewNoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NewHearingsAddedHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    FixedTimeConfiguration.class})
class NewHearingsAddedHandlerTest {
    private static final String CASE_REFERENCE = "12345";

    @Autowired
    private NewHearingsAddedHandler newHearingsAddedHandler;

    @Autowired
    private Time time;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

    private LocalDateTime futureDate;

    private NoticeOfHearingTemplate noticeOfHearingTemplate = NoticeOfHearingTemplate.builder().build();

    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);

        given(newNoticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            any(CaseDetails.class), any(HearingBooking.class), any()))
            .willReturn(noticeOfHearingTemplate);
    }

    @Test
    void shouldSendNotificationToLAWhenNewHearingIsAdded() {
        final CallbackRequest callbackRequest = callbackRequest();
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(UUID.randomUUID(), createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))));

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        newHearingsAddedHandler.sendEmailToLA(new NewHearingsAdded(callbackRequest, hearingBookings));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            noticeOfHearingTemplate,
            CASE_REFERENCE);
    }

    @Test
    void shouldSendNotificationToCafcassWhenNewHearingIsAdded() {
        final CallbackRequest callbackRequest = callbackRequest();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(UUID.randomUUID(), createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))));

        newHearingsAddedHandler.sendEmailToCafcass(new NewHearingsAdded(callbackRequest, hearingBookings));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING,
            CAFCASS_EMAIL_ADDRESS,
            noticeOfHearingTemplate,
            CASE_REFERENCE);
    }

    @Test
    void shouldSendNotificationToRepresentativesWhenNewHearingIsAdded() {
        final CallbackRequest callbackRequest = callbackRequest();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(UUID.randomUUID(), createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))));
        final EventData eventData = new EventData(new NewHearingsAdded(callbackRequest, hearingBookings));

        newHearingsAddedHandler.sendEmailToRepresentatives(new NewHearingsAdded(callbackRequest, hearingBookings));

        verify(representativeNotificationService)
            .sendToRepresentativesByServedPreference(
                RepresentativeServingPreferences.EMAIL,
                NOTICE_OF_NEW_HEARING,
                noticeOfHearingTemplate.toMap(mapper),
                eventData
            );

        verify(representativeNotificationService)
            .sendToRepresentativesByServedPreference(
                RepresentativeServingPreferences.DIGITAL_SERVICE,
                NOTICE_OF_NEW_HEARING,
                noticeOfHearingTemplate.toMap(mapper),
                eventData
            );
    }
}
