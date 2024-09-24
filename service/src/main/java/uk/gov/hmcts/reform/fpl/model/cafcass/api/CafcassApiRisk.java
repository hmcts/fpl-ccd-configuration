package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiRisk {
    private List<String> neglectOccurrences;
    private List<String> sexualAbuseOccurrences;
    private List<String> physicalHarmOccurrences;
    private List<String> emotionalHarmOccurrences;
}
