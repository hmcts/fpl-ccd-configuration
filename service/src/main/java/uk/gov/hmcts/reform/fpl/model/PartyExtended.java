package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.TelephoneNumber;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public final class PartyExtended extends Party {

    private final String gender;
    private final String genderIdentification;
    private final String placeOfBirth;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @Builder
    private PartyExtended(String partyID,
                          String idamID,
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
                          String placeOfBirth,
                          String relationshipToChild,
                          String contactDetailsHidden,
                          String litigationIssues,
                          String litigationIssuesDetails) {
        super(partyID, idamID, partyType, title, firstName, lastName, organisationName, dateOfBirth, address, email,
            telephoneNumber);

        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.placeOfBirth = placeOfBirth;
        this.relationshipToChild = relationshipToChild;
        this.contactDetailsHidden = contactDetailsHidden;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
    }
}
