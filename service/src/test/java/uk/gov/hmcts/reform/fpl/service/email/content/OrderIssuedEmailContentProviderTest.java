package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementOrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.PbaService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderNotificationDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryFinalMarker;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderLanguageRequirementGenerator;
import uk.gov.hmcts.reform.fpl.service.others.OthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.updaters.ChildrenSmartFinalOrderUpdater;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FAMILY_MAN_NUMBER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCMOParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForPlacementOrder;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {OrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class, FixedTimeConfiguration.class, ManageOrdersClosedCaseFieldGenerator.class,
    SealedOrderHistoryExtraTitleGenerator.class, SealedOrderHistoryTypeGenerator.class,
    LookupTestConfig.class, ChildrenService.class, AppointedGuardianFormatter.class,
    SealedOrderHistoryExtraTitleGenerator.class, OthersNotifiedGenerator.class,
    SealedOrderLanguageRequirementGenerator.class,
    OthersService.class, OrderIssuedEmailContentProviderTypeOfOrderCalculator.class,
    SealedOrderHistoryService.class, IdentityService.class, C43ChildArrangementOrderTitleGenerator.class,
    ChildrenSmartSelector.class, ChildrenSmartFinalOrderUpdater.class, ChildSelectionUtils.class, CourtService.class,
    PlacementService.class, RespondentService.class})
@MockBeans({
    @MockBean(OrderCreationService.class), @MockBean(SealedOrderHistoryExtraTitleGenerator.class),
    @MockBean(SealedOrderHistoryFinalMarker.class), @MockBean(OrderNotificationDocumentService.class),
    @MockBean(FeeService.class), @MockBean(PbaNumberService.class), @MockBean(DocumentSealingService.class),
    @MockBean(RespondentService.class), @MockBean(DocmosisDocumentGeneratorService.class),
    @MockBean(UploadDocumentService.class), @MockBean(HearingVenueLookUpService.class),
    @MockBean(HighCourtAdminEmailLookupConfiguration.class), @MockBean(UserService.class),
    @MockBean(PbaService.class)
})
class OrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Child TEST_CHILD = testChild("Theodore", "Bailey").getValue();

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .familyManCaseNumber(TEST_FAMILY_MAN_NUMBER)
        .orderCollection(createOrders(testDocument))
        .hearingDetails(createHearingBookings(
            LocalDateTime.now().plusMonths(3), LocalDateTime.now().plusMonths(3).plusHours(1)
        ))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Jones").build())
            .build()))
        .children1(wrapElements(TEST_CHILD))
        .build();

    @Autowired
    private OrderIssuedEmailContentProvider underTest;

    @MockBean
    private OrderIssuedEmailContentProviderTypeOfOrderCalculator calculator;

    @Test
    void shouldBuildGeneratedOrderParametersWithCaseUrl() {
        when(calculator.getTypeOfOrder(CASE_DATA, GENERATED_ORDER)).thenReturn("blank order (c21)");

        NotifyData expectedParameters = getExpectedParameters(BLANK_ORDER.getLabel(), true);
        NotifyData actualParameters = underTest.getNotifyDataWithCaseUrl(
            CASE_DATA, testDocument, GENERATED_ORDER
        );

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersWithoutCaseUrl() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);
        when(calculator.getTypeOfOrder(CASE_DATA, GENERATED_ORDER)).thenReturn("blank order (c21)");

        NotifyData expectedParameters = getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true);
        NotifyData actualParameters = underTest.getNotifyDataWithoutCaseUrl(
            CASE_DATA, testDocumentReference(), GENERATED_ORDER
        );

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildNoticeOfPlacementOrderParameters() {
        when(calculator.getTypeOfOrder(CASE_DATA, NOTICE_OF_PLACEMENT_ORDER)).thenReturn("notice of placement order");

        NotifyData expectedParameters = getExpectedParameters(NOTICE_OF_PLACEMENT_ORDER.getLabel(), false);
        NotifyData actualParameters = underTest.getNotifyDataWithCaseUrl(
            CASE_DATA, testDocument, NOTICE_OF_PLACEMENT_ORDER
        );

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCaseManagementOrderParameters() {
        UUID hearingId = randomUUID();
        CaseData data = CASE_DATA.toBuilder()
            .lastHearingOrderDraftsHearingId(hearingId)
            .hearingDetails(List.of(
                element(HearingBooking.builder().startDate(LocalDateTime.now().plusMonths(6)).build()),
                element(hearingId, HearingBooking.builder().startDate(LocalDateTime.now().minusDays(3)).build())))
            .build();

        when(calculator.getTypeOfOrder(data, CMO)).thenReturn("case management order");

        NotifyData expectedParameters = getExpectedCMOParameters(CMO.getLabel());
        NotifyData actualParameters = underTest.getNotifyDataForCMO(data, testDocument, CMO);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildPlacementOrderNotifyData() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);

        PlacementOrderIssuedNotifyData notifyData =
            underTest.getNotifyDataForPlacementOrder(CASE_DATA, testDocument, TEST_CHILD);

        assertThat(notifyData).isEqualTo(getExpectedParametersForPlacementOrder());
    }

}
