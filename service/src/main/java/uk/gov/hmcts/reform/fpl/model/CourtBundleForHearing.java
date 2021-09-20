package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CourtBundleForHearing extends DocumentMetaData {
    private DocumentReference document;
    //private List<String> confidential;

    @JsonIgnore
    @Override
    public DocumentReference getTypeOfDocument() {
        return document;
    }

//    @JsonIgnore
//    public boolean isConfidentialDocument() {
//        return confidential != null && confidential.contains("CONFIDENTIAL");
//    }

//    @JsonGetter("confidentialTabLabel")
//    public String generateConfidentialTabLabel() {
//        return isConfidentialDocument() ? "Confidential" : null;
//    }
}
