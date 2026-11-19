package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllPartyFlags {
    private CaseFlagsType caseFlags;
    private CaseFlagsType applicantFlags;
    private CaseFlagsType applicantExternalFlags;
    private CaseFlagsType applicantRepresentativeFlags;
    private CaseFlagsType applicantRepresentativeExternalFlags;
    private CaseFlagsType respondent1Flags;
    private CaseFlagsType respondent1ExternalFlags;
    private CaseFlagsType respondent2Flags;
    private CaseFlagsType respondent2ExternalFlags;
    private CaseFlagsType respondent3Flags;
    private CaseFlagsType respondent3ExternalFlags;
    private CaseFlagsType respondent4Flags;
    private CaseFlagsType respondent4ExternalFlags;
    private CaseFlagsType respondent5Flags;
    private CaseFlagsType respondent5ExternalFlags;
    private CaseFlagsType respondent6Flags;
    private CaseFlagsType respondent6ExternalFlags;
    private CaseFlagsType respondent7Flags;
    private CaseFlagsType respondent7ExternalFlags;
    private CaseFlagsType respondent8Flags;
    private CaseFlagsType respondent8ExternalFlags;
    private CaseFlagsType respondent9Flags;
    private CaseFlagsType respondent9ExternalFlags;
    private CaseFlagsType respondent10Flags;
    private CaseFlagsType respondent10ExternalFlags;
    private CaseFlagsType representative1Flags;
    private CaseFlagsType representative1ExternalFlags;
    private CaseFlagsType representative2Flags;
    private CaseFlagsType representative2ExternalFlags;
    private CaseFlagsType representative3Flags;
    private CaseFlagsType representative3ExternalFlags;
    private CaseFlagsType representative4Flags;
    private CaseFlagsType representative4ExternalFlags;
    private CaseFlagsType representative5Flags;
    private CaseFlagsType representative5ExternalFlags;
    private CaseFlagsType representative6Flags;
    private CaseFlagsType representative6ExternalFlags;
    private CaseFlagsType representative7Flags;
    private CaseFlagsType representative7ExternalFlags;
    private CaseFlagsType representative8Flags;
    private CaseFlagsType representative8ExternalFlags;
    private CaseFlagsType representative9Flags;
    private CaseFlagsType representative9ExternalFlags;
    private CaseFlagsType representative10Flags;
    private CaseFlagsType representative10ExternalFlags;
}
