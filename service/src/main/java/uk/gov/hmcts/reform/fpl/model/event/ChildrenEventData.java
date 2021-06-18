package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChildrenEventData {
    // page 2
    String childrenHaveRepresentation;
    RespondentSolicitor childrenMainRepresentative;

    // page 3
    String childrenHaveSameRepresentation;
    ChildRepresentationDetails childRepresentationDetails0;
    ChildRepresentationDetails childRepresentationDetails1;
    ChildRepresentationDetails childRepresentationDetails2;
    ChildRepresentationDetails childRepresentationDetails3;
    ChildRepresentationDetails childRepresentationDetails4;
    ChildRepresentationDetails childRepresentationDetails5;
    ChildRepresentationDetails childRepresentationDetails6;
    ChildRepresentationDetails childRepresentationDetails7;
    ChildRepresentationDetails childRepresentationDetails8;
    ChildRepresentationDetails childRepresentationDetails9;
    ChildRepresentationDetails childRepresentationDetails10;
    ChildRepresentationDetails childRepresentationDetails11;
    ChildRepresentationDetails childRepresentationDetails12;
    ChildRepresentationDetails childRepresentationDetails13;
    ChildRepresentationDetails childRepresentationDetails14;

    @JsonIgnore
    public List<ChildRepresentationDetails> getAllRepresentationDetails() {
        // mutable to allow null values
        List<ChildRepresentationDetails> childRepresentationDetails = new ArrayList<>();
        childRepresentationDetails.add(childRepresentationDetails0);
        childRepresentationDetails.add(childRepresentationDetails1);
        childRepresentationDetails.add(childRepresentationDetails2);
        childRepresentationDetails.add(childRepresentationDetails3);
        childRepresentationDetails.add(childRepresentationDetails4);
        childRepresentationDetails.add(childRepresentationDetails5);
        childRepresentationDetails.add(childRepresentationDetails6);
        childRepresentationDetails.add(childRepresentationDetails7);
        childRepresentationDetails.add(childRepresentationDetails8);
        childRepresentationDetails.add(childRepresentationDetails9);
        childRepresentationDetails.add(childRepresentationDetails10);
        childRepresentationDetails.add(childRepresentationDetails11);
        childRepresentationDetails.add(childRepresentationDetails12);
        childRepresentationDetails.add(childRepresentationDetails13);
        childRepresentationDetails.add(childRepresentationDetails14);
        return childRepresentationDetails;
    }
}
