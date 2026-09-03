package uk.gov.hmcts.reform.fpl.model.order.generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class GeneratedRefusalOrder extends GeneratedOrder {
    private final DocumentReference refusalDocument;
    private final DocumentReference refusalDocumentConfidential;

    @Override
    @JsonIgnore
    public boolean isConfidential() {
        return isNotEmpty(refusalDocumentConfidential);
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocumentOrDocumentConfidential() {
        return (isConfidential()) ? refusalDocumentConfidential : refusalDocument;
    }
}
