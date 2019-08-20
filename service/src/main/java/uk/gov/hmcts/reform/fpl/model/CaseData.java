package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseData {
    private final List<Element<Applicant>> applicants;

    //Single argument constructors do not play nice with @Data annotations. This will be fixed in future PR.
    @JsonCreator
    public CaseData(@JsonProperty("applicants") final List<Element<Applicant>> applicants) {
        this.applicants = applicants;
    }
}
