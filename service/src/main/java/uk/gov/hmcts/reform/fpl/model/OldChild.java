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
    public OldChild(@JsonProperty("childName") String childName,
                    @JsonProperty("childDOB") Date childDOB,
                    @JsonProperty("childGender") String childGender,
                    @JsonProperty("childGenderIdentification") String childGenderIdentification,
                    @JsonProperty("livingSituation") String livingSituation,
                    @JsonProperty("situationDetails") String situationDetails,
                    @JsonProperty("situationDate") String situationDate,
                    @JsonProperty("keyDates") String keyDates,
                    @JsonProperty("careAndContact") String careAndContact,
                    @JsonProperty("adoption") String adoption,
                    @JsonProperty("placementOrderApplication") String placementOrderApplication,
                    @JsonProperty("placementCourt") String placementCourt,
                    @JsonProperty("mothersName") String mothersName,
                    @JsonProperty("fathersName") String fathersName,
                    @JsonProperty("fathersResponsibility") String fathersResponsibility,
                    @JsonProperty("socialWorkerName") String socialWorkerName,
                    @JsonProperty("socialWorkerTel") String socialWorkerTel,
                    @JsonProperty("additionalNeeds") String additionalNeeds,
                    @JsonProperty("additionalNeedsDetails") String additionalNeedsDetails,
                    @JsonProperty("detailsHidden") String detailsHidden,
                    @JsonProperty("detailsHiddenReason") String detailsHiddenReason,
                    @JsonProperty("litigationIssues") String litigationIssues,
                    @JsonProperty("litigationIssuesDetails") String litigationIssuesDetails,
                    @JsonProperty("address") Address address) {
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
