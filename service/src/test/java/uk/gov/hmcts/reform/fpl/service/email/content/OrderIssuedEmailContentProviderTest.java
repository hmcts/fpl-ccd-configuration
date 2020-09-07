package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedAllocatedJudgeParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCaseUrlParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ContextConfiguration(classes = {OrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class, FixedTimeConfiguration.class, CaseConverter.class})
class OrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] documentContents = TestDataHelper.DOCUMENT_CONTENT;
    private static final CaseData caseData = createCase();

    @MockBean
    private GeneratedOrderService generatedOrderService;

    @Autowired
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @Test
    void shouldBuildGeneratedOrderParametersWithCaseUrl() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            caseData, documentContents, GENERATED_ORDER);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersWithoutCaseUrl() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
            caseData, documentContents, GENERATED_ORDER);
        Map<String, Object> expectedParameters = getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildNoticeOfPlacementOrderParameters() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            caseData, documentContents, NOTICE_OF_PLACEMENT_ORDER);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(NOTICE_OF_PLACEMENT_ORDER.getLabel(),
            false);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildCaseManagementOrderParameters() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            caseData, documentContents, CMO);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(CMO.getLabel(), true);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersForAllocatedJudge() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeLastName("Scott")
            .judgeTitle(DEPUTY_DISTRICT_JUDGE)
            .build();

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        AllocatedJudgeTemplateForGeneratedOrder actualParameters = orderIssuedEmailContentProvider
            .buildAllocatedJudgeOrderIssuedNotification(caseData);

        AllocatedJudgeTemplateForGeneratedOrder expectedParameters = getExpectedAllocatedJudgeParameters();

        assertThat(actualParameters).isEqualToComparingFieldByField(expectedParameters);
    }

    private static CaseData createCase() {
        return CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .familyManCaseNumber("SACCCCCCCC5676576567")
            .orderCollection(createOrders(DocumentReference.builder().build()))
            .hearingDetails(createHearingBookings(LocalDateTime.now().plusMonths(3),
                LocalDateTime.now().plusMonths(3).plusHours(1)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Jones").build())
                .build()))
            .build();
    }
}
