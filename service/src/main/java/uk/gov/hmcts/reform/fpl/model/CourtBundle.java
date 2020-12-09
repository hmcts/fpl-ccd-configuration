package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtBundle extends DocumentMetaData {
    private String hearing;
    private DocumentReference document;

    @JsonIgnore
    @Override
    public DocumentReference getTypeOfDocument() {
        return document;
    }
}
