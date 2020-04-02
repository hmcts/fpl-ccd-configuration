package uk.gov.hmcts.reform.fpl.model;

import static org.apache.commons.lang3.StringUtils.defaultString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasGender;

import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.groups.Default;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@HasGender(groups = {Default.class, SealedSDOGroup.class})
public final class ChildParty extends Party {

    public final String firstName;
    public final String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public final LocalDate dateOfBirth;
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
        return firstName;
    }

    @NotBlank(message = "Tell us the names of all children in the case")
    public String getLastName() {
        return lastName;
    }

    @NotNull(message = "Tell us the date of birth of all children in the case",
        groups = {Default.class, SealedSDOGroup.class})
    @PastOrPresent(message = "Date of birth is in the future. You cannot send this application until that date")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Builder(toBuilder = true)
    public ChildParty(String partyId,
        PartyType partyType,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Address address,
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
        super(partyId, partyType, address);
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
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

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", defaultString(firstName), defaultString(lastName)).trim();
    }
}
