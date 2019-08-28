package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class Child {
    @Valid
    @NotNull(message = "You need to add details to children")
    private final ChildParty party;
}
