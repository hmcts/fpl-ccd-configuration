package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.AddressNotKnowReason;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Respondent implements Representable, WithSolicitor, ConfidentialParty<Respondent> {
    @Valid
    @NotNull(message = "You need to add details to respondents")
    private final RespondentParty party;
    private final String leadRespondentIndicator;
    @Deprecated(since = "FPLA-2428")
    private String persistRepresentedBy;
    @Builder.Default
    private List<Element<UUID>> representedBy = new ArrayList<>();

    private String legalRepresentation;

    private RespondentSolicitor solicitor;
    private List<Element<LegalCounsellor>> legalCounsellors;

    public void addRepresentative(UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            this.representedBy.add(element(representativeId));
        }
    }

    @JsonIgnore
    public boolean hasAddress() {
        return isNotEmpty(party) && isNotEmpty(party.getAddress())
            && isNotEmpty(party.getAddress().getPostcode());
    }

    public boolean containsConfidentialDetails() {
        String hiddenValue = defaultIfNull(party.getContactDetailsHidden(), "");

        return hiddenValue.equalsIgnoreCase("Yes");
    }

    @Override
    public Party toParty() {
        return party;
    }

    @Override
    public Respondent extractConfidentialDetails() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .addressKnow(this.party.getAddressKnow())
                .firstName(this.party.getFirstName())
                .lastName(this.party.getLastName())
                .addressKnow(this.party.getAddressKnow())
                .address(this.party.getAddress())
                .telephoneNumber(this.party.getTelephoneNumber())
                .email(this.party.getEmail())
                .build())
            .build();
    }

    @Override
    public Respondent addConfidentialDetails(Party party) {
        RespondentParty.RespondentPartyBuilder partyBuilder = this.getParty().toBuilder()
            .firstName(party.getFirstName())
            .lastName(party.getLastName())
            .address(party.getAddress())
            .telephoneNumber(party.getTelephoneNumber())
            .email(party.getEmail());

        if (!isEmpty(((RespondentParty) party).getAddressKnow())) {
            partyBuilder.addressKnow(((RespondentParty) party).getAddressKnow());
        }

        return this.toBuilder()
            .party(partyBuilder.build())
            .build();
    }

    @Override
    public Respondent removeConfidentialDetails() {
        return this.toBuilder()
            .party(this.party.toBuilder()
                .addressKnow(null)
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

    @JsonIgnore
    public boolean isDeceasedOrNFA() {
        return isNotEmpty(party) && IsAddressKnowType.NO.equals(party.getAddressKnow())
               && (AddressNotKnowReason.DECEASED.getType().equals(party.getAddressNotKnowReason())
                        || AddressNotKnowReason.NO_FIXED_ABODE.getType().equals(party.getAddressNotKnowReason()));
    }
}
