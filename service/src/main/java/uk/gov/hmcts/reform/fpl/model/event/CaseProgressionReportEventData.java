package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseProgressionReportType;
import uk.gov.hmcts.reform.fpl.model.Temp;

import java.util.ArrayList;
import java.util.List;

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
    private String brightonDFJCourts;
    @Temp
    private String essexAndSuffolkDFJCourts;
    @Temp
    private String guildfordDFJCourts;
    @Temp
    private String lutonDFJCourts;
    @Temp
    private String medwayDFJCourts;
    @Temp
    private String miltonKeynesDFJCourts;
    @Temp
    private String norwichDFJCourts;
    @Temp
    private String peterboroughDFJCourts;
    @Temp
    private String readingDFJCourts;
    @Temp
    private String watfordDFJCourts;
    @Temp
    private String bournemouthAndDorsetDFJCourts;
    @Temp
    private String bristolDFJCourts;
    @Temp
    private String devonDFJCourts;
    @Temp
    private String portsmouthDFJCourts;
    @Temp
    private String swindonDFJCourts;
    @Temp
    private String tauntonDFJCourts;
    @Temp
    private String truroDFJCourts;
    @Temp
    private String northWalesDFJCourts;
    @Temp
    private String southEastWalesDFJCourts;
    @Temp
    private String swanseaDFJCourts;
    @Temp
    private CaseProgressionReportType reportType;
    @Temp
    private String londonDFJ;
    @Temp
    private String midlandsDFJ;
    @Temp
    private String northEastDFJ;
    @Temp
    private String northWestDFJ;
    @Temp
    private String southEastDFJ;
    @Temp
    private String southWestDFJ;
    @Temp
    private String walesDFJ;
    @Temp
    private String nationalArea;

    public List<String> getDFJCourts() {
        ArrayList<String> dfgCourts = new ArrayList<>();
        dfgCourts.add(centralLondonDFJCourts);
        dfgCourts.add(eastLondonDFJCourts);
        dfgCourts.add(westLondonDFJCourts);
        dfgCourts.add(birminghamDFJCourts);
        dfgCourts.add(coventryDFJCourts);
        dfgCourts.add(derbyDFJCourts);
        dfgCourts.add(leicesterDFJCourts);
        dfgCourts.add(lincolnDFJCourts);
        dfgCourts.add(northamptonDFJCourts);
        dfgCourts.add(nottinghamDFJCourts);
        dfgCourts.add(wolverhamptonDFJCourts);
        dfgCourts.add(worcesterDFJCourts);
        dfgCourts.add(stokeOnTrentrDFJCourts);
        dfgCourts.add(clevelandAndSouthDurhamDFJCourts);
        dfgCourts.add(humbersideDFJCourts);
        dfgCourts.add(northYorkshireDFJCourts);
        dfgCourts.add(northumbriaAndNorthDurhamDFJCourts);
        dfgCourts.add(southYorkshireDFJCourts);
        dfgCourts.add(westYorkshireDFJCourts);
        dfgCourts.add(blackburnLancasterDFJCourts);
        dfgCourts.add(carlisleDFJCourts);
        dfgCourts.add(liverpoolDFJCourts);
        dfgCourts.add(manchesterDFJCourts);
        dfgCourts.add(brightonDFJCourts);
        dfgCourts.add(essexAndSuffolkDFJCourts);
        dfgCourts.add(guildfordDFJCourts);
        dfgCourts.add(lutonDFJCourts);
        dfgCourts.add(medwayDFJCourts);
        dfgCourts.add(miltonKeynesDFJCourts);
        dfgCourts.add(norwichDFJCourts);
        dfgCourts.add(peterboroughDFJCourts);
        dfgCourts.add(readingDFJCourts);
        dfgCourts.add(watfordDFJCourts);
        dfgCourts.add(bournemouthAndDorsetDFJCourts);
        dfgCourts.add(bristolDFJCourts);
        dfgCourts.add(devonDFJCourts);
        dfgCourts.add(portsmouthDFJCourts);
        dfgCourts.add(swindonDFJCourts);
        dfgCourts.add(tauntonDFJCourts);
        dfgCourts.add(truroDFJCourts);
        dfgCourts.add(northWalesDFJCourts);
        dfgCourts.add(southEastWalesDFJCourts);
        dfgCourts.add(swanseaDFJCourts);
        
        return dfgCourts;
    }
}
