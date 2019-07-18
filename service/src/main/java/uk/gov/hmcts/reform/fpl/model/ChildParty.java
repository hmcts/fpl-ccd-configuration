package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.TelephoneNumber;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ChildParty extends Party {
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
    private final TelephoneNumber socialWorkerTel;
    private final String additionalNeeds;
    private final String additionalNeedsDetails;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @Builder(toBuilder = true)
    private ChildParty(String partyID,
                       String partyType,
                       String title,
                       String firstName,
                       String lastName,
                       String organisationName,
                       Date dateOfBirth,
                       Address address,
                       EmailAddress email,
                       TelephoneNumber telephoneNumber,
                       String gender,
                       String genderIdentification,
                       String livingSituation,
                       String situationDetails,
                       Date situationDate,
                       String keyDates,
                       String careAndContact,
                       String adoption,
                       String placementOrderApplication,
                       String placementCourt,
                       String mothersName,
                       String fathersName,
                       String fathersResponsibility,
                       String socialWorkerName,
                       TelephoneNumber socialWorkerTel,
                       String additionalNeeds,
                       String additionalNeedsDetails,
                       String detailsHidden,
                       String detailsHiddenReason,
                       String litigationIssues,
                       String litigationIssuesDetails) {
        super(partyID, partyType, title, firstName, lastName, organisationName, dateOfBirth, address,
            email, telephoneNumber);
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
