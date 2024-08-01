package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CafcassApiRisk {
    private List<String> neglectOccurrences;
    private List<String> sexualAbuseOccurrences;
    private List<String> physicalHarmOccurrences;
    private List<String> emotionalHarmOccurrences;
}
