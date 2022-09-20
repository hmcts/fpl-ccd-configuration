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
    private String centralLondonDFJCourts;
    @Temp
    private String eastLondonDFJCourts;
    @Temp
    private String westLondonDFJCourts;
    @Temp
    private String birminghamDFJCourts;
    @Temp
    private String coventryDFJCourts;
    @Temp
    private String derbyDFJCourts;
    @Temp
    private String leicesterDFJCourts;
    @Temp
    private String lincolnDFJCourts;
    @Temp
    private String northamptonDFJCourts;
    @Temp
    private String nottinghamDFJCourts;
    @Temp
    private String wolverhamptonDFJCourts;
    @Temp
    private String worcesterDFJCourts;
    @Temp
    private String stokeOnTrentrDFJCourts;
    @Temp
    private String clevelandAndSouthDurhamDFJCourts;
    @Temp
    private String humbersideDFJCourts;
    @Temp
    private String northYorkshireDFJCourts;
    @Temp
    private String northumbriaAndNorthDurhamDFJCourts;
    @Temp
    private String southYorkshireDFJCourts;
    @Temp
    private String westYorkshireDFJCourts;
    @Temp
    private String blackburnLancasterDFJCourts;
    @Temp
    private String carlisleDFJCourts;
    @Temp
    private String liverpoolDFJCourts;
    @Temp
    private String manchesterDFJCourts;
    @Temp
    private String swanseaDFJCourts;
    @Temp
    private String reportType;
}
