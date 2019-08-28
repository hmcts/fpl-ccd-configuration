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

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldChildren {

    private final OldChild firstChild;
    private final List<Element<OldChild>> additionalChildren;

    @JsonCreator
    public OldChildren(@JsonProperty("firstChild") OldChild firstChild,
                       @JsonProperty("additionalChildren") List<Element<OldChild>> additionalChildren) {
        this.firstChild = firstChild;
        this.additionalChildren = additionalChildren;
    }

    public OldChildren(OldChild firstChild, OldChild... additionalChildren) {
        this(firstChild, Arrays.stream(additionalChildren)
            .map(oldChild -> new Element<>(UUID.randomUUID(), oldChild))
            .collect(Collectors.toList()));
    }

    @JsonIgnore
    public List<OldChild> getAllChildren() {
        ImmutableList.Builder<OldChild> builder = ImmutableList.builder();
        if (firstChild != null) {
            builder.add(firstChild);
        }
        if (additionalChildren != null) {
            builder.addAll(additionalChildren.stream().map(Element::getValue).collect(Collectors.toList()));
        }
        return builder.build();
    }
}
