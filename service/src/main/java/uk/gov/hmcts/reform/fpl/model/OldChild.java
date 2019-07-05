package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("\"squid:S00107\"")
public class OldChild {

    private final String childName;
    private final Date childDOB;
    private final String childGender;
    private final String childGenderIdentification;
    private final String livingSituation;
    private final String situationDetails;
    private final String situationDate;
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
    private final Address address;

    @JsonCreator
    public OldChild(@JsonProperty("childName") final String childName,
                    @JsonProperty("childDOB") final Date childDOB,
                    @JsonProperty("childGender") final String childGender,
                    @JsonProperty("childGenderIdentification") final String childGenderIdentification,
                    @JsonProperty("livingSituation") final String livingSituation,
                    @JsonProperty("situationDetails") final String situationDetails,
                    @JsonProperty("situationDate") final String situationDate,
                    @JsonProperty("keyDates") final String keyDates,
                    @JsonProperty("careAndContact") final String careAndContact,
                    @JsonProperty("adoption") final String adoption,
                    @JsonProperty("placementOrderApplication") final String placementOrderApplication,
                    @JsonProperty("placementCourt") final String placementCourt,
                    @JsonProperty("mothersName") final String mothersName,
                    @JsonProperty("fathersName") final String fathersName,
                    @JsonProperty("fathersResponsibility") final String fathersResponsibility,
                    @JsonProperty("socialWorkerName") final String socialWorkerName,
                    @JsonProperty("socialWorkerTel") final String socialWorkerTel,
                    @JsonProperty("additionalNeeds") final String additionalNeeds,
                    @JsonProperty("additionalNeedsDetails") final String additionalNeedsDetails,
                    @JsonProperty("detailsHidden") final String detailsHidden,
                    @JsonProperty("detailsHiddenReason") final String detailsHiddenReason,
                    @JsonProperty("litigationIssues") final String litigationIssues,
                    @JsonProperty("litigationIssuesDetails") final String litigationIssuesDetails,
                    @JsonProperty("address") final Address address) {
        this.childName = childName;
        this.childDOB = childDOB;
        this.childGender = childGender;
        this.childGenderIdentification = childGenderIdentification;
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
        this.address = address;
    }
}
