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
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    SubmittedCaseEventHandler.class,
    NotificationService.class,
    OutsourcedCaseContentProvider.class,
    LocalAuthorityNameLookupConfiguration.class,
    RespondentSolicitorContentProvider.class,
    CaseUrlService.class,
    ObjectMapper.class,
})
class SubmittedCaseEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName(RESPONDENT_LAST_NAME).build())
        .legalRepresentation(YES.getValue())
        .solicitor(RespondentSolicitor.builder()
            .email("solicitor@test.com")
            .firstName("John")
            .lastName("Watson")
            .organisation(Organisation.builder().organisationID("123").organisationName("Organisation1").build())
            .build())
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
            .hasSubject("Urgent application – same day hearing, Watson")
            .hasBody(emailContent()
                .start()
                .line("Third party org has made a new application for:")
                .line()
                .line("\n\n* Care order\n* Supervision order")
                .line()
                .line("Hearing date requested: same day")
                .line()
                .line("Respondent's surname: Watson")
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
    void notifyRegisteredSolicitor() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(RESPONDENT))
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
                .line("Dear John Watson")
                .line()
                .line(LOCAL_AUTHORITY_NAME + " has made a new C100A application on the Family"
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
}
