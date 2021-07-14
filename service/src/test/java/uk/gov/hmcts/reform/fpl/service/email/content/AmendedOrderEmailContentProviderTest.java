package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.AmendedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraOthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryFinalMarker;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryTypeGenerator;
import uk.gov.hmcts.reform.fpl.updaters.ChildrenSmartFinalOrderUpdater;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {AmendedOrderEmailContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class, FixedTimeConfiguration.class,
    ChildrenService.class, AppointedGuardianFormatter.class,
    OthersService.class})
class AmendedOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .familyManCaseNumber("SACCCCCCCC5676576567")
        .orderCollection(createOrders(testDocument))
        .hearingDetails(createHearingBookings(
            LocalDateTime.now().plusMonths(3), LocalDateTime.now().plusMonths(3).plusHours(1)
        ))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Jones").build())
            .build()))
        .build();

    @Autowired
    private AmendedOrderEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void setUp() {
        when(helper.getSubjectLineLastName(CASE_DATA)).thenReturn("Jones");
    }

    @Test
    void shouldBuildAmendedOrderParameters() {
        NotifyData expectedParameters = getExpectedParameters();
        NotifyData actualParameters = underTest.getNotifyData(
            CASE_DATA, testDocument, AmendedOrderType.CASE_MANAGEMENT_ORDER.getLabel());

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    public static OrderAmendedNotifyData getExpectedParameters() {
        return OrderAmendedNotifyData.builder()
            .orderType("case management order")
            .callout("^Jones, SACCCCCCCC5676576567, hearing 14 Oct 2021")
            .courtName("Family Court")
            .documentLink("http://fake-url/testUrl")
            .caseUrl("http://fake-url/cases/case-details/12345#Orders")
            .lastName("Jones")
            .build();
    }
}
