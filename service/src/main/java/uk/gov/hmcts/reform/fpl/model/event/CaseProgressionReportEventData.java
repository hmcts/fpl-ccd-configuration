package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseProgressionReportType;
import uk.gov.hmcts.reform.fpl.model.Temp;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseProgressionReportEventData {
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CentralLondonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String centralLondonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "EastLondonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String eastLondonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WestLondonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String westLondonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BirminghamDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String birminghamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CoventryDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String coventryDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DerbyDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String derbyDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LeicesterDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String leicesterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LincolnDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String lincolnDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthamptonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northamptonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NottinghamDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String nottinghamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WolverhamptonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String wolverhamptonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WorcesterDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String worcesterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "Stoke-on-TrentDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String stokeOnTrentrDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ClevelandAndSouthDurhamDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String clevelandAndSouthDurhamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HumbersideDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String humbersideDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthYorkshireDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northYorkshireDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthumbriaAndNorthDurhamDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northumbriaAndNorthDurhamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthYorkshireDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southYorkshireDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WestYorkshireDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String westYorkshireDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BlackburnLancasterDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String blackburnLancasterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CarlisleDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String carlisleDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LiverpoolDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String liverpoolDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ManchesterDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String manchesterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BrightonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String brightonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "EssexAndSuffolkDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String essexAndSuffolkDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "GuildfordDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String guildfordDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LutonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String lutonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MedwayDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String medwayDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MiltonKeynesDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String miltonKeynesDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorwichDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String norwichDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PeterboroughDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String peterboroughDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ReadingDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String readingDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WatfordDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String watfordDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BournemouthAndDorsetDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String bournemouthAndDorsetDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BristolDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String bristolDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DevonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String devonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PortsmouthDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String portsmouthDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SwindonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String swindonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "TauntonDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String tauntonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "TruroDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String truroDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthWalesDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northWalesDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthEastWalesDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southEastWalesDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SwanseaDFJCourts",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String swanseaDFJCourts;
    @CCD(
            label = "Select report type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ReportType",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private CaseProgressionReportType reportType;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LondonDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String londonDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MidlandsDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String midlandsDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthEastDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northEastDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthWestDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northWestDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthEastDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southEastDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthWestDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southWestDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WalesDFJ",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String walesDFJ;
    @CCD(
            label = "Select region?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NationalArea",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String nationalArea;

    @JsonIgnore
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
