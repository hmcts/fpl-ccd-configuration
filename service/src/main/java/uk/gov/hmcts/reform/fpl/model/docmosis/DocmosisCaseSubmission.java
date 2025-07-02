package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisCaseSubmission implements DocmosisData {
    private final List<DocmosisRespondent> respondents;
    private final List<DocmosisApplicant> applicants;
    private final List<DocmosisChild> children;
    private final List<DocmosisOtherParty> others;
    private final String welshLanguageRequirement;
    private final String courtName;
    private final String applicantOrganisations;
    private final String respondentNames;
    private final String ordersNeeded;
    private final String directionsNeeded;
    private final DocmosisHearing hearing;
    private final DocmosisAllocation allocation;
    private final DocmosisHearingPreferences hearingPreferences;
    private final DocmosisInternationalElement internationalElement;
    private final String userFullName;
    private final String submittedDate;
    private final String groundsForEPOReason;
    private final String groundsForChildAssessmentOrderReason;
    private final String groundsThresholdReason;
    private final String thresholdDetails;
    private final DocmosisRisks risks;
    private final DocmosisFactorsParenting factorsParenting;
    private final List<DocmosisProceeding> proceeding;
    private final DocmosisAnnexDocuments annexDocuments;
    private final String relevantProceedings;
    private final boolean dischargeOfOrder;

    private String caseNumber;
    private String courtSeal;
    private String draftWaterMark;
}
