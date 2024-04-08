package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingCourtBundle {
    private String hearing;
    private List<Element<CourtBundle>> courtBundle;

    public List<Element<CourtBundle>> getCourtBundle() {
        return defaultIfNull(this.courtBundle, new ArrayList<>());
    }

}
