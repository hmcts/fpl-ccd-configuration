package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasChildName;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@HasChildName
public final class ChildParty extends Party {
    private final String gender;
    private final String genderIdentification;
    private final String livingSituation;
    private final String livingSituationDetails;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final Date addressChangeDate;
    private final String keyDates;
    private final String careAndContactPlan;
    private final String adoption;
    private final String placementOrderApplication;
    private final String placementCourt;
    private final String mothersName;
    private final String fathersName;
    private final String fathersResponsibility;
    private final String socialWorkerName;
    private final Telephone socialWorkerTelephoneNumber;
    private final String additionalNeeds;
    private final String additionalNeedsDetails;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @Builder(toBuilder = true)
    public ChildParty(String partyId,
                      PartyType partyType,
                      String firstName,
                      String lastName,
                      String organisationName,
                      Date dateOfBirth,
                      Address address,
                      EmailAddress email,
                      Telephone telephoneNumber,
                      String gender,
                      String genderIdentification,
                      String livingSituation,
                      String livingSituationDetails,
                      Date addressChangeDate,
                      String keyDates,
                      String careAndContactPlan,
                      String adoption,
                      String placementOrderApplication,
                      String placementCourt,
                      String mothersName,
                      String fathersName,
                      String fathersResponsibility,
                      String socialWorkerName,
                      Telephone socialWorkerTelephoneNumber,
                      String additionalNeeds,
                      String additionalNeedsDetails,
                      String detailsHidden,
                      String detailsHiddenReason,
                      String litigationIssues,
                      String litigationIssuesDetails) {
        super(partyId, partyType, firstName, lastName, organisationName,
            dateOfBirth, address, email, telephoneNumber);
        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.livingSituation = livingSituation;
        this.livingSituationDetails = livingSituationDetails;
        this.addressChangeDate = addressChangeDate;
        this.keyDates = keyDates;
        this.careAndContactPlan = careAndContactPlan;
        this.adoption = adoption;
        this.placementOrderApplication = placementOrderApplication;
        this.placementCourt = placementCourt;
        this.mothersName = mothersName;
        this.fathersName = fathersName;
        this.fathersResponsibility = fathersResponsibility;
        this.socialWorkerName = socialWorkerName;
        this.socialWorkerTelephoneNumber = socialWorkerTelephoneNumber;
        this.additionalNeeds = additionalNeeds;
        this.additionalNeedsDetails = additionalNeedsDetails;
        this.detailsHidden = detailsHidden;
        this.detailsHiddenReason = detailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
    }
}
