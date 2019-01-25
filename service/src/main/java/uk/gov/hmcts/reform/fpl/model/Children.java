package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Children {

    private Child firstChild;
    private List<AdditionalChild> additionalChildren;

    @JsonCreator
    public Children(@JsonProperty("firstChild") final Child firstChild,
                    @JsonProperty("additionalChildren")final List<AdditionalChild> additionalChildren) {
        this.firstChild = firstChild;
        this.additionalChildren = additionalChildren;
    }

    public Child getFirstChild() {
        return firstChild;
    }

    public List<AdditionalChild> getAdditionalChildren() {
        return additionalChildren;
    }

}
