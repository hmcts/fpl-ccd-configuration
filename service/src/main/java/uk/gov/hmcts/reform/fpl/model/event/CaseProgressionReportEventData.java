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
import uk.gov.hmcts.reform.fpl.model.CentralLondonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.EastLondonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WestLondonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BirminghamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.CoventryDFJCourts;
import uk.gov.hmcts.reform.fpl.model.DerbyDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LeicesterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LincolnDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthamptonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NottinghamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WolverhamptonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WorcesterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.StokeOnTrentDFJCourts;
import uk.gov.hmcts.reform.fpl.model.ClevelandAndSouthDurhamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.HumbersideDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthYorkshireDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthumbriaAndNorthDurhamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SouthYorkshireDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WestYorkshireDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BlackburnLancasterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.CarlisleDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LiverpoolDFJCourts;
import uk.gov.hmcts.reform.fpl.model.ManchesterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BrightonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.EssexAndSuffolkDFJCourts;
import uk.gov.hmcts.reform.fpl.model.GuildfordDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LutonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.MedwayDFJCourts;
import uk.gov.hmcts.reform.fpl.model.MiltonKeynesDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorwichDFJCourts;
import uk.gov.hmcts.reform.fpl.model.PeterboroughDFJCourts;
import uk.gov.hmcts.reform.fpl.model.ReadingDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WatfordDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BournemouthAndDorsetDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BristolDFJCourts;
import uk.gov.hmcts.reform.fpl.model.DevonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.PortsmouthDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SwindonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.TauntonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.TruroDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthWalesDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SouthEastWalesDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SwanseaDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LondonDFJ;
import uk.gov.hmcts.reform.fpl.model.MidlandsDFJ;
import uk.gov.hmcts.reform.fpl.model.NorthEastDFJ;
import uk.gov.hmcts.reform.fpl.model.NorthWestDFJ;
import uk.gov.hmcts.reform.fpl.model.SouthEastDFJ;
import uk.gov.hmcts.reform.fpl.model.SouthWestDFJ;
import uk.gov.hmcts.reform.fpl.model.WalesDFJ;
import uk.gov.hmcts.reform.fpl.model.NationalArea;

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
            typeParameterClass = CentralLondonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String centralLondonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "EastLondonDFJCourts",
            typeParameterClass = EastLondonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String eastLondonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WestLondonDFJCourts",
            typeParameterClass = WestLondonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String westLondonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BirminghamDFJCourts",
            typeParameterClass = BirminghamDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String birminghamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CoventryDFJCourts",
            typeParameterClass = CoventryDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String coventryDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DerbyDFJCourts",
            typeParameterClass = DerbyDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String derbyDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LeicesterDFJCourts",
            typeParameterClass = LeicesterDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String leicesterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LincolnDFJCourts",
            typeParameterClass = LincolnDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String lincolnDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthamptonDFJCourts",
            typeParameterClass = NorthamptonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northamptonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NottinghamDFJCourts",
            typeParameterClass = NottinghamDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String nottinghamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WolverhamptonDFJCourts",
            typeParameterClass = WolverhamptonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String wolverhamptonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WorcesterDFJCourts",
            typeParameterClass = WorcesterDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String worcesterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "Stoke-on-TrentDFJCourts",
            typeParameterClass = StokeOnTrentDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String stokeOnTrentrDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ClevelandAndSouthDurhamDFJCourts",
            typeParameterClass = ClevelandAndSouthDurhamDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String clevelandAndSouthDurhamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HumbersideDFJCourts",
            typeParameterClass = HumbersideDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String humbersideDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthYorkshireDFJCourts",
            typeParameterClass = NorthYorkshireDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northYorkshireDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthumbriaAndNorthDurhamDFJCourts",
            typeParameterClass = NorthumbriaAndNorthDurhamDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northumbriaAndNorthDurhamDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthYorkshireDFJCourts",
            typeParameterClass = SouthYorkshireDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southYorkshireDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WestYorkshireDFJCourts",
            typeParameterClass = WestYorkshireDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String westYorkshireDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BlackburnLancasterDFJCourts",
            typeParameterClass = BlackburnLancasterDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String blackburnLancasterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CarlisleDFJCourts",
            typeParameterClass = CarlisleDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String carlisleDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LiverpoolDFJCourts",
            typeParameterClass = LiverpoolDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String liverpoolDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ManchesterDFJCourts",
            typeParameterClass = ManchesterDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String manchesterDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BrightonDFJCourts",
            typeParameterClass = BrightonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String brightonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "EssexAndSuffolkDFJCourts",
            typeParameterClass = EssexAndSuffolkDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String essexAndSuffolkDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "GuildfordDFJCourts",
            typeParameterClass = GuildfordDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String guildfordDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "LutonDFJCourts",
            typeParameterClass = LutonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String lutonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MedwayDFJCourts",
            typeParameterClass = MedwayDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String medwayDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MiltonKeynesDFJCourts",
            typeParameterClass = MiltonKeynesDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String miltonKeynesDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorwichDFJCourts",
            typeParameterClass = NorwichDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String norwichDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PeterboroughDFJCourts",
            typeParameterClass = PeterboroughDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String peterboroughDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ReadingDFJCourts",
            typeParameterClass = ReadingDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String readingDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WatfordDFJCourts",
            typeParameterClass = WatfordDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String watfordDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BournemouthAndDorsetDFJCourts",
            typeParameterClass = BournemouthAndDorsetDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String bournemouthAndDorsetDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "BristolDFJCourts",
            typeParameterClass = BristolDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String bristolDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DevonDFJCourts",
            typeParameterClass = DevonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String devonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PortsmouthDFJCourts",
            typeParameterClass = PortsmouthDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String portsmouthDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SwindonDFJCourts",
            typeParameterClass = SwindonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String swindonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "TauntonDFJCourts",
            typeParameterClass = TauntonDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String tauntonDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "TruroDFJCourts",
            typeParameterClass = TruroDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String truroDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthWalesDFJCourts",
            typeParameterClass = NorthWalesDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northWalesDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthEastWalesDFJCourts",
            typeParameterClass = SouthEastWalesDFJCourts.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southEastWalesDFJCourts;
    @CCD(
            label = "Select court under DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SwanseaDFJCourts",
            typeParameterClass = SwanseaDFJCourts.class,
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
            typeParameterClass = LondonDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String londonDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MidlandsDFJ",
            typeParameterClass = MidlandsDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String midlandsDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthEastDFJ",
            typeParameterClass = NorthEastDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northEastDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NorthWestDFJ",
            typeParameterClass = NorthWestDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String northWestDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthEastDFJ",
            typeParameterClass = SouthEastDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southEastDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SouthWestDFJ",
            typeParameterClass = SouthWestDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String southWestDFJ;
    @CCD(
            label = "Select DFJ area",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WalesDFJ",
            typeParameterClass = WalesDFJ.class,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    @Temp
    private String walesDFJ;
    @CCD(
            label = "Select region?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "NationalArea",
            typeParameterClass = NationalArea.class,
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
