package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.TelephoneNumber;

import java.util.Date;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public final class ChildParty extends Party {
    private final String partyID;
    private final String idamID;
    private final String partyType;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String organisationName;
    private final Date dateOfBirth;
    private final Address address;
    private final EmailAddress email;
    private final TelephoneNumber telephoneNumber;
    private final String gender;
    private final String genderIdentification;
    private final String livingSituation;
    private final String situationDetails;
    private final Date situationDate;
    private final String keyDates;
    private final String careAndContact;
    private final String adoption;
    private final String placementOrderApplication;
    private final String placementCourt;
    private final String mothersName;
    private final String fathersName;
    private final String fathersResponsibility;
    private final String socialWorkerName;
    private final String socialWorkerTel;
    private final String additionalNeeds;
    private final String additionalNeedsDetails;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @JsonCreator
    private ChildParty(@JsonProperty("partyID") String partyID,
                       @JsonProperty("idamID") String idamID,
                       @JsonProperty("partyType") String partyType,
                       @JsonProperty("title") String title,
                       @JsonProperty("firstName") String firstName,
                       @JsonProperty("lastName") String lastName,
                       @JsonProperty("organisationName") String organisationName,
                       @JsonProperty("dateOfBirth") Date dateOfBirth,
                       @JsonProperty("address") Address address,
                       @JsonProperty("email") EmailAddress email,
                       @JsonProperty("telephoneNumber") TelephoneNumber telephoneNumber,
                       @JsonProperty("gender") String gender,
                       @JsonProperty("genderIdentification") String genderIdentification,
                       @JsonProperty("livingSituation") String livingSituation,
                       @JsonProperty("situationDetails") String situationDetails,
                       @JsonProperty("situationDate") Date situationDate,
                       @JsonProperty("keyDates") String keyDates,
                       @JsonProperty("careAndContact") String careAndContact,
                       @JsonProperty("adoption") String adoption,
                       @JsonProperty("placementOrderApplication") String placementOrderApplication,
                       @JsonProperty("placementCourt") String placementCourt,
                       @JsonProperty("mothersName") String mothersName,
                       @JsonProperty("fathersName") String fathersName,
                       @JsonProperty("fathersResponsibility") String fathersResponsibility,
                       @JsonProperty("socialWorkerName") String socialWorkerName,
                       @JsonProperty("socialWorkTel") String socialWorkerTel,
                       @JsonProperty("additionalNeeds") String additionalNeeds,
                       @JsonProperty("additionalNeedsDetails") String additionalNeedsDetails,
                       @JsonProperty("detailsHidden") String detailsHidden,
                       @JsonProperty("detailsHiddenReason") String detailsHiddenReason,
                       @JsonProperty("litigationIssues") String litigationIssues,
                       @JsonProperty("litigationIssuesDetails") String litigationIssuesDetails) {
        super(partyID, idamID, partyType, title, firstName, lastName, organisationName, dateOfBirth, address,
            email, telephoneNumber);
        this.partyID = partyID;
        this.idamID = idamID;
        this.partyType = partyType;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organisationName = organisationName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.email = email;
        this.telephoneNumber = telephoneNumber;
        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.livingSituation = livingSituation;
        this.situationDetails = situationDetails;
        this.situationDate = situationDate;
        this.keyDates = keyDates;
        this.careAndContact = careAndContact;
        this.adoption = adoption;
        this.placementOrderApplication = placementOrderApplication;
        this.placementCourt = placementCourt;
        this.mothersName = mothersName;
        this.fathersName = fathersName;
        this.fathersResponsibility = fathersResponsibility;
        this.socialWorkerName = socialWorkerName;
        this.socialWorkerTel = socialWorkerTel;
        this.additionalNeeds = additionalNeeds;
        this.additionalNeedsDetails = additionalNeedsDetails;
        this.detailsHidden = detailsHidden;
        this.detailsHiddenReason = detailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
    }
}
