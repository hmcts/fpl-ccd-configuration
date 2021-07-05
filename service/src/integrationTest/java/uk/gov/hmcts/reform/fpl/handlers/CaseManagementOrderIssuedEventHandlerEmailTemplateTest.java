package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProviderTypeOfOrderCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraOthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryFinalMarker;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryTypeGenerator;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    CaseManagementOrderIssuedEventHandler.class, CaseManagementOrderEmailContentProvider.class,
    EmailNotificationHelper.class, CaseUrlService.class, IssuedOrderAdminNotificationHandler.class,
    OrderIssuedEmailContentProvider.class, FixedTimeConfiguration.class, SealedOrderHistoryService.class,
    OrderIssuedEmailContentProviderTypeOfOrderCalculator.class, ChildrenSmartSelector.class
})
@MockBeans({
    @MockBean(CoreCaseDataService.class), @MockBean(IdentityService.class), @MockBean(ChildrenService.class),
    @MockBean(OrderCreationService.class), @MockBean(ManageOrdersClosedCaseFieldGenerator.class),
    @MockBean(SealedOrderHistoryExtraTitleGenerator.class), @MockBean(SealedOrderHistoryTypeGenerator.class),
    @MockBean(SealedOrderHistoryFinalMarker.class), @MockBean(AppointedGuardianFormatter.class),
    @MockBean(OthersService.class), @MockBean(SealedOrderHistoryExtraOthersNotifiedGenerator.class)
})
class CaseManagementOrderIssuedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String RESPONDENT_LAST_NAME = "khorne";
    private static final String CHILD_LAST_NAME = "nurgle";
    private static final long CASE_ID = 123456L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";

    @MockBean
    private FeatureToggleService toggleService;

    @Autowired
    private CaseManagementOrderIssuedEventHandler underTest;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void notifyParties(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);
        UUID hearingId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .hearingDetails(List.of(element(hearingId, HearingBooking.builder()
                .startDate(LocalDateTime.of(2021, 6, 8, 0, 0, 0))
                .build())))
            .lastHearingOrderDraftsHearingId(hearingId)
            .build();
        HearingOrder cmo = HearingOrder.builder()
            .order(DocumentReference.builder().binaryUrl("/some-url/binary").build())
            .hearing("some hearing")
            .build();

        underTest.notifyParties(new CaseManagementOrderIssuedEvent(caseData, cmo));

        List<SendEmailResponse> responses = allResponses();

        SendEmailResponse laResponse = responses.get(0);
        SendEmailResponse digitalRepResponse = responses.get(2);
        List.of(laResponse, digitalRepResponse).forEach(response ->
            assertThat(response)
                .hasSubject("CMO issued, " + name)
                .hasBody(emailContent()
                    .line("The case management order has been issued for:")
                    .line()
                    .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", some hearing")
                    .line()
                    .h1("Next steps")
                    .line()
                    .line("You should now check the order to see your directions and compliance dates.")
                    .line()
                    .line("You can review the order by:")
                    .list("signing into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                    .line()
                    .list("using this link http://fake-url/some-url/binary")
                    .line()
                    .line("HM Courts & Tribunals Service")
                    .line()
                    .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                        + "contactfpl@justice.gov.uk")
                )
        );

        SendEmailResponse cafcassResponse = responses.get(1);
        SendEmailResponse emailRepResponse = responses.get(3);
        List.of(cafcassResponse, emailRepResponse).forEach(response ->
            assertThat(response)
                .hasSubject("CMO issued, " + name)
                .hasBody(emailContent()
                    .line("The case management order has been issued for:")
                    .line()
                    .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", some hearing")
                    .line()
                    .h1("Next steps")
                    .line()
                    .line("You should now check the order to see your directions and compliance dates.")
                    .line()
                    .line("You can review the order by:")
                    .line()
                    .line()
                    .list("using this link " + GOV_NOTIFY_DOC_URL)
                    .line()
                    .line("HM Courts & Tribunals Service")
                    .line()
                    .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                        + "contactfpl@justice.gov.uk")
                )
        );

        SendEmailResponse hmctsResponse = responses.get(4);
        assertThat(hmctsResponse)
            .hasSubject("New case management order issued, " + name)
            .hasBody(emailContent()
                .line("A new case management order has been issued by Family Court")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM, hearing 8 Jun 2021")
                .line()
                .line("You should now check the order and do any required tasks or case updates.")
                .line()
                .line("You can review the order by:")
                .line()
                .list("signing into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS),
                    "using this link http://fake-url/some-url/binary")
                .line()
                .line("HM Courts & Tribunal Service")
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
