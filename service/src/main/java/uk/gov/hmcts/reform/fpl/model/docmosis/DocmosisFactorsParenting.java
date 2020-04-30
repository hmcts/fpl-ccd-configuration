package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisFactorsParenting {
    private final String anythingElse;
    private final String alcoholDrugAbuseDetails;
    private final String domesticViolenceDetails;
}
