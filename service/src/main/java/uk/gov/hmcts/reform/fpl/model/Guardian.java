package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Guardian {
    @CCD(label = "Name of Guardian")
    private String guardianName;
    @CCD(label = "Telephone number")
    private String telephoneNumber;
    @CCD(label = "Email address", typeOverride = FieldType.Email)
    private String email;
    @CCD(ignore = true)
    private List<String> children;

    // getter/setter work around for CCD persisting only
    public List<Element<String>> getChildrenRepresenting() {
        return wrapElements(children);
    }

    // getter/setter work around for CCD persisting only
    public void setChildrenRepresenting(List<Element<String>> childrenRepresenting) {
        children = unwrapElements(childrenRepresenting);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Representing")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<String>> childrenRepresenting;
  // ==== end synthesised definition-only fields ====
}
