package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class Child implements ConfidentialParty<Child> {
    @Valid
    @NotNull(message = "You need to add details to children")
    private final ChildParty party;

    public boolean containsConfidentialDetails() {
        String hiddenValue = defaultIfNull(party.getDetailsHidden(), "");

        return hiddenValue.equals("Yes");
    }

    @JsonIgnore
    @Override
    public Party getConfidentialParty() {
        return party;
    }

    @JsonIgnore
    @Override
    public Child setConfidentialParty(Party party) {
        return this.toBuilder()
            .party(ChildParty.builder()
                .firstName(party.firstName)
                .lastName(party.lastName)
                .address(party.address)
                .telephoneNumber(party.telephoneNumber)
                .email(party.email)
                .build())
            .build();
    }
}
