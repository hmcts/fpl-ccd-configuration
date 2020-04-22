package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.AssertionHelper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest.BASE_URL;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCaseUrlParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, OrderIssuedEmailContentProvider.class, LookupTestConfig.class
})
@TestPropertySource(properties = {"ccd.ui.base.url=" + BASE_URL})
class OrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    private static final Long CASE_ID = 12345L;
    private static final byte[] documentContents = {1, 2, 3, 4, 5};

    @Test
    void shouldBuildGeneratedOrderParametersWithCaseUrl() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true);

        AssertionHelper.assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildGeneratedOrderParametersWithoutCaseUrl() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER);
        Map<String, Object> expectedParameters = getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true);

        AssertionHelper.assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildNoticeOfPlacementOrderParameters() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, NOTICE_OF_PLACEMENT_ORDER);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(NOTICE_OF_PLACEMENT_ORDER.getLabel(),
            false);

        AssertionHelper.assertEquals(actualParameters, expectedParameters);
    }

    @Test
    void shouldBuildCaseManagementOrderParameters() {
        Map<String, Object> actualParameters = orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
            createCase(), LOCAL_AUTHORITY_CODE, documentContents, CMO);
        Map<String, Object> expectedParameters = getExpectedCaseUrlParameters(CMO.getLabel(), true);

        AssertionHelper.assertEquals(actualParameters, expectedParameters);
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
            .id(CASE_ID)
            .build();
    }
}
