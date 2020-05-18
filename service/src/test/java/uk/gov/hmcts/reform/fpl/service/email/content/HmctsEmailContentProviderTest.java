package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {HmctsEmailContentProvider.class, LookupTestConfig.class})
class HmctsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");

        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = new SubmitCaseHmctsTemplate();
        hmctsSubmissionTemplate.setCourt(COURT_NAME);
        hmctsSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        hmctsSubmissionTemplate.setDataPresent(YES.getValue());
        hmctsSubmissionTemplate.setFullStop(NO.getValue());
        hmctsSubmissionTemplate.setOrdersAndDirections(ordersAndDirections);
        hmctsSubmissionTemplate.setTimeFramePresent(YES.getValue());
        hmctsSubmissionTemplate.setTimeFrameValue("same day");
        hmctsSubmissionTemplate.setUrgentHearing(YES.getValue());
        hmctsSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setFirstRespondentName("Smith");
        hmctsSubmissionTemplate.setReference(CASE_REFERENCE);
        hmctsSubmissionTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(hmctsSubmissionTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithIncompleteCaseDetails() {
        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = new SubmitCaseHmctsTemplate();

        hmctsSubmissionTemplate.setCourt(COURT_NAME);
        hmctsSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        hmctsSubmissionTemplate.setDataPresent(YES.getValue());
        hmctsSubmissionTemplate.setFullStop(NO.getValue());
        hmctsSubmissionTemplate.setOrdersAndDirections(List.of("Care order"));
        hmctsSubmissionTemplate.setTimeFramePresent(NO.getValue());
        hmctsSubmissionTemplate.setTimeFrameValue("");
        hmctsSubmissionTemplate.setUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setFirstRespondentName("");
        hmctsSubmissionTemplate.setReference("123");
        hmctsSubmissionTemplate.setCaseUrl(caseUrl("123"));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(buildCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(hmctsSubmissionTemplate);
    }

    private CaseDetails buildCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .data(ImmutableMap.of(
                "orders", Orders.builder()
                    .orderType(List.of(CARE_ORDER))
                    .build()))
            .build();
    }
}
