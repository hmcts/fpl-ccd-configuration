package uk.gov.hmcts.reform.fpl.handlers;

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
import uk.gov.hmcts.reform.fpl.events.NewHearingsAddedEvent;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    NewHearingsAddedEventHandler.class,
    JacksonAutoConfiguration.class,
    LookupTestConfig.class,
    FixedTimeConfiguration.class})
class NewHearingsAddedEventHandlerTest {
    private static final String CASE_REFERENCE = "12345";

    @Autowired
    private NewHearingsAddedEventHandler newHearingsAddedEventHandler;

    @Autowired
    private Time time;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;

    @MockBean
    private HearingBookingService hearingBookingService;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);
    }

    @Test
    void shouldSendNotificationToLAWhenNewHearingIsAdded() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(UUID.randomUUID(), createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))));

        given(hearingBookingService.getSelectedHearings(any(), any())).willReturn(hearingBookings);
        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);
        given(newNoticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            any(CaseDetails.class), any(HearingBooking.class), any()))
            .willReturn(NewNoticeOfHearingTemplate.builder().build());

        newHearingsAddedEventHandler.sendEmail(new NewHearingsAddedEvent(callbackRequest));

        verify(notificationService, times(2)).sendEmail(
            anyString(),
            anyString(),
            any(NewNoticeOfHearingTemplate.class),
            anyString());

        verify(representativeNotificationService, times(2))
            .sendToRepresentativesByServedPreference(
                any(RepresentativeServingPreferences.class),
                anyString(),
                anyMap(),
                any(EventData.class)
            );
    }
}
