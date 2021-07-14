package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    SendNoticeOfHearingHandler.class, NoticeOfHearingEmailContentProvider.class, CaseUrlService.class,
    RepresentativeNotificationService.class, CaseDataExtractionService.class, EmailNotificationHelper.class
})
@MockBean(SendDocumentService.class)
class SendNoticeOfHearingHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String CHILD_LAST_NAME = "Mortarion";
    private static final String RESPONDENT_LAST_NAME = "Lorgar";
    private static final long CASE_ID = 12345L;
    private static final HearingBooking HEARING = HearingBooking.builder()
        .type(HearingType.CASE_MANAGEMENT)
        .startDate(LocalDateTime.of(2021, 12, 12, 12, 0, 0))
        .endDate(LocalDateTime.of(2021, 12, 21, 0, 0, 0))
        .noticeOfHearing(DocumentReference.builder().binaryUrl("/binary/url").build())
        .preAttendanceDetails("20 minutes before the hearing")
        .build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .familyManCaseNumber("FAM_NUM")
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .build();

    @MockBean
    private HearingVenueLookUpService venueLookUp;
    @Autowired
    private SendNoticeOfHearingHandler underTest;

    @BeforeEach
    void setUp() {
        HearingVenue venue = mock(HearingVenue.class);
        when(venueLookUp.getHearingVenue(HEARING)).thenReturn(venue);
        when(venueLookUp.buildHearingVenue(venue)).thenReturn("some building, somewhere");
    }

    @Test
    void notifyCafcass() {
        underTest.notifyCafcass(new SendNoticeOfHearing(CASE_DATA, HEARING));

        assertThat(response())
            .hasSubject("New case management hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("There's a new case management hearing for:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .h1("Hearing details")
                .line("Date: 12 December 2021")
                .line("Venue: some building, somewhere")
                .line("Pre-hearing time: 20 minutes before the hearing")
                .line("Hearing time: 12 December, 12:00pm - 21 December, 12:00am")
                .line()
                .line("Parties and their legal representatives must attend pre-hearing discussions.")
                .line()
                .h1("Next steps")
                .line("You can check what you need to do before the hearing by:")
                .line()
                .line()
                .list("using this link " + GOV_NOTIFY_DOC_URL)
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Please do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                     + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyLocalAuthority() {
        underTest.notifyLocalAuthority(new SendNoticeOfHearing(CASE_DATA, HEARING));

        assertThat(response())
            .hasSubject("New case management hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("There's a new case management hearing for:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .h1("Hearing details")
                .line("Date: 12 December 2021")
                .line("Venue: some building, somewhere")
                .line("Pre-hearing time: 20 minutes before the hearing")
                .line("Hearing time: 12 December, 12:00pm - 21 December, 12:00am")
                .line()
                .line("Parties and their legal representatives must attend pre-hearing discussions.")
                .line()
                .h1("Next steps")
                .line("You can check what you need to do before the hearing by:")
                .list("signing into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.HEARINGS))
                .line()
                .list("using this link http://fake-url/binary/url")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Please do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                     + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyRepresentatives() {
        underTest.notifyRepresentatives(new SendNoticeOfHearing(CASE_DATA, HEARING));

        assertThat(response())
            .hasSubject("New case management hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("There's a new case management hearing for:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .h1("Hearing details")
                .line("Date: 12 December 2021")
                .line("Venue: some building, somewhere")
                .line("Pre-hearing time: 20 minutes before the hearing")
                .line("Hearing time: 12 December, 12:00pm - 21 December, 12:00am")
                .line()
                .line("Parties and their legal representatives must attend pre-hearing discussions.")
                .line()
                .h1("Next steps")
                .line("You can check what you need to do before the hearing by:")
                .line()
                .line()
                .list("using this link " + GOV_NOTIFY_DOC_URL)
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Please do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                     + "contactfpl@justice.gov.uk")
            );
    }
}
