package uk.gov.hmcts.reform.fpl.model.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfChangeAnswers {
    private final String respondentFirstName;
    private final String respondentLastName;
    private final String applicantName;
}
