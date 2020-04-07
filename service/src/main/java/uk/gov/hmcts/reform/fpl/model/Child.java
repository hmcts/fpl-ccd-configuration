package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.UUID.randomUUID;
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

    @Override
    public Party toParty() {
        return party;
    }

    @Override
    public Child extractConfidentialDetails() {
        return this.toBuilder()
            .party(ChildParty.builder()
                .firstName(this.party.firstName)
                .lastName(this.party.lastName)
                .address(this.party.address)
                .telephoneNumber(this.party.telephoneNumber)
                .email(this.party.email)
                .showAddressInConfidentialTab("Yes")
                .build())
            .build();
    }

    @Override
    public Child addConfidentialDetails(Party party) {
        return this.toBuilder()
            .party(this.getParty().toBuilder()
                .firstName(party.firstName)
                .lastName(party.lastName)
                .address(party.address)
                .telephoneNumber(party.telephoneNumber)
                .email(party.email)
                .build())
            .build();
    }

    @Override
    public Child removeConfidentialDetails() {
        return this.toBuilder()
            .party(this.party.toBuilder()
                .address(null)
                .telephoneNumber(null)
                .email(null)
                .build())
            .build();
    }

    public static Child expandCollection() {
        return Child.builder()
            .party(ChildParty.builder()
                .partyId(randomUUID().toString())
                .build())
            .build();
    }
}
