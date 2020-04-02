package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Other implements Representable, ConfidentialParty<Other> {
    @SuppressWarnings("membername")
    @JsonProperty("DOB")
    private final String DOB;
    private final String name;
    private final String gender;
    private final Address address;
    private final String telephone;
    private final String birthPlace;
    private final String childInformation;
    private final String genderIdentification;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final List<Element<UUID>> representedBy = new ArrayList<>();

    public void addRepresentative(UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            this.representedBy.add(element(representativeId));
        }
    }

    public boolean containsConfidentialDetails() {
        return "Yes".equals(detailsHidden);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class OtherParty extends Party {

        @Builder
        private OtherParty(String partyId,
                           PartyType partyType,
                           String firstName,
                           String lastName,
                           String organisationName,
                           LocalDate dateOfBirth,
                           Address address,
                           EmailAddress email,
                           Telephone telephoneNumber) {
            super(partyId, partyType, firstName, lastName, organisationName, dateOfBirth, address, email,
                telephoneNumber);
        }
    }

    @JsonIgnore
    @Override
    public Party getConfidentialParty() {
        return OtherParty.builder()
            .firstName(this.getName())
            .address(this.getAddress())
            .telephoneNumber(Telephone.builder().telephoneNumber(this.telephone).build())
            .build();
    }

    @JsonIgnore
    @Override
    public Other getConfidentialDetails() {
        return Other.builder()
            .name(this.name)
            .address(this.address)
            .telephone(this.telephone)
            .build();
    }

    @JsonIgnore
    @Override
    public Other addConfidentialDetails(Party party) {
        Telephone telephone = ofNullable(party.telephoneNumber).orElse(Telephone.builder().build());

        return this.toBuilder()
            .name(party.firstName)
            .address(party.address)
            .telephone(ofNullable(telephone.getTelephoneNumber()).orElse(""))
            .build();
    }

    @JsonIgnore
    @Override
    public Other removeConfidentialDetails() {
        return this.toBuilder()
            .address(null)
            .telephone(null)
            .build();
    }
}
