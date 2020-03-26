package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @JsonIgnore
    @Override
    public Party getConfidentialParty() {
        return new Party(null, null, name, null,
            null, null, address, null,
            Telephone.builder().telephoneNumber(telephone).build());
    }

    @JsonIgnore
    @Override
    public Other cloneWithConfidentialParty(Party party) {
        if (party.telephoneNumber == null) {
            throw new IllegalArgumentException("telephone number can't be null");
        }

        return this.toBuilder()
            .address(party.address)
            .name(party.firstName)
            .telephone(party.telephoneNumber.getTelephoneNumber())
            .build();
    }

    @Override
    public Other cloneWithFullParty(Party party) {
        return cloneWithConfidentialParty(party);
    }
}
