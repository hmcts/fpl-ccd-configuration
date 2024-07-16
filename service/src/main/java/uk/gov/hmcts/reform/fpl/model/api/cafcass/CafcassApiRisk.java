package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CafcassApiRisk {
    private List<String> neglectOccurrences;
    private List<String> sexualAbuseOccurrences;
    private List<String> physicalHarmOccurrences;
    private List<String> emotionalHarmOccurrences;
}
