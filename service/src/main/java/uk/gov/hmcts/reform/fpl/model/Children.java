package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Size;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Children {
    private final Child firstChild;
    private final List<Element<Child>> additionalChildren;
    
    @JsonCreator
    public Children(@JsonProperty("firstChild") Child firstChild,
                    @JsonProperty("additionalChildren") List<Element<Child>> additionalChildren) {
        this.firstChild = firstChild;
        this.additionalChildren = additionalChildren;
    }

    public Children(Child firstChild, Child... additionalChildren) {
        this(firstChild, Arrays.stream(additionalChildren)
            .map(child -> new Element<>(UUID.randomUUID(), child))
            .collect(Collectors.toList()));
    }

    @JsonIgnore
    @Size(min = 1, message = "Tell us the names of all children in the case")
    public List<@Valid Child> getAllChildren() {
        ImmutableList.Builder<Child> builder = ImmutableList.builder();
        if (firstChild != null) {
            builder.add(firstChild);
        }
        if (additionalChildren != null) {
            builder.addAll(additionalChildren.stream().map(Element::getValue).collect(Collectors.toList()));
        }
        return builder.build();
    }
}
