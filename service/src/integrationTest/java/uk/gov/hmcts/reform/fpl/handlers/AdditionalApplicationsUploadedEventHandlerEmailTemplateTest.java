package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    AdditionalApplicationsUploadedEventHandler.class, EmailNotificationHelper.class,
    AdditionalApplicationsUploadedEmailContentProvider.class, CaseUrlService.class,
    RepresentativeNotificationService.class
})
@MockBeans({
    @MockBean(FeatureToggleService.class), @MockBean(IdamClient.class), @MockBean(RequestData.class),
    @MockBean(SendDocumentService.class), @MockBean(OthersService.class), @MockBean(OtherRecipientsInbox.class),
    @MockBean(Time.class)
})
class AdditionalApplicationsUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final String CHILD_LAST_NAME = "Jones";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2099, 2, 20, 20, 20, 0);

    public static final CaseData CASE_DATA = CaseData.builder()
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

    @Autowired
    private AdditionalApplicationsUploadedEventHandler underTest;

    @Autowired
    private RequestData requestData;

    @Autowired
    private FeatureToggleService toggleService;

    @Autowired
    private Time time;

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(HEARING_DATE.minusDays(1));
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void notifyAdmin(boolean toggle, String name) {
        given(toggleService.isEldestChildLastNameEnabled()).willReturn(toggle);
        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("New application uploaded, " + name)
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
                    "send a message to the judge or legal adviser",
                    "send a copy to relevant parties")
                .line()
                .end("To review the application, sign in to " + caseDetailsUrl(CASE_ID, OTHER_APPLICATIONS))
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void notifyParties(boolean toggle, String name) {
        given(toggleService.isEldestChildLastNameEnabled()).willReturn(toggle);
        given(toggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        underTest.notifyLocalAuthority(new AdditionalApplicationsUploadedEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("New application uploaded, " + name)
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
                .end("To review the application, sign in to " + caseDetailsUrl(CASE_ID, OTHER_APPLICATIONS))
            );
    }

    private static Stream<Arguments> subjectLineSource() {
        return Stream.of(
            Arguments.of(true, CHILD_LAST_NAME),
            Arguments.of(false, RESPONDENT_LAST_NAME)
        );
    }
}
