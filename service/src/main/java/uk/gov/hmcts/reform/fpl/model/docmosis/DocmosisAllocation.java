package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisAllocation {
    private final String proposal;
    private final String proposalReason;
}
