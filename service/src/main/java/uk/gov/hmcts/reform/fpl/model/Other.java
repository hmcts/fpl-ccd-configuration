package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.enums.AddressNotKnowReason;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Other implements Representable, ConfidentialParty<Other> {
    @JsonProperty("DOB")
    private final String dateOfBirth;
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
    private List<Element<UUID>> representedBy;
    private final String addressNotKnowReason;
    private final String addressKnow;

    public List<Element<UUID>> getRepresentedBy() {
        if (this.representedBy == null) {
            this.representedBy = new ArrayList<>();
        }
        return this.representedBy;
    }

    public void addRepresentative(UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            if (this.representedBy == null) {
                this.representedBy = new ArrayList<>();
            }
            this.representedBy.add(element(representativeId));
        }
    }

    public void addRepresentative(UUID id, UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            if (this.representedBy == null) {
                this.representedBy = new ArrayList<>();
            }
            this.representedBy.add(element(id, representativeId));
        }
    }

    public boolean containsConfidentialDetails() {
        return "Yes".equals(detailsHidden);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class OtherParty extends Party {

        //sonarqube complaining about 9 params in constructor. Should be no more than 7.
        // need to have constructor here to allow inheritance from Party. Will be address in others data migration.
        @SuppressWarnings("squid:S00107")
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

    @Override
    public Party toParty() {
        return OtherParty.builder()
            .firstName(this.getName())
            .address(this.getAddress())
            .dateOfBirth(nonNull(this.getDateOfBirth()) ? LocalDate.parse(this.getDateOfBirth(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null)
            .telephoneNumber(Telephone.builder().telephoneNumber(this.telephone).build())
            .build();
    }

    @Override
    public Other extractConfidentialDetails() {
        return Other.builder()
            .name(this.name)
            .address(this.address)
            .telephone(this.telephone)
            .build();
    }

    @Override
    public Other addConfidentialDetails(Party party) {
        return null;
    }

    @Override
    public Other removeConfidentialDetails() {
        Other other =  this.toBuilder()
            .address(null)
            .telephone(null)
            .build();
        return other;
    }

    @JsonIgnore
    public boolean hasAddressAdded() {
        return !isNull(getAddress()) && !ObjectUtils.isEmpty(getAddress().getPostcode());
    }

    @JsonIgnore
    public boolean isRepresented() {
        return !ObjectUtils.isEmpty(getRepresentedBy());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return Stream.of(dateOfBirth, name, gender, telephone, birthPlace, childInformation, genderIdentification,
            litigationIssues, litigationIssuesDetails, detailsHidden, detailsHiddenReason, representedBy,
            addressNotKnowReason
        ).allMatch(ObjectUtils::isEmpty)
            && (isNull(address) || address.equals(Address.builder().build()));
    }

    @JsonIgnore
    public boolean isDeceasedOrNFA() {
        return AddressNotKnowReason.DECEASED.getType().equals(addressNotKnowReason)
            || AddressNotKnowReason.NO_FIXED_ABODE.getType().equals(addressNotKnowReason);
    }
}
