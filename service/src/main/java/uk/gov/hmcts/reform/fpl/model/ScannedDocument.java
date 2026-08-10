package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder(toBuilder = true)
public class ScannedDocument {
    @CCD(
            label = "Document type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ScannedDocumentType",
            typeParameterClass = ScannedDocumentType.class
    )
    private final String type;
    @CCD(label = "Document subtype")
    private final String subtype;
    @CCD(label = "Document url", typeOverride = FieldType.Document)
    private final DocumentReference url;
    @CCD(label = "Document control number")
    private final String controlNumber;
    @CCD(label = "File name")
    private final String fileName;
    @CCD(label = "Scanned date")
    private final LocalDateTime scannedDate;
    @CCD(label = "Delivery date")
    private final LocalDateTime deliveryDate;
    @CCD(label = "Exception record reference")
    private final String exceptionRecordReference;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonProperty("# Record Meta data")
  @CCD(label = "Scanned records", typeOverride = FieldType.Label)
  private String __Record_Meta_data;
  // ==== end synthesised definition-only fields ====
}
