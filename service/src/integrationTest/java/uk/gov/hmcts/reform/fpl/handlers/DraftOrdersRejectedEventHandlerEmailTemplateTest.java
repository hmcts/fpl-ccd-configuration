package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersRejectedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    DraftOrdersRejectedEventHandler.class, ReviewDraftOrdersEmailContentProvider.class, EmailNotificationHelper.class,
    CaseUrlService.class
})
@MockBeans(value = {
    @MockBean(FurtherEvidenceNotificationService.class)
})
class DraftOrdersRejectedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String CHILD_LAST_NAME = "Smith";
    private static final String RESPONDENT_LAST_NAME = "Baker";
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final long CASE_ID = 12345L;

    @Autowired
    private DraftOrdersRejectedEventHandler underTest;
    @Autowired
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @BeforeEach
    void setUp() {
        when(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(any()))
            .thenReturn(Set.of("designatedLA@gmail.com"));
        when(furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(any()))
            .thenReturn(Set.of("secondaryLA@gmail.com"));
    }

    @Test
    void sendNotificationForExistingDraftOrder() {
        UUID lastHearingId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = List.of(element(lastHearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.of(0, 0)))
            .build()));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()
            ))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()
            ))
            .lastHearingOrderDraftsHearingId(lastHearingId)
            .hearingDetails(hearings)
            .build();

        List<HearingOrder> rejectedOrders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .build());

        underTest.sendNotifications(new DraftOrdersRejected(caseData, rejectedOrders));

        assertThat(response())
            .hasSubject("Changes needed on draft orders, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Baker, FAM_NUM, case management hearing, 1 February 2021")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: ")
                .line()
                .list("Order 1 - Missing information about XYZ", "Order 2 - Please change ABC")
                .line()
                .line("Sign in to upload revised orders at " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                     + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void sendNotificationsIfUploadedByDesignatedLA() {
        UUID lastHearingId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = List.of(element(lastHearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.of(0, 0)))
            .build()));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()
            ))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()
            ))
            .lastHearingOrderDraftsHearingId(lastHearingId)
            .hearingDetails(hearings)
            .build();

        List<HearingOrder> rejectedOrders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
                .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .uploaderCaseRoles(List.of(CaseRole.LASOLICITOR))
                .uploaderType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY)
                .build());

        underTest.sendNotifications(new DraftOrdersRejected(caseData, rejectedOrders));

        assertThat(response())
            .hasSubject("Changes needed on draft orders, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Baker, FAM_NUM, case management hearing, 1 February 2021")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: ")
                .line()
                .list("Order 1 - Missing information about XYZ", "Order 2 - Please change ABC")
                .line()
                .line("Sign in to upload revised orders at " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void sendNotificationsIfUploadedBySecondaryLA() {
        UUID lastHearingId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = List.of(element(lastHearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.of(0, 0)))
            .build()));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()
            ))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()
            ))
            .lastHearingOrderDraftsHearingId(lastHearingId)
            .hearingDetails(hearings)
            .build();

        List<HearingOrder> rejectedOrders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .uploaderCaseRoles(List.of(CaseRole.LASHARED))
                .uploaderType(DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY)
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .uploaderCaseRoles(List.of(CaseRole.LASHARED))
                .uploaderType(DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY)
                .build());

        underTest.sendNotifications(new DraftOrdersRejected(caseData, rejectedOrders));

        assertThat(response())
            .hasSubject("Changes needed on draft orders, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Baker, FAM_NUM, case management hearing, 1 February 2021")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: ")
                .line()
                .list("Order 1 - Missing information about XYZ", "Order 2 - Please change ABC")
                .line()
                .line("Sign in to upload revised orders at " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "CHILDSOLICITORA", "CHILDSOLICITORB", "CHILDSOLICITORC", "CHILDSOLICITORD", "CHILDSOLICITORE",
        "CHILDSOLICITORF", "CHILDSOLICITORG", "CHILDSOLICITORH", "CHILDSOLICITORI", "CHILDSOLICITORJ",
        "CHILDSOLICITORK", "CHILDSOLICITORL", "CHILDSOLICITORM", "CHILDSOLICITORN", "CHILDSOLICITORO"
    })
    void sendNotificationsIfUploadedByChildSolicitor(CaseRole childCaseRole) {
        when(furtherEvidenceNotificationService.getChildSolicitorEmails(any(), eq(childCaseRole)))
            .thenReturn(Set.of("child@gmail.com"));
        UUID lastHearingId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = List.of(element(lastHearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.of(0, 0)))
            .build()));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()
            ))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()
            ))
            .lastHearingOrderDraftsHearingId(lastHearingId)
            .hearingDetails(hearings)
            .build();

        List<HearingOrder> rejectedOrders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .uploaderCaseRoles(List.of(childCaseRole))
                .uploaderType(DocumentUploaderType.SOLICITOR)
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .uploaderCaseRoles(List.of(childCaseRole))
                .uploaderType(DocumentUploaderType.SOLICITOR)
                .build());

        underTest.sendNotifications(new DraftOrdersRejected(caseData, rejectedOrders));

        assertThat(response())
            .hasSubject("Changes needed on draft orders, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Baker, FAM_NUM, case management hearing, 1 February 2021")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: ")
                .line()
                .list("Order 1 - Missing information about XYZ", "Order 2 - Please change ABC")
                .line()
                .line("Sign in to upload revised orders at " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "SOLICITORA", "SOLICITORB", "SOLICITORC", "SOLICITORD", "SOLICITORE", "SOLICITORF", "SOLICITORG", "SOLICITORH",
        "SOLICITORI", "SOLICITORJ"
    })
    void sendNotificationsIfUploadedByRespondentSolicitor(CaseRole respondentCaseRole) {
        when(furtherEvidenceNotificationService.getRespondentSolicitorEmails(any(), eq(respondentCaseRole)))
            .thenReturn(Set.of("respondent@gmail.com"));
        UUID lastHearingId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = List.of(element(lastHearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.of(0, 0)))
            .build()));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()
            ))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()
            ))
            .lastHearingOrderDraftsHearingId(lastHearingId)
            .hearingDetails(hearings)
            .build();

        List<HearingOrder> rejectedOrders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .uploaderCaseRoles(List.of(respondentCaseRole))
                .uploaderType(DocumentUploaderType.SOLICITOR)
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .uploaderCaseRoles(List.of(respondentCaseRole))
                .uploaderType(DocumentUploaderType.SOLICITOR)
                .build());

        underTest.sendNotifications(new DraftOrdersRejected(caseData, rejectedOrders));

        assertThat(response())
            .hasSubject("Changes needed on draft orders, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Baker, FAM_NUM, case management hearing, 1 February 2021")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: ")
                .line()
                .list("Order 1 - Missing information about XYZ", "Order 2 - Please change ABC")
                .line()
                .line("Sign in to upload revised orders at " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
