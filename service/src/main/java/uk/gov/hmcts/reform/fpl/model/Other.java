package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.enums.AddressNotKnowReason;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
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
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@Jacksonized
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

    // Flag for preventing from purging the converted old field value during deserialization if addressKnowV2 is null
    @Builder.Default
    @Getter(AccessLevel.NONE)
    private final boolean isConvertedAddressKnow = false;
    private final IsAddressKnowType addressKnowV2;

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

    public static class OtherBuilder {
        private boolean isConvertedAddressKnow = false;

        /** <h2>Deprecated. use addressKnowV2 instead</h2>
         * <h3>This builder method will convert the old addressKnow field to addressKnowV2 during deserialization</h3>
         * <p>Was having ElasticSearch initialisation exception during the release:
         * <br>
         * <i>ElasticSearch initialisation exception" mapper [data.hearingDetails.value.others.value.addressKnow]
         * cannot be changed from type [keyword] to [text]</i></p>
         * <p>Creating a new field to avoid updating the existing indexed field.
         * Review all indexed field in the future</p>
         * @see IsAddressKnowType
         **/
        @Deprecated(since = "DFPL-2546")
        public OtherBuilder addressKnow(String addressKnow) {
            if (this.addressKnowV2 == null && isNotEmpty(addressKnow)) {
                this.addressKnowV2 = (YES.getValue().equalsIgnoreCase(addressKnow))
                    ? IsAddressKnowType.YES : IsAddressKnowType.NO;
                this.isConvertedAddressKnow = true;
            }
            return this;
        }

        public OtherBuilder addressKnowV2(IsAddressKnowType addressKnowV2) {
            // Prevent from purging the converted old field value during deserialization if addressKnowV2 is null
            if (!isConvertedAddressKnow || addressKnowV2 != null) {
                this.addressKnowV2 = addressKnowV2;
                this.isConvertedAddressKnow = false;
            }
            return this;
        }
    }
}
