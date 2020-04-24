package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, CafcassEmailContentProvider.class, LookupTestConfig.class
})
class CafcassEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(cafcassEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

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
        cafcassSubmissionTemplate.setCaseUrl(buildCaseUrl(CASE_REFERENCE));

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(cafcassSubmissionTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() {
        SubmitCaseCafcassTemplate cafcassSubmissionTemplate = new SubmitCaseCafcassTemplate();
        cafcassSubmissionTemplate.setCafcass(CAFCASS_NAME);
        cafcassSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        cafcassSubmissionTemplate.setDataPresent(NO.getValue());
        cafcassSubmissionTemplate.setFullStop(YES.getValue());
        cafcassSubmissionTemplate.setOrdersAndDirections(List.of(""));
        cafcassSubmissionTemplate.setTimeFramePresent(NO.getValue());
        cafcassSubmissionTemplate.setTimeFrameValue("");
        cafcassSubmissionTemplate.setUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setFirstRespondentName("");
        cafcassSubmissionTemplate.setReference("123");
        cafcassSubmissionTemplate.setCaseUrl(buildCaseUrl("123"));

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(emptyCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(cafcassSubmissionTemplate);
    }
}
