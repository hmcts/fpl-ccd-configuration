package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.UploaderInfo;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.DocumentAcknowledge;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "C2DraftOrder", generate = true)
@Data
@Builder(toBuilder = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DraftOrder implements UploaderInfo {
    @CCD(label = "Document name")
    private String title;
    @CCD(label = "File", categoryID = "c2Applications", typeOverride = FieldType.Document)
    private DocumentReference document;
    @CCD(ignore = true)
    private LocalDate dateUploaded;
    @CCD(
            label = "Document Uploader Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DocumentUploaderType"
    )
    private DocumentUploaderType uploaderType;
    @CCD(label = "Document Uploader Case Roles", searchable = false)
    private List<CaseRole> uploaderCaseRoles;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Date and time uploaded")
  private java.time.LocalDateTime dateTimeUploaded;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
  private java.util.Set<DocumentAcknowledge> documentAcknowledge;
  // ==== end synthesised definition-only fields ====
}
