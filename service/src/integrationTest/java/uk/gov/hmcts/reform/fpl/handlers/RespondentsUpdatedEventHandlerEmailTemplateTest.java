package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    RespondentsUpdatedEventHandler.class,
    RespondentService.class,
    RegisteredRepresentativeSolicitorContentProvider.class,
    UnregisteredRepresentativeSolicitorContentProvider.class,
    EmailNotificationHelper.class,
    FixedTimeConfiguration.class
})
@MockBeans({
    @MockBean(CafcassNotificationService.class),
    @MockBean(LocalAuthorityService.class)
})
class RespondentsUpdatedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_FIRST_NAME = "John";
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName(RESPONDENT_LAST_NAME).build())
        .build();
    private static final long CASE_ID = 1234567890123456L;
    public static final String CASE_NAME = "FPL case test";

    @Autowired
    private RespondentsUpdatedEventHandler underTest;

    @ParameterizedTest
    @MethodSource("representativeNameSource")
    void notifyRegisteredSolicitor(String firstName, String lastName, String expectedSalutation) {
        final Respondent respondent1 = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .solicitor(RespondentSolicitor.builder()
                .email("solicitor@test.com")
                .firstName(firstName)
                .lastName(lastName)
                .organisation(Organisation.builder().organisationID("123").organisationName("Organisation1").build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(respondent1))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseName("FPL case test")
            .children1(List.of(element(Child.builder()
                .party(ChildParty.builder()
                    .firstName("Timothy").lastName("Jones")
                    .dateOfBirth(LocalDate.of(2010, 1, 1)).build())
                .build())))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(OPEN)
            .caseName("test1")
            .build();

        underTest.notifyRegisteredRespondentSolicitors(new RespondentsUpdated(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New application, FPL case test, Jones")
            .hasBody(emailContent()
                .start()
                .line(String.format("%s%s has made the following application on the Family"
                    + " Public Law (FPL) digital service:", expectedSalutation, LOCAL_AUTHORITY_NAME))
                .line()
                .callout(String.format(" %s, %s", CASE_NAME, CASE_ID))
                .line()
                .line(String.format("They’ve given your details as the legal representative for %s %s.",
                    RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME))
                .line()
                .line(
                    "You should now ask your organisation's FPL case access administrator to assign the case to you.")
                .line()
                .line("They'll need to sign into https://manage-org.platform.hmcts.net")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For legal representation guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-respond-to-a-family-public-law-order-application")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyUnregisteredSolicitors() {
        Respondent respondent = RESPONDENT.toBuilder()
            .legalRepresentation(YES.getValue())
            .party(RespondentParty.builder().firstName("Emma").lastName("Jones").build())
            .solicitor(RespondentSolicitor.builder()
                .email("RespondentSolicitor@test.com")
                .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered Org Name").build())
                .build()
            ).build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(respondent))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseName("FPL case test")
            .children1(List.of(element(Child.builder()
                .party(ChildParty.builder()
                    .firstName("Timothy").lastName("Jones")
                    .dateOfBirth(LocalDate.of(2010, 1, 1)).build())
                .build())))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(OPEN)
            .build();

        underTest.notifyUnregisteredRespondentSolicitors(new RespondentsUpdated(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New application involving your client, FPL case test, Jones")
            .hasBody(emailContent()
                .start()
                .line(LOCAL_AUTHORITY_NAME + " has made a new application on the Family Public Law digital "
                    + "service.")
                .line()
                .line("They’ve given your details as the legal representative for Emma Jones.")
                .line()
                .line("Legal representatives must be registered to use the service.")
                .line()
                .line("You can register at https://manage-org.platform.hmcts.net/register-org/register")
                .line()
                .line("You’ll need your organisation’s Pay By Account (PBA) details.")
                .line()
                .h1("After you've registered")
                .line()
                .line("Once registered, you'll need to complete an online notice of change.")
                .line()
                .line("Use the 'notice of change' link on the 'case list' page to do this.")
                .line()
                .line("You'll need:")
                .list("reference number 1234-5678-9012-3456",
                    "the name of the applicant that made the application",
                    "your client's first and last names")
                .line()
                .line("You’ll then be able to:")
                .list("access relevant case files",
                    "upload your own statements and reports",
                    "make applications in the case, for example C2")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    private static Stream<Arguments> representativeNameSource() {
        // requires \n\n at the end due to line break discrepancies between the to expected salutations
        String expectedSalutation = String.join(" ", "Dear", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME + "\n\n");
        return Stream.of(
            Arguments.of(RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME, expectedSalutation),
            Arguments.of(null, null, EMPTY)
        );
    }
}
