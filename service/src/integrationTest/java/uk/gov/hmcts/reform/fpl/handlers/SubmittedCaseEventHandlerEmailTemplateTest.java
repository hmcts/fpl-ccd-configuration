package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    SubmittedCaseEventHandler.class,
    NotificationService.class,
    OutsourcedCaseContentProvider.class,
    RespondentSolicitorContentProvider.class,
    LocalAuthorityNameLookupConfiguration.class,
    CaseUrlService.class,
    ObjectMapper.class,
})
class SubmittedCaseEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final String SOLICITOR_LAST_NAME = "Smith";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder()
        .lastName(RESPONDENT_LAST_NAME).build())
        .build();

    @Autowired
    private SubmittedCaseEventHandler underTest;

    @MockBean
    private PaymentService paymentService;

    //tech-debt: add test for admin email - a1562b56-4ff7-4e3e-b62d-5fb9f086ee8f
    @MockBean
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    //tech-debt: add test for cafcass email - e5630e8b-3b25-4773-a41a-a42af123bebc
    @MockBean
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Test
    void notifyManagedLA() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(RESPONDENT))
            .orders(Orders.builder().orderType(List.of(CARE_ORDER, SUPERVISION_ORDER)).build())
            .hearing(Hearing.builder().timeFrame("Same day").build())
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationName("Third party org").build())
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(OPEN)
            .build();

        underTest.notifyManagedLA(new SubmittedCaseEvent(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("Urgent application – same day hearing, " + RESPONDENT_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line("Third party org has made a new application for:")
                .line()
                .line("\n\n* Care order\n* Supervision order")
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
    void notifyUnregisteredSolicitors() {
        Respondent respondent = RESPONDENT.toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .lastName(SOLICITOR_LAST_NAME)
                .email("RespondentSolicitor@test.com")
                .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered Org Name").build())
                .build()
            ).build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(respondent))
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(OPEN)
            .build();

        underTest.notifyUnregisteredSolicitors(new SubmittedCaseEvent(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("New C110A application for your client")
            .hasBody(emailContent()
                .start()
                .line("Dear " + SOLICITOR_LAST_NAME)
                .line()
                .line(LOCAL_AUTHORITY_1_NAME + " has made a new C110A application on the Family Public Law digital "
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
}
