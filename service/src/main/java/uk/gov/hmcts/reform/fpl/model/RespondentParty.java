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

@Data
@EqualsAndHashCode(callSuper = true)
public final class RespondentParty extends Party {

    private final String gender;
    private final String genderIdentification;
    private final String placeOfBirth;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    //TODO: remove jsonCreator and property annotations
    //TODO: toBuilder means that TelephoneNumber and EmailAddress are instantiated.

    @JsonCreator
    @Builder(toBuilder = true)
    public RespondentParty(@JsonProperty("partyID") String partyID,
                           @JsonProperty("partyType") String partyType,
                           @JsonProperty("firstName") String firstName,
                           @JsonProperty("lastName") String lastName,
                           @JsonProperty("dateOfBirth") Date dateOfBirth,
                           @JsonProperty("address") Address address,
                           @JsonProperty("email") EmailAddress email,
                           @JsonProperty("telephoneNumber") TelephoneNumber telephoneNumber,
                           @JsonProperty("gender") String gender,
                           @JsonProperty("genderIdentification") String genderIdentification,
                           @JsonProperty("placeOfBirth") String placeOfBirth,
                           @JsonProperty("relationshipToChild") String relationshipToChild,
                           @JsonProperty("contactDetailsHidden") String contactDetailsHidden,
                           @JsonProperty("contactDetailsHiddenReason") String contactDetailsHiddenReason,
                           @JsonProperty("litigationIssues") String litigationIssues,
                           @JsonProperty("litigationIssuesDetails") String litigationIssuesDetails) {
        super(partyID, partyType, firstName, lastName, dateOfBirth, address, email,
            telephoneNumber);

        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.placeOfBirth = placeOfBirth;
        this.relationshipToChild = relationshipToChild;
        this.contactDetailsHidden = contactDetailsHidden;
        this.contactDetailsHiddenReason = contactDetailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
    }
}
