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
public class CaseProgressionReportEventData {
    @Temp
    private String carlisleDFJCourts;
    @Temp
    private String swanseaDFJCourts;
    @Temp
    private String centralLondonDFJCourts;
    @Temp
    private String eastLondonDFJCourts;
    @Temp
    private String westLondonDFJCourts;
    @Temp
    private String birminghamDJFCourts;
    @Temp
    private String coventryDJFCourts;
    @Temp
    private String derbyDJFCourts;
    @Temp
    private String leicesterDJFCourts;
    @Temp
    private String lincolnDJFCourts;
    @Temp
    private String northamptonDJFCourts;
    @Temp
    private String nottinghamDJFCourts;
    @Temp
    private String wolverhampton;
    @Temp
    private String worcester;
    @Temp
    private String stokeOnTrent;
    @Temp
    private String reportType;
}
