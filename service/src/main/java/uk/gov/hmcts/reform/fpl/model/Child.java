package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.ChildParty.ChildPartyBuilder;
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
        String hiddenValue = defaultIfNull(party.getIsAddressConfidential(), "");
        String hiddenSocialWorker = defaultIfNull(party.getSocialWorkerDetailsHidden(), "");

        return YesNo.YES.getValue().equals(hiddenValue) || YesNo.YES.getValue().equals(hiddenSocialWorker);
    }

    @Override
    public Party toParty() {
        return party;
    }

    @Override
    public Child extractConfidentialDetails() {
        ChildPartyBuilder childPartyBuilder = ChildParty.builder()
            .livingSituation(this.party.getLivingSituation())
            .livingSituationDetails(this.party.getLivingSituationDetails())
            .firstName(this.party.getFirstName())
            .lastName(this.party.getLastName())
            .telephoneNumber(this.party.getTelephoneNumber())
            .email(this.party.getEmail()) // legacy behaviour, always hide email if present (no longer entered)
            .isAddressConfidential(this.party.getIsAddressConfidential())
            .socialWorkerDetailsHidden(this.party.getSocialWorkerDetailsHidden())
            .socialWorkerDetailsHiddenReason(this.party.getSocialWorkerDetailsHiddenReason());

        if (YesNo.YES.equalsString(this.party.getIsAddressConfidential())) {
            childPartyBuilder = childPartyBuilder.address(this.party.getAddress());
        }

        if (YesNo.YES.equalsString(this.party.getSocialWorkerDetailsHidden())) {
            childPartyBuilder = childPartyBuilder.socialWorkerName(this.party.getSocialWorkerName())
                .socialWorkerEmail(this.party.getSocialWorkerEmail())
                .socialWorkerTelephoneNumber(this.party.getSocialWorkerTelephoneNumber());
        }

        return this.toBuilder()
            .party(childPartyBuilder.build())
            .build();
    }

    @Override
    public Child addConfidentialDetails(Party party) {
        ChildPartyBuilder childPartyBuilder = this.getParty().toBuilder()
            .firstName(party.getFirstName())
            .lastName(party.getLastName())
            .telephoneNumber(party.getTelephoneNumber())
            .email(party.getEmail()); // legacy behaviour, always hide email if present (no longer entered)

        // Do not nullify old data that may not have been moved over prior to DFPL-2639
        if (!isEmpty(((ChildParty) party).getLivingSituation())) {
            childPartyBuilder.livingSituation(((ChildParty) party).getLivingSituation());
        }

        if (!isEmpty(((ChildParty) party).getLivingSituationDetails())) {
            childPartyBuilder.livingSituationDetails(((ChildParty) party).getLivingSituationDetails());
        }

        if (YesNo.YES.equalsString(this.party.getIsAddressConfidential())) {
            childPartyBuilder = childPartyBuilder.address(party.getAddress());
        }

        if (YesNo.YES.equalsString(this.party.getSocialWorkerDetailsHidden())) {
            childPartyBuilder = childPartyBuilder.socialWorkerName(this.party.getSocialWorkerName())
                .socialWorkerEmail(this.party.getSocialWorkerEmail())
                .socialWorkerTelephoneNumber(this.party.getSocialWorkerTelephoneNumber());
        }

        return this.toBuilder()
            .party(childPartyBuilder.build())
            .build();
    }

    @Override
    public Child removeConfidentialDetails() {
        ChildPartyBuilder childPartyBuilder = this.party.toBuilder();
        childPartyBuilder.email(null); // legacy behaviour, always hide email if present (no longer entered)

        if (YesNo.YES.equalsString(this.party.getIsAddressConfidential())) {
            childPartyBuilder = childPartyBuilder.address(null);
        }

        if (YesNo.YES.equalsString(this.party.getSocialWorkerDetailsHidden())) {
            childPartyBuilder = childPartyBuilder.socialWorkerName(null)
                .socialWorkerEmail(null)
                .socialWorkerTelephoneNumber(null);
        }

        return this.toBuilder()
            .party(childPartyBuilder.build())
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
