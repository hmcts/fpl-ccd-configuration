package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.Temp;

import static uk.gov.hmcts.reform.fpl.enums.JudgeType.FEE_PAID_JUDGE;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.model.FeePaidJudgeTitle;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AllocateJudgeEventData {
    @CCD(
            label = "What type of judge do you want to allocate:",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    @Temp
    private final JudgeType judgeType;
    @CCD(
            label = "Select judge title",
            searchable = false,
            typeParameterOverride = "FeePaidJudgeTitle",
            typeParameterClass = FeePaidJudgeTitle.class,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    @Temp
    private final JudgeOrMagistrateTitle feePaidJudgeTitle;
    @CCD(
            label = "Search for Judge",
            searchable = false,
            typeOverride = FieldType.JudicialUser,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    @Temp
    private final JudicialUser judicialUser;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
    @Temp
    private final Judge manualJudgeDetails;

    public JudgeOrMagistrateTitle getFeePaidJudgeTitle() {
        return (FEE_PAID_JUDGE.equals(judgeType)) ? feePaidJudgeTitle : null;
    }
}
