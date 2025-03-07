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


@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@HasGender(groups = {Default.class, SealedSDOGroup.class})
@SuppressWarnings({"java:S1133","java:S1874"})
public final class ChildParty extends Party {
    private final ChildGender gender;
    private final String genderIdentification;
    private final String livingSituation;
    private final String livingSituationDetails;
    private final String isAddressConfidential;
    private final String livingWithDetails;
    private final LocalDate addressChangeDate;
    private final LocalDate datePowersEnd;
    private final LocalDate careStartDate;
    private final LocalDate dischargeDate;
    private final String keyDates;
    private final String careAndContactPlan;
    private final String adoption;
    private final String placementOrderApplication;
    private final String placementCourt;
    private final String mothersName;
    private final String fathersName;
    /**
     * No longer used as part of C110a flow and template DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String fathersResponsibility;
    private final String socialWorkerName;
    private final Telephone socialWorkerTelephoneNumber;
    private final String socialWorkerEmail;
    private final String socialWorkerDetailsHidden;
    private final String socialWorkerDetailsHiddenReason;
    private final String additionalNeeds;
    private final String additionalNeedsDetails;
    /**
     * Replaced by isAddressConfidential and socialWorkerDetailHidden DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String detailsHidden;
    /**
     * No longer used, replaced by socialWorkerDetailsHiddenReason DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String detailsHiddenReason;
    /**
     * No longer used as part of C110a flow and template DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String litigationIssues;
    /**
     * No longer required as part of C110a flow and template DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String litigationIssuesDetails;
    /**
     * Replaced by isAddressConfidential but kept backwards compatability DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String showAddressInConfidentialTab;
    private final LocalDate completionDate;
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
        }
        return YesNo.from(YesNo.YES.equalsString(getDetailsHidden())).getValue();
    }

    public String getSocialWorkerDetailsHidden() {
        if (isNotEmpty(socialWorkerDetailsHidden)) {
            return socialWorkerDetailsHidden;
        }
        return YesNo.from(YesNo.YES.equalsString(getDetailsHidden())).getValue();
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
            YesNo.from(YesNo.YES.equalsString(detailsHidden)).getValue();
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
            YesNo.from(YesNo.YES.equalsString(detailsHidden)).getValue();
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
}
