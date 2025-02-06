package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    @Deprecated
    private final String name;
    private final String firstName;
    private final String lastName;

    @Deprecated
    private final String gender;
    private final Address address;
    private final String telephone;
    @Deprecated
    private final String birthPlace;
    private final String childInformation;
    @Deprecated
    private final String genderIdentification;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
    @Deprecated
    private final String detailsHidden;
    @Deprecated
    private final String detailsHiddenReason;
    @Deprecated
    private List<Element<UUID>> representedBy;
    private final String addressNotKnowReason;
    private final IsAddressKnowType addressKnowV2;
    private final String whereaboutsUnknownDetails;
    private final String hideAddress;
    private final String hideTelephone;

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
        return YES.getValue().equals(detailsHidden)
               || YES.getValue().equals(hideAddress)
               || YES.getValue().equals(hideTelephone);
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
            .firstName(ObjectUtils.isEmpty(this.getFirstName()) ? this.getName() : this.getFirstName())
            .lastName(this.getLastName())
            .address(this.getAddress())
            .dateOfBirth(nonNull(this.getDateOfBirth()) ? LocalDate.parse(this.getDateOfBirth(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null)
            .telephoneNumber(Telephone.builder().telephoneNumber(this.telephone).build())
            .build();
    }

    @Override
    public Other extractConfidentialDetails() {
        Other.OtherBuilder otherBuilder = Other.builder()
            .name(this.name) // legacy data
            .firstName(this.firstName)
            .lastName(this.lastName)
            .hideAddress(this.getHideAddress())
            .hideTelephone(this.getHideTelephone());

        if (YES.equalsString(this.detailsHidden) || YES.equalsString(this.getHideAddress())) {
            otherBuilder = otherBuilder.addressKnowV2(this.addressKnowV2).address(this.address);
        }

        if (YES.equalsString(this.detailsHidden) || YES.equalsString(this.getHideTelephone())) {
            otherBuilder = otherBuilder.telephone(this.telephone);
        }

        return otherBuilder.build();
    }

    @Override
    public Other addConfidentialDetails(Party party) {
        return null;
    }

    @Override
    public Other removeConfidentialDetails() {
        Other.OtherBuilder otherBuilder =  this.toBuilder();

        if (YES.equalsString(this.detailsHidden) || YES.equalsString(this.getHideAddress())) {
            otherBuilder = otherBuilder.addressKnowV2(null).address(null);
        }

        if (YES.equalsString(this.detailsHidden) || YES.equalsString(this.getHideTelephone())) {
            otherBuilder = otherBuilder.telephone(null);
        }

        return otherBuilder.build();
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
        // Flag for preventing from purging the converted old field value during deserialization
        // if addressKnowV2 is null
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
            if (!this.isConvertedAddressKnow || addressKnowV2 != null) {
                this.addressKnowV2 = addressKnowV2;
                this.isConvertedAddressKnow = false;
            }
            return this;
        }
    }
}
