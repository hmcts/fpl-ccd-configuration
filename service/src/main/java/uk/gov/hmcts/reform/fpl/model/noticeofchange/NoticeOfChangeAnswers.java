package uk.gov.hmcts.reform.fpl.model.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfChangeAnswers {
    @CCD(label = " ")
    private final String respondentFirstName;
    @CCD(label = " ")
    private final String respondentLastName;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ")
  private String applicantName;
  // ==== end synthesised definition-only fields ====
}
