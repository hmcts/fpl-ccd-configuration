package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Children {

    private final Child firstChild;
    private final List<AdditionalEntries<Child>> additionalChildren;

    @JsonCreator
    public Children(@JsonProperty("firstChild") Child firstChild,
                    @JsonProperty("additionalChildren") List<AdditionalEntries<Child>> additionalChildren) {
        this.firstChild = firstChild;
        this.additionalChildren = additionalChildren;
    }

    public Children(Child firstChild, Child... additionalChildren) {
        this(firstChild, Arrays.stream(additionalChildren)
            .map(child -> new AdditionalEntries<>(UUID.randomUUID(), child))
            .collect(Collectors.toList()));
    }

    public List<Child> getAllChildren() {
        List<Child> allChildren = new ArrayList<>();
        allChildren.add(firstChild);
        additionalChildren.stream().forEach(additionalEntry -> {
            allChildren.add(additionalEntry.getValue());
        });
        return allChildren;
    }

}
