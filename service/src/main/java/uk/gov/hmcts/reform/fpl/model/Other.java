package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Other implements Representable, ConfidentialParty {
    @SuppressWarnings("membername")
    private final String DOB;
    private final String name;
    private final String gender;
    private final Address address;
    private final String telephone;
    private final String birthplace;
    private final String childInformation;
    private final String litigationIssues;
    private final String genderIdentification;
    private final String detailsHidden;
    private final List<Element<UUID>> representedBy = new ArrayList<>();

    public void addRepresentative(UUID representativeId) {
        if (!unwrapElements(representedBy).contains(representativeId)) {
            this.representedBy.add(element(representativeId));
        }
    }

    public boolean containsConfidentialDetails() {
        String hiddenValue = defaultIfNull(getDetailsHidden(), "");

        return hiddenValue.equals("Yes");
    }
}
