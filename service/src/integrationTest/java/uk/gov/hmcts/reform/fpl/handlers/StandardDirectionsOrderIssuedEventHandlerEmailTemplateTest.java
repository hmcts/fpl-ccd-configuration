package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@SpringBootTest(classes = {
    StandardDirectionsOrderIssuedEventHandler.class,
    NotificationService.class,
    SDOIssuedCafcassContentProvider.class,
    CaseUrlService.class,
    SDOIssuedContentProvider.class,
    ObjectMapper.class,
    CtscEmailLookupConfiguration.class
})
public class StandardDirectionsOrderIssuedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final long ID = 1234567890123456L;
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(ID)
        .caseLocalAuthority("LA_CODE")
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Stevens").build())
            .build()))
        .hearingDetails(wrapElements(HearingBooking.builder()
            .startDate(LocalDateTime.of(2021, 5, 12, 0, 0, 0))
            .build()))
        .standardDirectionOrder(StandardDirectionOrder.builder()
            .orderDoc(testDocumentReference())
            .build())
        .build();

    @Autowired
    private StandardDirectionsOrderIssuedEventHandler underTest;

    @Test
    void cafcassEmailTemplate() {
        underTest.notifyCafcass(new StandardDirectionsOrderIssuedEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("SDO and notice of proceedings issued, Stevens")
            .hasBody(emailContent()
                .line("The standard directions order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line(" You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review it by using this link " + GOV_NOTIFY_DOC_URL + ".")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void ctcsEmailTemplate() {
        underTest.notifyCTSC(new StandardDirectionsOrderIssuedEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("SDO and notice of proceedings issued, Stevens")
            .hasBody(emailContent()
                .line("The standard directions order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .line(" To view the order, sign into " + caseDetailsUrl(ID, ORDERS))
            );
    }

    @Test
    void laEmailTemplate() {
        underTest.notifyLocalAuthority(new StandardDirectionsOrderIssuedEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("SDO and notice of proceedings issued, Stevens")
            .hasBody(emailContent()
                .line("The standard directions order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line("You should now:")
                .line()
                .list("serve all parties with the SDO", "check your directions and compliance dates")
                .line()
                .line("To view the order, sign in to:")
                .line(caseDetailsUrl(ID, ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }
}
