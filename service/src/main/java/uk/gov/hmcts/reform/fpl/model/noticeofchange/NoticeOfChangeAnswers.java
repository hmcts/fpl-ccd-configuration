package uk.gov.hmcts.reform.fpl.model.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfChangeAnswers {
    private final int policyReference;
    private final String respondentFirstName;
    private final String respondentLastName;
    private final LocalDate respondentDOB;
    private final String applicantName;
}
