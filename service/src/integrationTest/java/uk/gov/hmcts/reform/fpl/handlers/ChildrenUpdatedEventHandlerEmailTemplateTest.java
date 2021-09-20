package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.ChildrenUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.diff.ChildRepresentativeDiffCalculator;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    ChildrenUpdatedEventHandler.class, ChildRepresentativeDiffCalculator.class, EmailNotificationHelper.class,
    RegisteredRepresentativeSolicitorContentProvider.class, UnregisteredRepresentativeSolicitorContentProvider.class
})
@MockBeans({@MockBean(FeatureToggleService.class)})
class ChildrenUpdatedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String CASE_NAME = "FPL case test";
    private static final String CHILD_FIRST_NAME = "Sherlock";
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final String SOLICITOR_FIRST_NAME = "John";
    private static final String SOLICITOR_LAST_NAME = "Watson";
    private static final long CASE_ID = 1234567890123456L;

    @Autowired
    private ChildrenUpdatedEventHandler underTest;

    @Test
    void notifyRegisteredSolicitor() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder()
                    .firstName(CHILD_FIRST_NAME)
                    .lastName(CHILD_LAST_NAME)
                    .dateOfBirth(LocalDate.now())
                    .build()
                )
                .solicitor(RespondentSolicitor.builder()
                    .email("solicitor@test.com")
                    .firstName(SOLICITOR_FIRST_NAME)
                    .lastName(SOLICITOR_LAST_NAME)
                    .organisation(Organisation.builder().organisationID("123").organisationName("Org123").build())
                    .build()
                )
                .build()
            ))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseName("FPL case test")
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder()
                    .firstName(CHILD_FIRST_NAME)
                    .lastName(CHILD_LAST_NAME)
                    .dateOfBirth(LocalDate.now())
                    .build()
                )
                .build()
            ))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseName("FPL case test")
            .build();

        underTest.notifyRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New C110A application, FPL case test, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line(format("Dear %s %s", SOLICITOR_FIRST_NAME, SOLICITOR_LAST_NAME))
                .line()
                .line(format("%s has made the following C110A application on the Family Public Law (FPL) digital"
                             + " service:", LOCAL_AUTHORITY_NAME
                ))
                .line()
                .callout(format(" %s, %s", CASE_NAME, CASE_ID))
                .line()
                .line(format("They’ve given your details as the legal representative for %s %s.",
                    CHILD_FIRST_NAME, CHILD_LAST_NAME
                ))
                .line()
                .line("You should now ask your organisation's FPL case access administrator to assign the case to you.")
                .line()
                .line("They'll need to sign into https://manage-org.platform.hmcts.net")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end(
                    "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk"
                )
            );
    }

    @Test
    void notifyUnregisteredSolicitors() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder()
                    .firstName(CHILD_FIRST_NAME)
                    .lastName(CHILD_LAST_NAME)
                    .dateOfBirth(LocalDate.now())
                    .build()
                )
                .solicitor(RespondentSolicitor.builder()
                    .email("solicitor@test.com")
                    .firstName(SOLICITOR_FIRST_NAME)
                    .lastName(SOLICITOR_LAST_NAME)
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Org123").build())
                    .build()
                )
                .build()
            ))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseName("FPL case test")
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName(CHILD_FIRST_NAME).lastName(CHILD_LAST_NAME).build())
                .build()))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .caseName("FPL case test")
            .build();

        underTest.notifyUnRegisteredSolicitors(new ChildrenUpdated(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New C110a application involving your client, FPL case test, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line(format("%s has made a new C110a application on the Family Public Law digital service.",
                    LOCAL_AUTHORITY_NAME
                ))
                .line()
                .line(format("They’ve given your details as the legal representative for %s %s.",
                    CHILD_FIRST_NAME, CHILD_LAST_NAME
                ))
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
                .list(
                    "reference number 1234-5678-9012-3456",
                    "the name of the local authority that made the application",
                    "your client's first and last names"
                )
                .line()
                .line("You’ll then be able to:")
                .list(
                    "access relevant case files",
                    "upload your own statements and reports",
                    "make applications in the case, for example C2"
                )
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end(
                    "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk"
                )
            );
    }
}
