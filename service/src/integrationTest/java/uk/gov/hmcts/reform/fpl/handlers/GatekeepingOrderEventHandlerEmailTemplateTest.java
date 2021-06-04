package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_AND_NOP;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.URGENT_AND_NOP;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {
    GatekeepingOrderEventHandler.class,
    SDOIssuedCafcassContentProvider.class,
    CaseUrlService.class,
    SDOIssuedContentProvider.class,
    CtscEmailLookupConfiguration.class,
    EmailNotificationHelper.class
})
class GatekeepingOrderEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final long ID = 1234567890123456L;
    private static final DocumentReference ORDER_DOC = testDocumentReference();
    private static final String RESPONDENT_LAST_NAME = "Stevens";
    private static final String CHILD_LAST_NAME = "Richards";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(ID)
        .caseLocalAuthority("LA_CODE")
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .hearingDetails(wrapElements(HearingBooking.builder()
            .startDate(LocalDateTime.of(2021, 5, 12, 0, 0, 0))
            .build()))
        .build();
    private static final GatekeepingOrderEvent.GatekeepingOrderEventBuilder EVENT_BUILDER =
        GatekeepingOrderEvent.builder()
            .caseData(CASE_DATA)
            .order(ORDER_DOC);

    @Autowired
    private GatekeepingOrderEventHandler underTest;

    @MockBean
    private FeatureToggleService toggleService;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void cafcassSDOAndNoPEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCafcass(EVENT_BUILDER.notificationGroup(SDO_AND_NOP).build());

        assertThat(response())
            .hasSubject("SDO and notice of proceedings issued, " + name)
            .hasBody(emailContent()
                .line("The standard directions order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps ")
                .line("You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review it by using this link " + GOV_NOTIFY_DOC_URL + ".")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void cafcassUrgentHearingOrderAndNoPEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCafcass(EVENT_BUILDER.notificationGroup(URGENT_AND_NOP).build());

        assertThat(response())
            .hasSubject("Urgent hearing order and notice of proceedings issued, " + name)
            .hasBody(emailContent()
                .line("An urgent hearing order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line("You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review it by using this link " + GOV_NOTIFY_DOC_URL + ".")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void cafcassSDOEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCafcass(EVENT_BUILDER.notificationGroup(SDO).build());

        assertThat(response())
            .hasSubject("Gatekeeping order issued, " + name)
            .hasBody(emailContent()
                .line("The gatekeeping order has been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line("You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review it by using this link " + GOV_NOTIFY_DOC_URL + ".")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void ctcsSDOAndNoPEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCTSC(EVENT_BUILDER.notificationGroup(SDO_AND_NOP).build());

        assertThat(response())
            .hasSubject("SDO and notice of proceedings issued, " + name)
            .hasBody(emailContent()
                .line("The standard directions order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line(" ")
                .end("To view the order, sign into " + caseDetailsUrl(ID, ORDERS))
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void ctcsUrgentHearingOrderAndNoPEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCTSC(EVENT_BUILDER.notificationGroup(URGENT_AND_NOP).build());

        assertThat(response())
            .hasSubject("Urgent hearing order and notice of proceedings issued, " + name)
            .hasBody(emailContent()
                .line("An urgent hearing order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .end("To view the order, sign into " + caseDetailsUrl(ID, ORDERS))
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void ctcsSDOEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyCTSC(EVENT_BUILDER.notificationGroup(SDO).build());

        assertThat(response())
            .hasSubject("Gatekeeping order issued, " + name)
            .hasBody(emailContent()
                .line("The gatekeeping order has been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .line("You can review it by signing in to: " + caseDetailsUrl(ID, ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void laSDOAndNoPEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyLocalAuthority(EVENT_BUILDER.notificationGroup(SDO_AND_NOP).build());

        assertThat(response())
            .hasSubject("SDO and notice of proceedings issued, " + name)
            .hasBody(emailContent()
                .line("The standard directions order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line("You should now:")
                .list("serve all parties with the SDO", "check your directions and compliance dates")
                .line()
                .line("To view the order, sign in to:")
                .line(caseDetailsUrl(ID, ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void laUrgentHearingOrderAndNoPEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyLocalAuthority(EVENT_BUILDER.notificationGroup(URGENT_AND_NOP).build());

        assertThat(response())
            .hasSubject("Urgent hearing order and notice of proceedings issued, " + name)
            .hasBody(emailContent()
                .line("An urgent hearing order and notice of proceedings have been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line("You should now:")
                .list("serve all parties with the order", "check for directions and compliance dates")
                .line()
                .line("To view the order, sign in to:")
                .line(caseDetailsUrl(ID, ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void laSDOEmailTemplate(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.notifyLocalAuthority(EVENT_BUILDER.notificationGroup(SDO).build());

        assertThat(response())
            .hasSubject("Gatekeeping order issued, " + name)
            .hasBody(emailContent()
                .line("The gatekeeping order has been issued for:")
                .line()
                .callout("Stevens, FAM_NUM, hearing 12 May 2021")
                .line()
                .h1("Next steps")
                .line("You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review it by signing in to: " + caseDetailsUrl(ID, ORDERS))
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
