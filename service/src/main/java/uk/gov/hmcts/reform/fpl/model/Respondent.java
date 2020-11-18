package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class Respondent implements Representable, ConfidentialParty<Respondent> {
    @Valid
    @NotNull(message = "You need to add details to respondents")
    private final RespondentParty party;
    private final String leadRespondentIndicator;
    @Deprecated(since = "FPLA-2428")
    private String persistRepresentedBy;
    @Builder.Default
    private List<Element<UUID>> representedBy = new ArrayList<>();

    public void addRepresentative(UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            this.representedBy.add(element(representativeId));
        }
    }

    public boolean containsConfidentialDetails() {
        String hiddenValue = defaultIfNull(party.getContactDetailsHidden(), "");

        return hiddenValue.equals("Yes");
    }

    @Override
    public Party toParty() {
        return party;
    }

    @Override
    public Respondent extractConfidentialDetails() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(this.party.getFirstName())
                .lastName(this.party.getLastName())
                .address(this.party.getAddress())
                .telephoneNumber(this.party.getTelephoneNumber())
                .email(this.party.getEmail())
                .build())
            .build();
    }

    @Override
    public Respondent addConfidentialDetails(Party party) {
        return this.toBuilder()
            .party(this.getParty().toBuilder()
                .firstName(party.getFirstName())
                .lastName(party.getLastName())
                .address(party.getAddress())
                .telephoneNumber(party.getTelephoneNumber())
                .email(party.getEmail())
                .build())
            .build();
    }

    @Override
    public Respondent removeConfidentialDetails() {
        return this.toBuilder()
            .party(this.party.toBuilder()
                .address(null)
                .telephoneNumber(null)
                .email(null)
                .build())
            .build();
    }

    public static Respondent expandCollection() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .partyId(randomUUID().toString())
                .build())
            .build();
    }
}
