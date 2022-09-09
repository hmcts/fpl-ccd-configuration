package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.Temp;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CMSReportEventData {
    @Temp
    private String carlisleDFJCourts;
    @Temp
    private String swanseaDFJCourts;
    @Temp
    private String centralLondonDFJCourts;
    @Temp
    private String reportType;
}
