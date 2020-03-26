package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class Respondent implements Representable, ConfidentialParty<Respondent> {
    @Valid
    @NotNull(message = "You need to add details to respondents")
    private final RespondentParty party;
    private final String leadRespondentIndicator;
    private final List<Element<UUID>> representedBy = new ArrayList<>();

    public void addRepresentative(UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            this.representedBy.add(element(representativeId));
        }
    }

    public boolean containsConfidentialDetails() {
        String hiddenValue = defaultIfNull(party.getContactDetailsHidden(), "");

        return hiddenValue.equals("Yes");
    }

    @JsonIgnore
    @Override
    public Party getConfidentialParty() {
        return party;
    }

    @JsonIgnore
    @Override
    public Respondent cloneWithConfidentialParty(Party party) {
        return this.toBuilder()
            .party(RespondentParty.builder()
                .firstName(party.firstName)
                .lastName(party.lastName)
                .address(party.address)
                .telephoneNumber(party.telephoneNumber)
                .email(party.email)
                .build())
            .build();
    }

    @Override
    public Respondent cloneWithFullParty(Party party) {
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


}
