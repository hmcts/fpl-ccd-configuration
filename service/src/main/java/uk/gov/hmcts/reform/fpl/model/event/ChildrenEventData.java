package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;

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
}
