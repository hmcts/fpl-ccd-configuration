package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCaseUrlParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ContextConfiguration(classes = {OrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class, HearingBookingService.class, FixedTimeConfiguration.class,
})
class OrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] documentContents = {1, 2, 3, 4, 5};

    @MockBean
    private GeneratedOrderService generatedOrderService;

    @Autowired
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldBuildGeneratedOrderParametersWithCaseUrl() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersWithoutCaseUrl() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER);
        Map<String, Object> expectedParameters = getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildNoticeOfPlacementOrderParameters() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, NOTICE_OF_PLACEMENT_ORDER);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(NOTICE_OF_PLACEMENT_ORDER.getLabel(),
            false);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildCaseManagementOrderParameters() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, CMO);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(CMO.getLabel(), true);

        assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersForAllocatedJudge() {
        CaseDetails caseDetails = createCase();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeLastName("Scott")
            .judgeTitle(DEPUTY_DISTRICT_JUDGE)
            .build();

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        Map<String, Object> actualParameters = orderIssuedEmailContentProvider
            .buildAllocatedJudgeOrderIssuedNotification(caseDetails);

        Map<String, Object> expectedParameters = getExpectedAllocatedJudgeParameters();

        assertEquals(actualParameters, expectedParameters);
    }

    private Map<String, Object> getExpectedAllocatedJudgeParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("orderType", "blank order (c21)")
            .put("callout", "^Jones, SACCCCCCCC5676576567, hearing 27 Aug 2020")
            .put("respondentLastName", "Jones")
            .put("judgeTitle", "Deputy District Judge")
            .put("judgeName", "Scott")
            .put("caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345")
            .build();
    }

    private static CaseDetails createCase() {
        final Map<String, Object> data = new HashMap<>();
        data.put("familyManCaseNumber", "SACCCCCCCC5676576567");
        data.put("orderCollection", createOrders(DocumentReference.builder().build()));
        data.put("hearingDetails",
            createHearingBookings(LocalDateTime.now().plusMonths(3), LocalDateTime.now().plusMonths(3).plusHours(1)));
        data.put("respondents1", wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Jones").build())
            .build()));

        return CaseDetails.builder()
            .data(data)
            .id(Long.valueOf(CASE_REFERENCE))
            .build();
    }
}
