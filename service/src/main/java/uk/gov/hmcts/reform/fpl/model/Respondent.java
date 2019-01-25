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
public class Respondent {

    private final String name;
    private final Date dob;
    private final String gender;
    private final String placeOfBirth;
    private final String telephone;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String litigationIssues;
    private final Address address;

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
    ) {
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

}
