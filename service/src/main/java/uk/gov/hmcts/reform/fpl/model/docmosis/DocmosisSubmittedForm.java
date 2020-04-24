package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.DocmosisAnnexDocuments;
import uk.gov.hmcts.reform.fpl.model.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.DocmosisProceeding;
import uk.gov.hmcts.reform.fpl.model.DocmosisRisks;

import java.util.List;

@Data
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
public class DocmosisSubmittedForm implements DocmosisData {
    private final List<DocmosisRespondent> respondents;
    private final List<DocmosisApplicant> applicants;
    private final List<DocmosisChildren> children;
    private final List<DocmosisOtherParty> others;
    private final String applicantOrganisations;
    private final String respondentNames;
    private final String ordersNeeded;
    private final String directionsNeeded;
    private final DocmosisHearing hearing;
    private final Allocation allocation;
    private final DocmosisHearingPreferences hearingPreferences;
    private final DocmosisInternationalElement internationalElement;
    private final String courtseal;
    private final String draftWaterMark;
    private final String userFullName;
    private final String submittedDate;
    private final String groundsForEPOReason;
    private final String groundsThresholdReason;
    private final String thresholdDetails;
    private final DocmosisRisks risks;
    private final DocmosisFactorsParenting factorsParenting;
    private final List<DocmosisProceeding> proceeding;
    private final DocmosisAnnexDocuments annexDocuments;
}
