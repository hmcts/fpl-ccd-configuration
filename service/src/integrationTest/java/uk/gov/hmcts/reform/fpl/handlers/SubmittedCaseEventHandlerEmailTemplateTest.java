package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    SubmittedCaseEventHandler.class, OutsourcedCaseContentProvider.class,
    RegisteredRepresentativeSolicitorContentProvider.class, CaseUrlService.class, CafcassEmailContentProvider.class,
    HmctsEmailContentProvider.class, EmailNotificationHelper.class
})
@MockBeans(value = {
    @MockBean(PaymentService.class),
    @MockBean(EventService.class),
    @MockBean(TranslationRequestService.class),
    @MockBean(CafcassNotificationService.class)
})
class SubmittedCaseEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final DocumentReference C110A = mock(DocumentReference.class);
    private static final String BINARY_URL = "/some-uuid/binary";
    private static final CaseData CASE_DATA_BEFORE = CaseData.builder().state(OPEN).build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(123L)
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .representativeType(RepresentativeType.LOCAL_AUTHORITY)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .orders(Orders.builder().orderType(List.of(CARE_ORDER, SUPERVISION_ORDER)).build())
        .hearing(Hearing.builder().hearingUrgencyType(HearingUrgencyType.SAME_DAY).build())
        .outsourcingPolicy(OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID("ORG1")
                .organisationName("Third party org")
                .build())
            .build())
        .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
            .submittedForm(C110A)
            .build())
        .build();

    @Autowired
    private SubmittedCaseEventHandler underTest;

    @BeforeEach
    void setUp() {
        when(C110A.getBinaryUrl()).thenReturn(BINARY_URL);
    }

    @Test
    void notifyManagedLA() {
        underTest.notifyManagedLA(new SubmittedCaseEvent(CASE_DATA, CASE_DATA_BEFORE));

        assertThat(response())
            .hasSubject("Urgent application – same day hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line("Third party org has made a new application for:")
                .lines(3)
                .list("Care order", "Supervision order")
                .line()
                .line("Hearing date requested: same day")
                .line()
                .line("Respondent's surname: " + RESPONDENT_LAST_NAME)
                .line()
                .line("CCD case number: 123")
                .line()
                .line("Your organisation’s case access administrator should now assign the case "
                    + "to the relevant person.")
                .line()
                .line("They can view the application at https://manage-org.platform.hmcts.net")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyAdmin() {
        underTest.notifyAdmin(new SubmittedCaseEvent(CASE_DATA, CASE_DATA_BEFORE));

        assertThat(response())
            .hasSubject("Urgent application – same day hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line(LOCAL_AUTHORITY_NAME + " has made a new application for:")
                .lines(3)
                .list("Care order", "Supervision order")
                .line()
                .line("Hearing date requested: same day")
                .line()
                .line("Respondent’s surname: " + RESPONDENT_LAST_NAME)
                .line()
                .line("CCD case number: 123.")
                .line()
                .h1("Next steps")
                .line()
                .line("You now need to check:")
                .list("the application is complete", "if payment has been made")
                .line()
                .line("You can review the order by:")
                .list("signing into http://fake-url/cases/case-details/123")
                .end("* using this link http://fake-url" + BINARY_URL) // required due to the list always adding \n
            );
    }

    @Test
    void notifyCafcass() {
        underTest.notifyCafcass(new SubmittedCaseEvent(CASE_DATA, CASE_DATA_BEFORE));

        assertThat(response())
            .hasSubject("Urgent application – same day hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line(LOCAL_AUTHORITY_NAME + " has made a new application for:")
                .lines(3)
                .list("Care order", "Supervision order")
                .line()
                .line("Hearing date requested: same day")
                .line()
                .line("Respondent’s surname: " + RESPONDENT_LAST_NAME)
                .line()
                .line("CCD case number: 123.")
                .line()
                .h1("Next steps")
                .line("You can now start to prepare for the hearing.")
                .line()
                .line("You can review the application by using this link " + GOV_NOTIFY_DOC_URL + ".")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
