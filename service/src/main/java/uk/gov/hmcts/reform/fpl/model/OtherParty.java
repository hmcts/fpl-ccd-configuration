package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.TelephoneNumber;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class OtherParty extends Party {
    private final String name;
    private final String gender;
    private final String genderIdentification;
    private final String birthPlace;
    private final String childInformation;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;

    @Builder
    private OtherParty(String partyID,
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
                       String name,
                       String gender,
                       String genderIdentification,
                       String birthPlace,
                       String childInformation,
                       String detailsHidden,
                       String detailsHiddenReason,
                       String litigationIssues,
                       String litigationIssuesDetails) {
        super(partyID, idamID, partyType, title, firstName, lastName, organisationName, dateOfBirth, address, email,
            telephoneNumber);

        this.name = name;
        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.birthPlace = birthPlace;
        this.childInformation = childInformation;
        this.detailsHidden = detailsHidden;
        this.detailsHiddenReason = detailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
    }
}
