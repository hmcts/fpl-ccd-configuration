package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Child implements WithSolicitor, ConfidentialParty<Child> {
    @Valid
    @NotNull(message = "You need to add details to children")
    private final ChildParty party;

    private String finalOrderIssued;
    private String finalOrderIssuedType;
    private String finalDecisionReason;
    private String finalDecisionDate;

    private RespondentSolicitor solicitor;
    private List<Element<LegalCounsellor>> legalCounsellors;

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
                .livingSituation(this.party.getLivingSituation())
                .livingSituationDetails(this.party.getLivingSituationDetails())
                .firstName(this.party.getFirstName())
                .lastName(this.party.getLastName())
                .address(this.party.getAddress())
                .telephoneNumber(this.party.getTelephoneNumber())
                .email(this.party.getEmail())
                .showAddressInConfidentialTab("Yes")
                .build())
            .build();
    }

    @Override
    public Child addConfidentialDetails(Party party) {
        ChildParty.ChildPartyBuilder partyBuilder = this.getParty().toBuilder()
            .firstName(party.getFirstName())
            .lastName(party.getLastName())
            .address(party.getAddress())
            .telephoneNumber(party.getTelephoneNumber())
            .email(party.getEmail());

        // Do not nullify old data that may not have been moved over prior to DFPL-2639
        if (!isEmpty(((ChildParty) party).getLivingSituation())) {
            partyBuilder.livingSituation(((ChildParty) party).getLivingSituation());
        }

        if (!isEmpty(((ChildParty) party).getLivingSituationDetails())) {
            partyBuilder.livingSituationDetails(((ChildParty) party).getLivingSituationDetails());
        }

        return this.toBuilder()
            .party(partyBuilder.build())
            .build();
    }

    @Override
    public Child removeConfidentialDetails() {
        return this.toBuilder()
            .party(this.party.toBuilder()
                .livingSituation(null)
                .livingSituationDetails(null)
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

    public String asLabel() {
        return this.getParty().getFullName();
    }
}
