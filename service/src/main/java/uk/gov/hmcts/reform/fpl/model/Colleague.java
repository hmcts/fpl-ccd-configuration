package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Colleague {

    @CCD(label = "Role")
    private final ColleagueRole role;
    @CCD(label = "Title", showCondition = "role=\"OTHER\"")
    private String title;
    @CCD(label = "Full name")
    @Deprecated
    private String fullName;
    @CCD(label = "First name")
    private String firstName;
    @CCD(label = "Last name")
    private String lastName;
    @CCD(label = "Email address")
    private String email;
    @CCD(label = "Phone number")
    private String phone;
    @CCD(label = "Alternative phone number")
    private String alternativePhone;
    @CCD(label = "DX code", showCondition = "role=\"SOLICITOR\"")
    private String dx;
    @CCD(label = "Solicitor reference", showCondition = "role=\"SOLICITOR\"")
    private String reference;
    @CCD(label = "Send them case update notifications?", typeOverride = FieldType.YesOrNo)
    private String notificationRecipient;
    @CCD(label = "Main contact", showCondition = "role=\"DO NOT SHOW\"", typeOverride = FieldType.YesOrNo)
    private String mainContact;

    @JsonIgnore
    public String getJobTitle() {
        if (role == ColleagueRole.OTHER) {
            return title;
        }
        return ofNullable(role)
            .map(ColleagueRole::getLabel)
            .orElse(null);
    }

    @JsonIgnore
    public String buildFullName() {
        return (isNotEmpty(firstName) && isNotEmpty(lastName))
            ? StringUtils.joinWith(" ", firstName, lastName) : fullName;
    }

    @JsonIgnore
    // cannot name this method as isMainContact as it will override the default getter method when serialising
    public boolean checkIfMainContact() {
        return YES.getValue().equals(mainContact);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<div class='govuk-tag govuk-tag--purple'>Main contact</div>",
          showCondition = "mainContact=\"Yes\"",
          typeOverride = FieldType.Label
  )
  private String mainContactTag;
  // ==== end synthesised definition-only fields ====
}
