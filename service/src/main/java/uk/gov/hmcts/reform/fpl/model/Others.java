package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Others {
    private final Other firstOther;
    private final List<Element<Other>> additionalOthers;

    @JsonCreator
    public Others(@JsonProperty("firstOther") Other firstOther,
                    @JsonProperty("additionalOthers") List<Element<Other>> additionalOthers) {
        this.firstOther = firstOther;
        this.additionalOthers = additionalOthers;
    }
}
