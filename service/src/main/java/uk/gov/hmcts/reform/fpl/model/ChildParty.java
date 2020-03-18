package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ChildParty extends Party {
    private final String gender;
    private final String genderIdentification;
    private final String livingSituation;
    private final String livingSituationDetails;
    private final LocalDate addressChangeDate;
    private final LocalDate datePowersEnd;
    private final LocalDate careStartDate;
    private final LocalDate dischargeDate;
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

    @NotBlank(message = "Tell us the names of all children in the case")
    public String getFirstName() {
        return super.getFirstName();
    }

    @NotBlank(message = "Tell us the names of all children in the case")
    public String getLastName() {
        return super.getLastName();
    }

    @PastOrPresent(message = "Date of birth is in the future. You cannot send this application until that date")
    public LocalDate getDateOfBirth() {
        return super.getDateOfBirth();
    }

    @Builder(toBuilder = true)
    public ChildParty(String partyId,
                      PartyType partyType,
                      String firstName,
                      String lastName,
                      String organisationName,
                      LocalDate dateOfBirth,
                      Address address,
                      EmailAddress email,
                      Telephone telephoneNumber,
                      String gender,
                      String genderIdentification,
                      String livingSituation,
                      String livingSituationDetails,
                      LocalDate addressChangeDate,
                      LocalDate datePowersEnd,
                      LocalDate careStartDate,
                      LocalDate dischargeDate,
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
        this.datePowersEnd = datePowersEnd;
        this.careStartDate = careStartDate;
        this.dischargeDate = dischargeDate;
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
