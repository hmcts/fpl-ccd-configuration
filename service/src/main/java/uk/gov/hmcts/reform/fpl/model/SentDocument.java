package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DocumentSentToParty", generate = true)
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentDocument {
    @CCD(label = "Party name", showCondition = "document=\"DO NOT SHOW\"")
    String partyName;
    @CCD(label = "File", categoryID = "documentsSentToParties", typeOverride = FieldType.Document)
    DocumentReference document;
    @CCD(label = "File", categoryID = "documentsSentToParties", typeOverride = FieldType.Document)
    DocumentReference coversheet;
    @CCD(label = "Date and time sent")
    String sentAt;
    @CCD(label = "ID from Send Letter Service")
    String letterId;
    @CCD(label = "Reason for removal")
    String removalReason;
}
