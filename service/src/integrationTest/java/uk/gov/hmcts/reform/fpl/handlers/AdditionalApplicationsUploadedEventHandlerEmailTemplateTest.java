package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType.WITHIN_5_DAYS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    AdditionalApplicationsUploadedEventHandler.class, EmailNotificationHelper.class,
    AdditionalApplicationsUploadedEmailContentProvider.class, CaseUrlService.class,
    RepresentativeNotificationService.class
})
@MockBeans({
    @MockBean(RequestData.class), @MockBean(SendDocumentService.class), @MockBean(OthersService.class),
    @MockBean(OtherRecipientsInbox.class), @MockBean(Time.class), @MockBean(CafcassNotificationService.class),
    @MockBean(UserService.class), @MockBean(ApplicantLocalAuthorityService.class)
})
class AdditionalApplicationsUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final String CHILD_LAST_NAME = "Jones";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2099, 2, 20, 20, 20, 0);

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .caseLocalAuthority(LOCAL_AUTHORITY_NAME)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .hearingDetails(wrapElements(HearingBooking.builder()
            .startDate(HEARING_DATE)
            .type(HearingType.CASE_MANAGEMENT)
            .build()))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder().type(WITH_NOTICE).supplementsBundle(List.of()).build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationType(C1_WITH_SUPPLEMENT)
                .supplementsBundle(List.of())
                .build())
            .build()))
        .build();

    private static final OrderApplicant APPLICANT = OrderApplicant.builder()
        .type(LOCAL_AUTHORITY).name(LOCAL_AUTHORITY_NAME).build();

    @Autowired
    private AdditionalApplicationsUploadedEventHandler underTest;

    @Autowired
    private RequestData requestData;

    @Autowired
    private Time time;

    @MockBean
    private CalendarService calendarService;

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(HEARING_DATE.minusDays(1));
    }

    @Test
    void notifyAdmin() {
        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(CASE_DATA, CASE_DATA, APPLICANT));

        assertThat(response())
            .hasSubject("New application uploaded, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("New applications have been made for the case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", hearing 20 Feb 2099")
                .line()
                .h1("Applications")
                .line()
                .line()
                .list("C2 (With notice)")
                .list("C1 - With supplement")
                .line()
                .h1("Next steps")
                .line("You need to:")
                .list("check the applications",
                    "check payment has been taken",
                    "send a message to the judge or legal adviser")
                .line()
                .line()
                .line()
                .end("To review the application, sign in to " + caseDetailsUrl(CASE_ID, OTHER_APPLICATIONS))
            );
    }

    @Test
    void notifyAdminUrgency() {
        given(calendarService.getWorkingDayFrom(LocalDate.now(), WITHIN_5_DAYS.getCount()))
                .willReturn(LocalDate.now().plusDays(WITHIN_5_DAYS.getCount()));
        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));
        CaseData caseData = CASE_DATA.toBuilder()
                .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                            .type(WITH_NOTICE)
                            .supplementsBundle(List.of())
                            .build())
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                            .applicationType(C1_WITH_SUPPLEMENT)
                            .urgencyTimeFrameType(WITHIN_5_DAYS)
                            .supplementsBundle(List.of())
                            .build())
                    .build()))
                .build();

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, CASE_DATA, APPLICANT));

        assertThat(response())
            .hasSubject("New application uploaded, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("New applications have been made for the case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", hearing 20 Feb 2099")
                .line()
                .h1("Applications")
                .line()
                .line()
                .list("C2 (With notice)")
                .list("C1 - With supplement")
                .line()
                .h1("Next steps")
                .line("You need to:")
                .list("check the applications",
                        "check payment has been taken",
                        "send a message to the judge or legal adviser")
                .line()
                .line("This application has been requested to be considered by "
                   + formatLocalDateToString(LocalDate.now().plusDays(WITHIN_5_DAYS.getCount()),DATE))
                .line()
                .end("To review the application, sign in to " + caseDetailsUrl(CASE_ID, OTHER_APPLICATIONS))
            );
    }

    @Test
    void notifyParties() {
        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(CASE_DATA, CASE_DATA, APPLICANT));

        assertThat(response())
            .hasSubject("New application uploaded, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("New applications have been made for the case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", hearing 20 Feb 2099")
                .line()
                .h1("Applications")
                .line()
                .line()
                .list("C2 (With notice)")
                .list("C1 - With supplement")
                .line()
                .line("To review the application, sign in to " + caseDetailsUrl(CASE_ID, OTHER_APPLICATIONS))
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .line("For legal representation guidance navigate to this link:")
                .end("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-respond-to-a-family-public-law-order-application")
            );
    }
}
