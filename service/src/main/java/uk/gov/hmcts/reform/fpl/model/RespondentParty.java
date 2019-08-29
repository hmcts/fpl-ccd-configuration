package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RespondentParty extends Party {
    private final String gender;
    private final String genderIdentification;
    private final String placeOfBirth;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @Builder(toBuilder = true)
    public RespondentParty(String partyId,
                           PartyType partyType,
                           String firstName,
                           String lastName,
                           String organisationName,
                           Date dateOfBirth,
                           Address address,
                           EmailAddress email,
                           Telephone telephoneNumber,
                           String gender,
                           String genderIdentification,
                           String placeOfBirth,
                           String relationshipToChild,
                           String contactDetailsHidden,
                           String contactDetailsHiddenReason,
                           String litigationIssues,
                           String litigationIssuesDetails) {
        super(partyId, partyType, firstName, lastName, organisationName,
            dateOfBirth, address, email, telephoneNumber);
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
