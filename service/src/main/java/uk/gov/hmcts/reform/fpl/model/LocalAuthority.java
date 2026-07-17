package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Jacksonized
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalAuthority {

    @CCD(label = "Local authority's id", showCondition = "name=\"DO NOT SHOW\"")
    private final String id;
    @CCD(label = "Name")
    private String name;
    @CCD(label = "Group email address")
    private String email;
    @CCD(label = "Phone number")
    private String phone;
    @CCD(label = "Address")
    private Address address;
    @CCD(
            label = "Legal team manager's name and last name",
            hint = "The statement of truth will be signed in this person's name"
    )
    private String legalTeamManager;
    @CCD(label = "PBA number", hint = "For example, PBA1234567", typeOverride = FieldType.DynamicList)
    @Temp
    private DynamicList pbaNumberDynamicList;
    @CCD(label = "PBA number", hint = "For example, PBA1234567")
    private String pbaNumber;
    @CCD(label = "Client code")
    private String clientCode;
    @CCD(label = "Customer reference")
    private String customerReference;
    @CCD(label = "Details of person you are representing")
    private RepresentingDetails representingDetails;
    @CCD(label = "Colleague")
    @Builder.Default
    private List<Element<Colleague>> colleagues = new ArrayList<>();
    @CCD(label = "Designated", showCondition = "id=\"DO NOT SHOW\"", typeOverride = FieldType.YesOrNo)
    private String designated;

    @JsonIgnore
    public Optional<Colleague> getFirstSolicitor() {
        return unwrapElements(colleagues).stream()
            .filter(colleague -> SOLICITOR.equals(colleague.getRole()))
            .findFirst();
    }

    @JsonIgnore
    public Optional<Element<Colleague>> getMainContactElement() {
        return nullSafeCollection(colleagues).stream()
            .filter(colleague -> colleague.getValue().checkIfMainContact())
            .findFirst();
    }

    @JsonIgnore
    public Optional<Colleague> getMainContact() {
        return getMainContactElement().map(Element::getValue);
    }

    @JsonIgnore
    public List<Element<Colleague>> getOtherContact() {
        return nullSafeCollection(colleagues).stream()
            .filter(colleague -> !colleague.getValue().checkIfMainContact())
            .toList();
    }

    @JsonIgnore
    public List<String> getContactEmails() {
        return unwrapElements(colleagues).stream()
            .filter(colleague -> YES.getValue().equals(colleague.getNotificationRecipient()))
            .map(Colleague::getEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<div class='govuk-tag govuk-tag--purple'>Designated local authority</div>",
          showCondition = "designated=\"Yes\"",
          typeOverride = FieldType.Label
  )
  private String designatedTag;
  @CCD(label = " ", typeOverride = FieldType.Label)
  private String applicationDetailsLabel;
  @CCD(label = " ", typeOverride = FieldType.Label)
  private String solicitorDetailsLabel;
  // ==== end synthesised definition-only fields ====
}
