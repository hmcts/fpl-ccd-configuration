package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.groups.Default;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasGender;

import java.time.LocalDate;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@HasGender(groups = {Default.class, SealedSDOGroup.class})
@SuppressWarnings({"java:S1133","java:S1874"})
public final class ChildParty extends Party {
    @CCD(
            label = "What was the child's sex at birth?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ChildGenderList"
    )
    private final ChildGender gender;
    @CCD(label = "What gender do they identify with?", showCondition = "gender=\"They identify in another way\"")
    private final String genderIdentification;
    @CCD(
            label = "Child's current living situation",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "LivingSituationList",
            typeParameterClass = LivingSituationList.class
    )
    private final String livingSituation;
    @CCD(
            label = "Provide the full details of their living situation",
            showCondition = "livingSituation=\"Other\"",
            typeOverride = FieldType.TextArea
    )
    private final String livingSituationDetails;
    @CCD(
            label = "Do you need to keep the address confidential?",
            showCondition = "livingSituation=\"Living with respondents\" OR livingSituation=\"Living with other family or friends\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String isAddressConfidential;
    @CCD(
            label = "Who are they living with?",
            hint = "Provide their names and relationship to the child",
            showCondition = "livingSituation=\"Living with other family or friends\"",
            typeOverride = FieldType.TextArea
    )
    private final String livingWithDetails;
    @CCD(
            label = "When did they start staying here?",
            hint = "For example, 31 3 2018",
            showCondition = "livingSituation=\"Living with other family or friends\""
    )
    private final LocalDate addressChangeDate;
    @CCD(label = "Date powers end", hint = "For example, 31 3 2018", showCondition = "livingSituation=\"Removed by *\"")
    private final LocalDate datePowersEnd;
    @CCD(
            label = "Date they went into care",
            hint = "For example, 31 3 2018",
            showCondition = "livingSituation=\"Voluntarily in section 20 care order\" OR livingSituation=\"Under the care of local authority\""
    )
    private final LocalDate careStartDate;
    @CCD(
            label = "Date of discharge",
            hint = "For example, 31 3 2018",
            showCondition = "livingSituation=\"In hospital and soon to be discharged\""
    )
    private final LocalDate dischargeDate;
    @CCD(
            label = "Important dates we need to consider when scheduling hearings",
            hint = "List any events HMCTS will need to take into account when scheduling hearings. For example, child starting primary school or taking GCSEs.",
            typeOverride = FieldType.TextArea
    )
    private final String keyDates;
    @CCD(
            label = "Brief summary of care and contact plan",
            hint = "For example, place baby in local authority foster care until further assessments are completed. Supervised contact for parents will be arranged.",
            typeOverride = FieldType.TextArea
    )
    private final String careAndContactPlan;
    @CCD(label = "Is adoption being considered at this stage?", typeOverride = FieldType.YesOrNo)
    private final String adoption;
    @CCD(
            label = "Are you submitting an application for a placement order?",
            showCondition = "adoption=\"Yes\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String placementOrderApplication;
    @CCD(
            label = "Which court are you applying to?",
            showCondition = "placementOrderApplication=\"Yes\" AND adoption=\"Yes\"",
            typeOverride = FieldType.TextArea
    )
    private final String placementCourt;
    @CCD(label = "Birth mother's full name")
    private final String mothersName;
    @CCD(label = "Birth father's full name")
    private final String fathersName;
    /**
     * No longer used as part of C110a flow and template DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @CCD(
            label = "Does the father have parental responsibility?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FathersResponsibilityList",
            typeParameterClass = FathersResponsibilityList.class
    )
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String fathersResponsibility;
    @CCD(label = "Name of social worker")
    private final String socialWorkerName;
    @CCD(label = " ")
    private final Telephone socialWorkerTelephoneNumber;
    @CCD(label = "Social worker's email")
    private final String socialWorkerEmail;
    @CCD(
            label = "Do you need social worker contact details to be confidential from other parties",
            typeOverride = FieldType.YesOrNo
    )
    private final String socialWorkerDetailsHidden;
    @CCD(
            label = "Give a reason",
            showCondition = "socialWorkerDetailsHidden=\"Yes\"",
            typeOverride = FieldType.TextArea
    )
    private final String socialWorkerDetailsHiddenReason;
    @CCD(
            label = "Does the child have any additional needs?",
            hint = "For example, physical or learning disabilities, severe allergies or conditions that need to be taken into account.",
            typeOverride = FieldType.YesOrNo
    )
    private final String additionalNeeds;
    @CCD(label = "Give details", showCondition = "additionalNeeds=\"Yes\"", typeOverride = FieldType.TextArea)
    private final String additionalNeedsDetails;
    /**
     * Replaced by isAddressConfidential and socialWorkerDetailHidden DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @CCD(
            label = "Do you need contact details hidden from other parties?",
            showCondition = "livingSituation=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String detailsHidden;
    /**
     * No longer used, replaced by socialWorkerDetailsHiddenReason DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @CCD(label = "Give reason", showCondition = "livingSituation=\"DO_NOT_SHOW\"", typeOverride = FieldType.TextArea)
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String detailsHiddenReason;
    /**
     * No longer used as part of C110a flow and template DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @CCD(
            label = "Do you believe this child will have problems with litigation capacity (understanding what's happening in the case)?",
            showCondition = "livingSituation=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "LitigationCapacityIssues",
            typeParameterClass = LitigationCapacityIssues.class
    )
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String litigationIssues;
    /**
     * No longer required as part of C110a flow and template DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @CCD(
            label = "Give details, including assessment outcomes and referrals to health services",
            showCondition = "livingSituation=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String litigationIssuesDetails;
    /**
     * Replaced by isAddressConfidential but kept backwards compatability DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @CCD(label = "Should Address field be shown in confidential tab", showCondition = "partyId=\"DO_NOT_SHOW\"")
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String showAddressInConfidentialTab;
    @CCD(label = "Completion date", hint = "For example, 31 3 2018")
    private final LocalDate completionDate;
    @CCD(label = "Reason for extending completion date")
    private final CaseExtensionReasonList extensionReason;

    @Override
    @NotBlank(message = "Tell us the names of all children in the case")
    public String getFirstName() {
        return super.getFirstName();
    }

    @Override
    @NotBlank(message = "Tell us the names of all children in the case")
    public String getLastName() {
        return super.getLastName();
    }

    @Override
    @NotNull(message = "Tell us the date of birth of all children in the case",
        groups = {Default.class, SealedSDOGroup.class})
    @PastOrPresent(message = "Date of birth is in the future. You cannot send this application until that date")
    public LocalDate getDateOfBirth() {
        return super.getDateOfBirth();
    }

    public String getIsAddressConfidential() {
        if (isNotEmpty(isAddressConfidential)) {
            return isAddressConfidential;
        } else if (isNotEmpty(detailsHidden)) {
            return YesNo.from(YesNo.YES.equalsString(getDetailsHidden())).getValue();
        } else {
            return null;
        }
    }

    public String getSocialWorkerDetailsHidden() {
        if (isNotEmpty(socialWorkerDetailsHidden)) {
            return socialWorkerDetailsHidden;
        } else if (isNotEmpty(detailsHidden)) {
            return YesNo.from(YesNo.YES.equalsString(getDetailsHidden())).getValue();
        } else {
            return null;
        }
    }

    @Builder(toBuilder = true)
    @SuppressWarnings("java:S107")
    public ChildParty(String partyId,
                      PartyType partyType,
                      String firstName,
                      String lastName,
                      String organisationName,
                      LocalDate dateOfBirth,
                      Address address,
                      EmailAddress email,
                      Telephone telephoneNumber,
                      ChildGender gender,
                      String genderIdentification,
                      String livingSituation,
                      String livingSituationDetails,
                      String isAddressConfidential,
                      String livingWithDetails,
                      LocalDate addressChangeDate,
                      LocalDate datePowersEnd,
                      LocalDate careStartDate,
                      LocalDate dischargeDate,
                      String keyDates,
                      String careAndContactPlan,
                      String adoption,
                      String placementOrderApplication,
                      String placementCourt,
                      String mothersName,
                      String fathersName,
                      String fathersResponsibility,
                      String socialWorkerName,
                      Telephone socialWorkerTelephoneNumber,
                      String socialWorkerEmail,
                      String socialWorkerDetailsHidden,
                      String socialWorkerDetailsHiddenReason,
                      String additionalNeeds,
                      String additionalNeedsDetails,
                      String detailsHidden,
                      String detailsHiddenReason,
                      String litigationIssues,
                      String litigationIssuesDetails,
                      String showAddressInConfidentialTab,
                      LocalDate completionDate,
                      CaseExtensionReasonList extensionReason) {
        super(partyId, partyType, firstName, lastName, organisationName,
            dateOfBirth, address, email, telephoneNumber);
        this.gender = gender;
        this.genderIdentification = genderIdentification;
        this.livingSituation = livingSituation;
        this.livingSituationDetails = livingSituationDetails;
        this.isAddressConfidential = isNotEmpty(isAddressConfidential) ? isAddressConfidential :
            isNotEmpty(detailsHidden) ? YesNo.from(YesNo.YES.equalsString(detailsHidden)).getValue() : null;
        this.livingWithDetails = livingWithDetails;
        this.addressChangeDate = addressChangeDate;
        this.datePowersEnd = datePowersEnd;
        this.careStartDate = careStartDate;
        this.dischargeDate = dischargeDate;
        this.keyDates = keyDates;
        this.careAndContactPlan = careAndContactPlan;
        this.adoption = adoption;
        this.placementOrderApplication = placementOrderApplication;
        this.placementCourt = placementCourt;
        this.mothersName = mothersName;
        this.fathersName = fathersName;
        this.fathersResponsibility = fathersResponsibility;
        this.socialWorkerName = socialWorkerName;
        this.socialWorkerTelephoneNumber = socialWorkerTelephoneNumber;
        this.socialWorkerEmail = socialWorkerEmail;
        this.socialWorkerDetailsHidden = isNotEmpty(socialWorkerDetailsHidden) ? socialWorkerDetailsHidden :
            isNotEmpty(detailsHidden) ? YesNo.from(YesNo.YES.equalsString(detailsHidden)).getValue() : null;
        this.socialWorkerDetailsHiddenReason = socialWorkerDetailsHiddenReason;
        this.additionalNeeds = additionalNeeds;
        this.additionalNeedsDetails = additionalNeedsDetails;
        this.detailsHidden = detailsHidden;
        this.detailsHiddenReason = detailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
        this.showAddressInConfidentialTab = showAddressInConfidentialTab;
        this.completionDate = completionDate;
        this.extensionReason = extensionReason;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## This address is automatically made confidential",
          showCondition = "livingSituation=\"Living in a refuge\"",
          typeOverride = FieldType.Label
  )
  private String addressAutoConfidentialLabel;
  @CCD(label = "Why is this case being extended?")
  private CaseExtensionReasonList caseExtensionReasonList;
  @CCD(
          label = "## Ability to take part in proceedings",
          showCondition = "proceedingsLabel=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String proceedingsLabel;
  // ==== end synthesised definition-only fields ====
}
