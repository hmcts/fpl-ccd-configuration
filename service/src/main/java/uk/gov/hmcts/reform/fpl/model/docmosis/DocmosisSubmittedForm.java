package uk.gov.hmcts.reform.fpl.model.docmosis;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.DocmosisRisks;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
@Data
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
public class DocmosisSubmittedForm implements DocmosisData {
    private final List<DocmosisRespondent> respondents;
    private final List<DocmosisApplicant> applicants;
    private final List<DocmosisChild> children;
    private final List<DocmosisOtherParty> others;
    private final String applicantOrganisations;
    private final String respondentNames;
    private final String ordersNeeded;
    private final String directionsNeeded;
    private final Hearing hearing;
    private final Allocation allocation;
    private final HearingPreferences hearingPreferences;
    private final DocmosisInternationalElement internationalElement;
    private final String courtseal;
    private final String userFullName;
    private final String submittedDate;
    private final String groundsForEPOReason;
    private final String groundsThresholdReason;
    private final String thresholdDetails;
    private final DocmosisRisks risks;
    private final DocmosisFactorsParenting factorsParenting;
    private final Proceeding proceeding;
}
