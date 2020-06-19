package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {CafcassEmailContentProvider.class, LookupTestConfig.class})
class CafcassEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");

        SubmitCaseCafcassTemplate cafcassSubmissionTemplate = new SubmitCaseCafcassTemplate();
        cafcassSubmissionTemplate.setCafcass(CAFCASS_NAME);
        cafcassSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        cafcassSubmissionTemplate.setDataPresent(YES.getValue());
        cafcassSubmissionTemplate.setFullStop(NO.getValue());
        cafcassSubmissionTemplate.setOrdersAndDirections(ordersAndDirections);
        cafcassSubmissionTemplate.setTimeFramePresent(YES.getValue());
        cafcassSubmissionTemplate.setTimeFrameValue("same day");
        cafcassSubmissionTemplate.setUrgentHearing(YES.getValue());
        cafcassSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setFirstRespondentName("Smith");
        cafcassSubmissionTemplate.setReference(CASE_REFERENCE);
        cafcassSubmissionTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(cafcassSubmissionTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithIncompleteCaseDetails() {
        SubmitCaseCafcassTemplate cafcassSubmissionTemplate = new SubmitCaseCafcassTemplate();
        cafcassSubmissionTemplate.setCafcass(CAFCASS_NAME);
        cafcassSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        cafcassSubmissionTemplate.setDataPresent(YES.getValue());
        cafcassSubmissionTemplate.setFullStop(NO.getValue());
        cafcassSubmissionTemplate.setOrdersAndDirections(List.of("Care order"));
        cafcassSubmissionTemplate.setTimeFramePresent(NO.getValue());
        cafcassSubmissionTemplate.setTimeFrameValue("");
        cafcassSubmissionTemplate.setUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setFirstRespondentName("");
        cafcassSubmissionTemplate.setReference("123");
        cafcassSubmissionTemplate.setCaseUrl(caseUrl("123"));

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(buildCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(cafcassSubmissionTemplate);
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
