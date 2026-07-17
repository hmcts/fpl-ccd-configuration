package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class DraftOrderUrgencyOption {
    @CCD(label = " ", typeOverride = FieldType.MultiSelectList, typeParameterOverride = "DraftOrderApprovalUrgency")
    private final List<YesNo> urgency;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<ul><li>Interim Care Order</li><li>Emergency Protection Order</li><li>Exclusion Order</li><li>Recovery Order</li><li>Child Assessment Order</li><li>Port Alert Order</li><li>Passport Order</li><li>Non-Molestation Order</li><li>Occupation Order</li></ul>If the order does not contain one of the orders above, leave blank and continue. ",
          typeOverride = FieldType.Label
  )
  private String urgencyLabel;
  // ==== end synthesised definition-only fields ====
}
