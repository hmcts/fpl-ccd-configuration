package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    LegalRepresentativesUpdatedHandler.class, LegalRepresentativeAddedContentProvider.class,
    LegalRepresentativesDifferenceCalculator.class, EmailNotificationHelper.class, CaseUrlService.class
})
class LegalRepresentativesUpdatedHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String CHILD_LAST_NAME = "Dorn";
    private static final String RESPONDENT_LAST_NAME = "Magnus";

    @MockBean
    private FeatureToggleService toggleService;
    @Autowired
    private LegalRepresentativesUpdatedHandler underTest;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void sendToLA(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("FAM_NUM")
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .legalRepresentatives(wrapElements(LegalRepresentative.builder()
                .fullName("Leman Russ")
                .email("fake@email.com")
                .build()
            ))
            .build();

        CaseData before = CaseData.builder().build();

        underTest.sendEmailToLegalRepresentativesAddedToCase(new LegalRepresentativesUpdated(caseData, before));

        assertThat(response())
            .hasSubject("You’ve been added to a case, " + name)
            .hasBody(emailContent()
                .line("Dear Leman Russ")
                .line()
                .line(LOCAL_AUTHORITY_NAME + " has added you to this case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .line("To view the case, sign in to:")
                .line("http://fake-url/cases/case-details/12345")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    private static Stream<Arguments> subjectLineSource() {
        return Stream.of(
            Arguments.of(true, CHILD_LAST_NAME),
            Arguments.of(false, RESPONDENT_LAST_NAME)
        );
    }
}
