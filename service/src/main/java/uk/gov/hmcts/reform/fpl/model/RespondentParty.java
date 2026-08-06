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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = RespondentParty.RespondentPartyBuilder.class)
public final class RespondentParty extends Party {
    @CCD(
            label = "What is the respondent's gender?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "GenderList"
    )
    private final String gender;
    @CCD(label = "What gender do they identify with?", showCondition = "gender=\"They identify in another way\"")
    private final String genderIdentification;
    @CCD(label = "Place of birth", hint = "For example, town or city")
    private final String placeOfBirth;
    @CCD(
            label = "Which children does the respondent have parental responsibility for and what is their relationship?",
            typeOverride = FieldType.TextArea
    )
    private final String relationshipToChild;
    @CCD(
            label = "Do you need contact details hidden from other parties?",
            showCondition = "addressKnow!=\"LIVE_IN_REFUGE\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String contactDetailsHidden;
    @CCD(
            label = "Give reason",
            showCondition = "contactDetailsHidden=\"Yes\" AND addressKnow!=\"LIVE_IN_REFUGE\"",
            typeOverride = FieldType.TextArea
    )
    private final String contactDetailsHiddenReason;
    @CCD(
            label = "Do you believe this person will have difficulty understanding what's happening with the case?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "LitigationCapacityIssues"
    )
    private final String litigationIssues;
    @CCD(
            label = "Give details, including assessment outcomes and referrals to health services",
            showCondition = "litigationIssues=\"YES\"",
            typeOverride = FieldType.TextArea
    )
    private final String litigationIssuesDetails;
    @CCD(
            label = "Why is this address unknown?",
            showCondition = "addressKnow=\"No\"",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "AddressNotKnowType"
    )
    private final String addressNotKnowReason;
    @CCD(label = "Current address known?")
    private final IsAddressKnowType addressKnow;
    @CCD(
            label = "Do you need to keep the address confidential?",
            showCondition = "addressKnow=\"Yes\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String hideAddress;
    @CCD(label = "Do you need to keep the contact number confidential?", typeOverride = FieldType.YesOrNo)
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## This address is automatically made confidential",
          showCondition = "addressKnow=\"LIVE_IN_REFUGE\"",
          typeOverride = FieldType.Label
  )
  private String addressAutoConfidentialLabel;
  @CCD(
          label = "Give more details",
          showCondition = "addressKnow=\"No\" AND addressNotKnowReason=\"Whereabouts unknown\"",
          typeOverride = FieldType.TextArea
  )
  private String whereaboutsUnknownDetails;
  @CCD(
          label = "## Relationship to the child",
          showCondition = "relationshipLabel=\"HIDE_LABEL\"",
          typeOverride = FieldType.Label
  )
  private String relationshipLabel;
  @CCD(
          label = "## Ability to take part in proceedings",
          showCondition = "proceedingsLabel=\"HIDE_LABEL\"",
          typeOverride = FieldType.Label
  )
  private String proceedingsLabel;
  // ==== end synthesised definition-only fields ====
}
