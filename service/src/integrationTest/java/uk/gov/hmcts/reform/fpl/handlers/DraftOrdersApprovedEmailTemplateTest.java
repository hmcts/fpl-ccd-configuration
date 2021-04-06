package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersApprovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    DraftOrdersApprovedEventHandler.class,
    ReviewDraftOrdersEmailContentProvider.class,
    NotificationService.class,
    ObjectMapper.class,
    CaseUrlService.class,
    RepresentativeNotificationService.class
})
class DraftOrdersApprovedEmailTemplateTest extends EmailTemplateTest {
    @MockBean
    private SendDocumentService sendDocumentService;

    @Autowired
    private DraftOrdersApprovedEventHandler underTest;

    @Test
    void notifyLA() {

        Element<HearingBooking> hearingBooking = element(HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, Month.FEBRUARY, 1, 11, 11, 11))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Smith")
                .judgeEmailAddress("test@test.com")
                .build())
            .build());

        HearingOrder cmo = HearingOrder.builder()
            .type(AGREED_CMO)
            .title("Agreed CMO discussed at hearing")
            .order(DocumentReference.builder().binaryUrl("/54321").build())
            .build();

        HearingOrder c21 = HearingOrder.builder()
            .type(C21)
            .title("Test order")
            .order(DocumentReference.builder().binaryUrl("/99999").build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(100L)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName("Smith")
                    .build())
                .build()))
            .hearingDetails(List.of(hearingBooking))
            .ordersToBeSent(List.of(element(cmo), element(c21)))
            .lastHearingOrderDraftsHearingId(hearingBooking.getId())
            .build();

        DraftOrdersApproved event = new DraftOrdersApproved(caseData, List.of(cmo, c21));

        underTest.sendNotificationToAdminAndLA(event);

        assertThat(response())
            .hasSubject("New orders issued, Smith")
            .hasBody(emailContent()
                .line("New orders have been issued for:")
                .line()
                .callout("Smith, case management hearing, 1 February 2020")
                .line()
                .line("The orders are:")
                .line()
                .callout("Agreed CMO discussed at hearing\nTest order")
                .line()
                .h1("Next steps")
                .line()
                .line("You should now check the orders to see if you have any directions and compliance dates.")
                .line()
                .line("You can review the orders by:")
                .list("signing into http://fake-url/cases/case-details/100#Orders")
                .list("using these links: \n\n* http://fake-url/54321\n* http://fake-url/99999")
                .lines(23)
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );

        verifyNoInteractions(sendDocumentService);
    }
}