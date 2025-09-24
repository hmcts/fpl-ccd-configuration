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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;

import java.time.LocalDate;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
    private final String hideAddress;
    private final String hideTelephone;


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
                           String addressNotKnowReason,
                           IsAddressKnowType addressKnow,
                           String hideAddress,
                           String hideTelephone) {
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
        // Check value if set, if not check contactDetails hidden (old field), otherwise default to No
        this.hideAddress = isNotEmpty(hideAddress) ? hideAddress
            : YesNo.from(YesNo.YES.equalsString(contactDetailsHidden)).getValue();
        this.hideTelephone = isNotEmpty(hideTelephone) ? hideTelephone
            : YesNo.from(YesNo.YES.equalsString(contactDetailsHidden)).getValue();
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

    public String getHideAddress() {
        if (isNotEmpty(hideAddress)) {
            return hideAddress;
        }
        return YesNo.from(YesNo.YES.equalsString(contactDetailsHidden)).getValue();
    }

    public String getHideTelephone() {
        if (isNotEmpty(hideTelephone)) {
            return hideTelephone;
        }
        return YesNo.from(YesNo.YES.equalsString(contactDetailsHidden)).getValue();
    }

}
