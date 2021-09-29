package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementConfidentialDocument {
    private Type type;
    private DocumentReference document;
    private String description;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        ANNEX_B("Annex B"),
        GUARDIANS_REPORT("Guardian's report"),
        OTHER_CONFIDENTIAL_DOCUMENTS("Other confidential documents");

        private final String name;
    }

}
