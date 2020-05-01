package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DocmosisDocument {
    private final String documentTitle;
    private final byte[] bytes;

    @JsonIgnore
    public String getDraftDocumentTile() {
        return "draft-" + documentTitle;
    }
}
