package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Other {
    private final String name;
    private final String DOB;
    private final String gender;
    private final String genderIdentification;
    private final String birthPlace;
    private final Address address;
    private final String telephone;
    private final String childInformation;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @JsonCreator
    public Other(@JsonProperty("name") String name,
                 @JsonProperty("DOB") String DOB,
                 @JsonProperty("gender") String gender,
                 @JsonProperty("genderIdentification") String genderIdentification,
                 @JsonProperty("birthPlace") String birthPlace,
                 @JsonProperty("address") Address address,
                 @JsonProperty("telephone") String telephone,
                 @JsonProperty("childInformation") String childInformation,
                 @JsonProperty("detailsHidden") String detailsHidden,
                 @JsonProperty("detailsHiddenReason") String detailsHiddenReason,
                 @JsonProperty("litigationIssues") String litigationIssues,
                 @JsonProperty("litigationIssuesDetails") String litigationIssuesDetails) {
        this.name = name;
        this.DOB = DOB;
        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.birthPlace = birthPlace;
        this.address = address;
        this.telephone = telephone;
        this.childInformation = childInformation;
        this.detailsHidden = detailsHidden;
        this.detailsHiddenReason =detailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
    }
}
