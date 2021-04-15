package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    RespondentsUpdatedEventHandler.class,
    NotificationService.class,
    RespondentSolicitorContentProvider.class,
    LocalAuthorityNameLookupConfiguration.class,
    CaseUrlService.class,
    ObjectMapper.class,
})
class RespondentsUpdatedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_FIRST_NAME = "John";
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName(RESPONDENT_LAST_NAME).build())
        .build();

    @Autowired
    private RespondentsUpdatedEventHandler underTest;

    @ParameterizedTest
    @MethodSource("representativeNameSource")
    void notifyRegisteredSolicitor(String firstName, String lastName, String expectedSalutation) {
        final Respondent respondent1 = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email("solicitor@test.com")
                .firstName(firstName)
                .lastName(lastName)
                .organisation(Organisation.builder().organisationID("123").organisationName("Organisation1").build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(respondent1))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(OPEN)
            .build();

        underTest.notifyRegisteredRespondentSolicitors(new SubmittedCaseEvent(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New C110A application for your client")
            .hasBody(emailContent()
                .start()
                .line(expectedSalutation)
                .line()
                .line(LOCAL_AUTHORITY_NAME + " has made a new C110A application on the Family"
                    + " Public Law (FPL) digital service.")
                .line()
                .line("They’ve given your details as a respondent’s legal representative.")
                .line()
                .line(
                    "You should now ask your organisation's FPL case access administrator to assign the case to you.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("representativeNameSource")
    void notifyUnregisteredSolicitors(String firstName, String lastName, String expectedSalutation) {
        Respondent respondent = RESPONDENT.toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email("RespondentSolicitor@test.com")
                .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered Org Name").build())
                .build()
            ).build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(respondent))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(OPEN)
            .build();

        underTest.notifyUnregisteredSolicitors(new RespondentsUpdated(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New C110A application for your client")
            .hasBody(emailContent()
                .start()
                .line(expectedSalutation)
                .line()
                .line(LOCAL_AUTHORITY_NAME + " has made a new C110A application on the Family Public Law digital "
                    + "service.")
                .line()
                .line("They’ve given your details as a respondent’s legal representative.")
                .line()
                .line("Legal representatives must be registered to use the service.")
                .line()
                .line("After you’ve registered, you’ll be able to:")
                .line()
                .list("access relevant case files")
                .list("upload your own statements and reports")
                .list("make applications in the case, for example C2")
                .line()
                .line("You can register at https://manage-org.platform.hmcts.net/register-org/register")
                .line()
                .line("You’ll need your organisation’s Pay By Account (PBA) details.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    private static Stream<Arguments> representativeNameSource() {
        String expectedSalutation = String.join(" ", "Dear", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME);
        return Stream.of(
            Arguments.of(RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME, expectedSalutation),
            Arguments.of(null, null, EMPTY));
    }
}
