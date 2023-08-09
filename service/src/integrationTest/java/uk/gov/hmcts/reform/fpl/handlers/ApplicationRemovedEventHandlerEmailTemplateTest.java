package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.ApplicationRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicationRemovedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveApplicationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    ApplicationRemovedEventHandler.class, ApplicationRemovedEmailContentProvider.class, EmailNotificationHelper.class,
    RemoveApplicationService.class, CaseUrlService.class, CtscTeamLeadLookupConfiguration.class
})
@MockBeans({
    @MockBean(FeatureToggleService.class), @MockBean(Time.class)
})
class ApplicationRemovedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final String CHILD_LAST_NAME = "Jones";
    private static final LocalDateTime REMOVAL_DATE = LocalDateTime.of(2010, 3, 20, 20, 20, 0);

    public static final AdditionalApplicationsBundle ADDITIONAL_APPLICATIONS = AdditionalApplicationsBundle.builder()
        .c2DocumentBundle(C2DocumentBundle.builder().type(WITH_NOTICE)
            .applicantName("Fred")
            .document(DocumentReference.builder().filename("C2").build())
            .supplementsBundle(List.of())
            .build())
        .otherApplicationsBundle(OtherApplicationsBundle.builder()
            .applicantName("Fred")
            .applicationType(C1_WITH_SUPPLEMENT)
            .document(DocumentReference.builder().filename("C1 With Supplement").build())
            .supplementsBundle(List.of())
            .build())
        .amountToPay("5000")
        .removalReason("Wrong case")
        .build();

    public static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .caseLocalAuthority(LOCAL_AUTHORITY_NAME)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .additionalApplicationsBundle(wrapElements(ADDITIONAL_APPLICATIONS))
        .build();

    @Autowired
    private ApplicationRemovedEventHandler underTest;

    @Autowired
    private Time time;

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(REMOVAL_DATE);
    }

    @Test
    void notifyTeamLeads() {
        underTest.notifyTeamLead(new ApplicationRemovedEvent(CASE_DATA, ADDITIONAL_APPLICATIONS));

        assertThat(response())
            .hasSubject("Refund needed for removed application, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The following file has been removed from this case, at the request of the applicant:")
                .line()
                .list("Case: 12345")
                .line()
                .list("File: C2, C1 With Supplement")
                .line()
                .list("Applicant: Fred")
                .line()
                .list("Removed on: 20 March 2010 at 8:20pm")
                .line()
                .list("Removal reason: Wrong case")
                .line()
                .h1("Next steps")
                .line()
                .line("An application fee of £50.00 needs to be refunded.")
                .line()
                .end("You can view the case details at " + caseDetailsUrl(CASE_ID))
            );
    }
}
