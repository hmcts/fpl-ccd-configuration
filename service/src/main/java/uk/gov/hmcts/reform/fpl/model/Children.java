package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Children {

    private Child firstChild;
    private List<AdditionalChild> additionalChildren;

    @JsonCreator
    public Children(@JsonProperty("firstChild") Child firstChild,
                    @JsonProperty("additionalChildren") List<AdditionalChild> additionalChildren) {
        this.firstChild = firstChild;
        this.additionalChildren = additionalChildren;
    }

    public Children(Child firstChild, Child... additionalChildren) {
        this(firstChild, Arrays.stream(additionalChildren)
            .map(child -> new AdditionalChild(UUID.randomUUID(), child))
            .collect(Collectors.toList()));
    }

    public Child getFirstChild() {
        return firstChild;
    }

    public List<AdditionalChild> getAdditionalChildren() {
        return additionalChildren;
    }

}
