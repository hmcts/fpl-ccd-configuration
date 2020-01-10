package uk.gov.hmcts.reform.fpl.model;

import ccd.sdk.types.ComplexType;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@ComplexType(name = "ChildrenNew")
public class Child implements ConfidentialParty {
    @Valid
    @NotNull(message = "You need to add details to children")
    private final ChildParty party;

    public boolean containsConfidentialDetails() {
        String hiddenValue = defaultIfNull(party.getDetailsHidden(), "");

        return hiddenValue.equals("Yes");
    }
}
