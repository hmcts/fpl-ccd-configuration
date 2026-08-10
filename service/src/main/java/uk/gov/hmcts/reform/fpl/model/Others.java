package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.GenderList;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.AddressNotKnowType;
import uk.gov.hmcts.reform.fpl.model.LitigationCapacityIssues;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Others {
    @CCD(ignore = true)
    private final Other firstOther;
    @CCD(ignore = true)
    private final List<Element<Other>> additionalOthers;

    public static Others from(List<Element<Other>> allOthers) {
        final LinkedList<Element<Other>> others = new LinkedList<>(Optional.ofNullable(allOthers).orElse(emptyList()));
        others.removeIf(other -> isNull(other) || isNull(other.getValue()) || other.getValue().isEmpty());

        if (isEmpty(others)) {
            return null;
        }

        return Others.builder()
            .firstOther(ofNullable(others.pollFirst()).map(Element::getValue).orElse(null))
            .additionalOthers(others)
            .build();
    }

    @JsonIgnore
    public boolean hasOthers() {
        return !(firstOther == null || firstOther.isEmpty()) || isNotEmpty(additionalOthers);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Full name")
  private String name;
  @CCD(label = "First name")
  private String firstName;
  @CCD(label = "Last name")
  private String lastName;
  @CCD(label = "Date of birth", hint = "For example, 31 3 1980")
  private java.time.LocalDate DOB;
  @CCD(label = "Gender", typeOverride = FieldType.FixedList, typeParameterOverride = "GenderList")
  private GenderList gender;
  @CCD(label = "What gender do they identify with?", showCondition = "gender=\"They identify in another way\"")
  private String genderIdentification;
  @CCD(label = "Place of birth", hint = "For example, town or city")
  private String birthPlace;
  @CCD(label = "*Current address known?", showCondition = "addressKnow=\"*\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo addressKnow;
  @CCD(label = "*Current address known?")
  private IsAddressKnowType addressKnowV2;
  @CCD(
          label = "## This address is automatically made confidential",
          showCondition = "addressKnowV2=\"LIVE_IN_REFUGE\"",
          typeOverride = FieldType.Label
  )
  private String addressAutoConfidentialLabel;
  @CCD(label = "Current address", showCondition = "addressKnowV2=\"Yes\" OR addressKnowV2=\"LIVE_IN_REFUGE\"")
  private uk.gov.hmcts.ccd.sdk.type.AddressUK address;
  @CCD(
          label = "*Reason the address is not known",
          showCondition = "addressKnowV2=\"No\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "AddressNotKnowType"
  )
  private AddressNotKnowType addressNotKnowReason;
  @CCD(
          label = "Give more details",
          showCondition = "addressKnowV2=\"No\" AND addressNotKnowReason=\"Whereabouts unknown\"",
          typeOverride = FieldType.TextArea
  )
  private String whereaboutsUnknownDetails;
  @CCD(label = "Do you need to keep the address confidential?", showCondition = "addressKnowV2=\"Yes\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hideAddress;
  @CCD(label = "Telephone number", max = 24)
  private String telephone;
  @CCD(label = "Do you need to keep the contact number confidential?", showCondition = "telephone=\"*\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hideTelephone;
  @CCD(
          label = "Do you need contact details hidden from other parties?",
          showCondition = "addressKnowV2!=\"LIVE_IN_REFUGE\""
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo detailsHidden;
  @CCD(
          label = "Give reason",
          showCondition = "detailsHidden=\"Yes\" AND addressKnowV2!=\"LIVE_IN_REFUGE\"",
          typeOverride = FieldType.TextArea
  )
  private String detailsHiddenReason;
  @CCD(
          label = "What is this person's relationship to the child or children in this case?",
          hint = "Include: the name of the child or children, this person's relationship to them and whether this person has parental responsibility",
          typeOverride = FieldType.TextArea
  )
  private String childInformation;
  @CCD(label = "## Ability to take part in proceedings", typeOverride = FieldType.Label)
  private String proceedingsLabel;
  @CCD(
          label = "Do you believe this person will have problems with litigation capacity (understanding what's happening in the case)?"
  )
  private LitigationCapacityIssues litigationIssues;
  @CCD(
          label = "Give details, including assessment outcomes and referrals to health services",
          showCondition = "litigationIssues=\"YES\"",
          typeOverride = FieldType.TextArea
  )
  private String litigationIssuesDetails;
  @CCD(label = "Represented by", showCondition = "representedBy=\"DO_NOT_SHOW\"")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<String>> representedBy;
  // ==== end synthesised definition-only fields ====
}
