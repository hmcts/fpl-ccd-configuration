package uk.gov.hmcts.reform.fpl.model.summary;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserRAccess;

@Value
@Builder
@Jacksonized
public class SyntheticCaseSummary {
    @CCD(label = "Flag added by", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryFlagAddedByFullName;
    @CCD(label = "Email", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryFlagAddedByEmail;
    @CCD(
            label = "Assessment Form",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    DocumentReference caseSummaryFlagAssessmentForm;
    @CCD(label = "Additional notes", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryCaseFlagNotes;

    @CCD(
            label = "Does this case require a welsh flag",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryLanguageRequirement;
    @CCD(
            label = "Does this case require a welsh flag",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryLALanguageRequirement;

    @CCD(label = "Orders requested", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryOrdersRequested;

    @CCD(label = "Date of issue", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    LocalDate caseSummaryDateOfIssue;
    @CCD(label = "26-week timeline date", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    LocalDate deadline26week;

    @CCD(
            label = "There are some unresolved messages",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryHasUnresolvedMessages;

    @CCD(label = "Court to issue", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryCourtName;
    @CCD(label = "Previous Court", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryPreviousCourtName;
    @CCD(label = "Is a High Court case", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryHighCourtCase;
    @CCD(label = "Is a High Court case", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryLAHighCourtCase;

    @CCD(
            label = "There is a next hearing",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryHasNextHearing;
    @CCD(label = "Type", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryNextHearingType;
    @CCD(
            label = "Date",
            access = {CaseworkerPubliclawSystemupdateCudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    LocalDate caseSummaryNextHearingDate;
    @CCD(
            label = "Date",
            access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSuperuserRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    LocalDateTime caseSummaryNextHearingDateTime;
    @CCD(
            label = "Next hearing details",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
    )
    NextHearingDetails nextHearingDetails;

    @CCD(label = "Judge", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryNextHearingJudge;
    @CCD(label = "Email address", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryNextHearingEmailAddress;
    @CCD(
            label = "Draft CMO",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    DocumentReference caseSummaryNextHearingCMO;

    @CCD(
            label = "There is a next hearing",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryHasPreviousHearing;
    @CCD(label = "Type", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryPreviousHearingType;
    @CCD(label = "Date", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    LocalDate caseSummaryPreviousHearingDate;
    @CCD(
            label = "CMO",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    DocumentReference caseSummaryPreviousHearingCMO;

    @CCD(
            label = "There is a next hearing",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryHasFinalHearing;
    @CCD(label = "Date", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    LocalDate caseSummaryFinalHearingDate;

    @CCD(
            label = "Allocated judge or magistrate",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryAllocatedJudgeName;
    @CCD(label = "Email address", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryAllocatedJudgeEmail;


    @CCD(label = "Number of children", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    Integer caseSummaryNumberOfChildren;
    @CCD(label = "Main contact", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryLASolicitorName;
    @CCD(label = "Email address", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryLASolicitorEmail;
    @CCD(
            label = "First respondent's last name",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryFirstRespondentLastName;
    @CCD(
            label = "First respondent's legal representative",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryFirstRespondentLegalRep;
    @CCD(label = "Cafcass guardian", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    String caseSummaryCafcassGuardian;

    @CCD(
            label = "Does this case hide the summary LA tab",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    String caseSummaryLATabHidden;

    public static SyntheticCaseSummary emptySummary() {
        return SyntheticCaseSummary.builder().build();
    }

}
