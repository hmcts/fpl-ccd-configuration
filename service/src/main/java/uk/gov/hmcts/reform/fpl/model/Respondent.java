package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Respondent {

    private String name;
    private Date dob;
    private String gender;
    private String placeOfBirth;
    private String telephone;
    private String relationshipToChild;
    private String contactDetailsHidden;
    private String litigationIssues;
    private Address address;

    @JsonCreator
    public Respondent(@JsonProperty("name") final String name,
                      @JsonProperty("dob") final Date dob,
                      @JsonProperty("gender") final String gender,
                      @JsonProperty("placeOfBirth") final String placeOfBirth,
                      @JsonProperty("telephone") final String telephone,
                      @JsonProperty("relationshipToChild") final String relationshipToChild,
                      @JsonProperty("contactDetailsHidden") final String contactDetailsHidden,
                      @JsonProperty("litigationIssues") final String litigationIssues,
                      @JsonProperty("address") final Address address
                      ){
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.placeOfBirth = placeOfBirth;
        this.telephone = telephone;
        this.relationshipToChild = relationshipToChild;
        this.contactDetailsHidden = contactDetailsHidden;
        this.litigationIssues = litigationIssues;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Date getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getRelationshipToChild() {
        return relationshipToChild;
    }

    public String getContactDetailsHidden() {
        return contactDetailsHidden;
    }

    public String getLitigationIssues() {
        return litigationIssues;
    }

    public Address getAddress() {
        return address;
    }

}
