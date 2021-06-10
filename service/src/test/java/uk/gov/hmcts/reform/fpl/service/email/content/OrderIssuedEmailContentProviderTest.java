package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {OrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class, FixedTimeConfiguration.class, DischargeCareOrderService.class,
    OrderIssuedEmailContentProviderTypeOfOrderCalculator.class, SealedOrderHistoryService.class, IdentityService.class})
class OrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final CaseData caseData = createCase();

    @Autowired
    private OrderIssuedEmailContentProvider underTest;

    @MockBean
    private ChildrenService childrenService;

    @MockBean
    private OrderCreationService orderCreationService;

    @Test
    void shouldBuildGeneratedOrderParametersWithCaseUrl() {
        NotifyData expectedParameters = getExpectedParameters(BLANK_ORDER.getLabel(), true);
        NotifyData actualParameters = underTest.getNotifyDataWithCaseUrl(
            caseData, testDocument, GENERATED_ORDER);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersWithoutCaseUrl() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);

        NotifyData expectedParameters = getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true);
        NotifyData actualParameters = underTest.getNotifyDataWithoutCaseUrl(
            caseData, testDocumentReference(), GENERATED_ORDER);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildNoticeOfPlacementOrderParameters() {
        NotifyData expectedParameters = getExpectedParameters(NOTICE_OF_PLACEMENT_ORDER.getLabel(),
            false);
        NotifyData actualParameters = underTest.getNotifyDataWithCaseUrl(
            caseData, testDocument, NOTICE_OF_PLACEMENT_ORDER);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCaseManagementOrderParameters() {
        UUID hearingId = randomUUID();
        CaseData data = caseData.toBuilder()
            .lastHearingOrderDraftsHearingId(hearingId)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .startDate(LocalDateTime.now().plusMonths(6)).build()),
                element(hearingId, HearingBooking.builder()
                    .startDate(LocalDateTime.now().minusDays(3)).build())))
            .build();

        NotifyData expectedParameters = getExpectedCMOParameters(CMO.getLabel());
        NotifyData actualParameters = underTest.getNotifyDataForCMO(
            data, testDocument, CMO);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    private static CaseData createCase() {
        return CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .familyManCaseNumber("SACCCCCCCC5676576567")
            .orderCollection(createOrders(testDocument))
            .hearingDetails(createHearingBookings(LocalDateTime.now().plusMonths(3),
                LocalDateTime.now().plusMonths(3).plusHours(1)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Jones").build())
                .build()))
            .build();
    }
}
