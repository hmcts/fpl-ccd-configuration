package uk.gov.hmcts.reform.fpl.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Child {

    @JsonCreator
    public Child(@JsonProperty("childName") final String childName,
                 @JsonProperty("childDOB") final String childDOB,
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

    private String childName;
    private String childDOB;
    private String childGender;
    private String livingSituation;
    private String keyDates;
    private String careAndContact;
    private String adoption;
    private String mothersName;
    private String fathersName;
    private String fathersResponsibility;
    private String socialWorkerName;
    private String socialWorkerTel;
    private String additionalNeeds;
    private String detailsHidden;
    private String litigationIssues;
    private Address address;

    public String getChildName() {
        return childName;
    }

    public String getChildDOB() {
        return childDOB;
    }

    public String getChildGender() {
        return childGender;
    }

    public String getLivingSituation() {
        return livingSituation;
    }

    public String getKeyDates() {
        return keyDates;
    }


    public String getCareAndContact() {
        return careAndContact;
    }

    public String getAdoption() {
        return adoption;
    }

    public String getMothersName() {
        return mothersName;
    }

    public String getFathersName() {
        return fathersName;
    }

    public String getFathersResponsibility() {
        return fathersResponsibility;
    }

    public String getSocialWorkerName() {
        return socialWorkerName;
    }

    public String getSocialWorkerTel() {
        return socialWorkerTel;
    }

    public String getAdditionalNeeds() {
        return additionalNeeds;
    }

    public String getDetailsHidden() {
        return detailsHidden;
    }

    public String getLitigationIssues() {
        return litigationIssues;
    }

    public Address getAddress() {
        return address;
    }


}
