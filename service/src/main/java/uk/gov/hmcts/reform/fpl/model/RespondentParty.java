package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = RespondentParty.RespondentPartyBuilder.class)
public final class RespondentParty extends Party {
    private final String gender;
    private final String genderIdentification;
    private final String placeOfBirth;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
    private final String addressNotKnowReason;
    private final IsAddressKnowType addressKnow;


    @Override
    @NotBlank(message = "Enter the respondent's full name")
    public String getFirstName() {
        return super.getFirstName();
    }

    @Override
    @NotBlank(message = "Enter the respondent's full name")
    public String getLastName() {
        return super.getLastName();
    }

    @NotBlank(message = "Enter the respondent's relationship to child",
        groups = {Default.class, SealedSDOGroup.class})
    public String getRelationshipToChild() {
        return relationshipToChild;
    }

    @Builder(toBuilder = true, builderClassName = "RespondentPartyBuilder")
    @SuppressWarnings("java:S107")
    public RespondentParty(String partyId,
                           PartyType partyType,
                           String firstName,
                           String lastName,
                           String organisationName,
                           LocalDate dateOfBirth,
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
                           String litigationIssuesDetails,
                           String addressNotKnowReason,  IsAddressKnowType addressKnow) {
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
        this.addressNotKnowReason = addressNotKnowReason;
        this.addressKnow = addressKnow;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class RespondentPartyBuilder {
    }

    public IsAddressKnowType getAddressKnow() {
        if (addressKnow != null) {
            return addressKnow;
        }
        return this.address != null && StringUtils.isNotBlank(this.address.getAddressLine1())
            ? IsAddressKnowType.YES : null;
    }
}
