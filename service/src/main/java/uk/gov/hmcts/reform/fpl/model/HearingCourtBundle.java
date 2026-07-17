package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.CourtBundleV2;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingCourtBundle {
    @CCD(label = "Hearing")
    private String hearing;
    @CCD(label = "Documents", typeOverride = FieldType.Collection, typeParameterOverride = "CourtBundleV2")
    private List<Element<CourtBundle>> courtBundle;

    public List<Element<CourtBundle>> getCourtBundle() {
        return defaultIfNull(this.courtBundle, new ArrayList<>());
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Documents")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<CourtBundleV2>> courtBundleNC;
  // ==== end synthesised definition-only fields ====
}
