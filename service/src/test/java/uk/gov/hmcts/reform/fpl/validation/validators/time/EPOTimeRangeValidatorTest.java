package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class EPOTimeRangeValidatorTest extends TimeValidatorTest {

    @Autowired
    private Time time;

    @SpyBean
    private ValidateGroupService validateGroupService;

    @Test
    void shouldReturnAnErrorWhenDateTimeExceedsRange() {
        CaseData caseData = CaseData.builder()
            .dateAndTimeOfIssue(time.now())
            .epoEndDate(time.now().plusDays(9))
            .build();
        List<String> errorMessages = validateGroupService.validateGroup(caseData, EPOEndDateGroup.class);
        assertThat(errorMessages).containsExactly("Date must be within 8 days of the order date");
    }

    @Test
    void shouldNotReturnAnErrorWhenDateTimeDoesNotExceedRange() {
        CaseData caseData = CaseData.builder()
            .dateAndTimeOfIssue(time.now())
            .epoEndDate(time.now().plusDays(4))
            .build();
        List<String> errorMessages = validateGroupService.validateGroup(caseData, EPOEndDateGroup.class);
        assertThat(errorMessages).isEmpty();
    }
}
