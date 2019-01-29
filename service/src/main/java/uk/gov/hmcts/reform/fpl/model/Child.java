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
public class Child {

    private final String childName;
    private final Date childDOB;
    private final String childGender;
    private final String livingSituation;
    private final String keyDates;
    private final String careAndContact;
    private final String adoption;
    private final String mothersName;
    private final String fathersName;
    private final String fathersResponsibility;
    private final String socialWorkerName;
    private final String socialWorkerTel;
    private final String additionalNeeds;
    private final String detailsHidden;
    private final String litigationIssues;
    private final Address address;

    @JsonCreator
    public Child(@JsonProperty("childName") final String childName,
                 @JsonProperty("childDOB") final Date childDOB,
                 @JsonProperty("childGender") final String childGender,
                 @JsonProperty("livingSituation") final String livingSituation,
                 @JsonProperty("keyDates") final String keyDates,
                 @JsonProperty("careAndContact") final String careAndContact,
                 @JsonProperty("adoption") final String adoption,
                 @JsonProperty("mothersName") final String mothersName,
                 @JsonProperty("fathersName") final String fathersName,
                 @JsonProperty("fathersResponsibility") final String fathersResponsibility,
                 @JsonProperty("socialWorkerName") final String socialWorkerName,
                 @JsonProperty("socialWorkerTel") final String socialWorkerTel,
                 @JsonProperty("additionalNeeds") final String additionalNeeds,
                 @JsonProperty("detailsHidden") final String detailsHidden,
                 @JsonProperty("litigationIssues") final String litigationIssues,
                 @JsonProperty("address") final Address address) {
        this.childName = childName;
        this.childDOB = childDOB;
        this.childGender = childGender;
        this.livingSituation = livingSituation;
        this.keyDates = keyDates;
        this.careAndContact = careAndContact;
        this.adoption = adoption;
        this.mothersName = mothersName;
        this.fathersName = fathersName;
        this.fathersResponsibility = fathersResponsibility;
        this.socialWorkerName = socialWorkerName;
        this.socialWorkerTel = socialWorkerTel;
        this.additionalNeeds = additionalNeeds;
        this.detailsHidden = detailsHidden;
        this.litigationIssues = litigationIssues;
        this.address = address;
    }
}
