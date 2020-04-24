package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
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
    JacksonAutoConfiguration.class, HmctsEmailContentProvider.class, LookupTestConfig.class
})
class HmctsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(hmctsEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

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
        hmctsSubmissionTemplate.setCaseUrl(buildCaseUrl(CASE_REFERENCE));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(hmctsSubmissionTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() {
        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = new SubmitCaseHmctsTemplate();
        hmctsSubmissionTemplate.setCourt(COURT_NAME);
        hmctsSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        hmctsSubmissionTemplate.setDataPresent(NO.getValue());
        hmctsSubmissionTemplate.setFullStop(YES.getValue());
        hmctsSubmissionTemplate.setOrdersAndDirections(List.of(""));
        hmctsSubmissionTemplate.setTimeFramePresent(NO.getValue());
        hmctsSubmissionTemplate.setTimeFrameValue("");
        hmctsSubmissionTemplate.setUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setFirstRespondentName("");
        hmctsSubmissionTemplate.setReference("123");
        hmctsSubmissionTemplate.setCaseUrl(buildCaseUrl("123"));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(emptyCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(hmctsSubmissionTemplate);
    }
}
