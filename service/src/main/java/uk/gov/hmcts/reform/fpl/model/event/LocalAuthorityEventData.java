package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.addMissingIds;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class LocalAuthorityEventData {

    LocalAuthority localAuthority;
    List<Element<Colleague>> localAuthorityColleagues;
    DynamicList localAuthorityColleaguesList;
    String localAuthorityMainContactShown;

    @JsonIgnore
    public LocalAuthority combined() {
        return localAuthority.toBuilder().colleagues(localAuthorityColleagues).build();
    }

    @JsonIgnore
    public List<String> getColleaguesEmails() {
        if (isEmpty(localAuthorityColleagues)) {
            return emptyList();
        }
        return localAuthorityColleagues.stream()
            .map(Element::getValue)
            .map(Colleague::getEmail)
            .collect(Collectors.toList());
    }

    public void setLocalAuthorityColleagues(List<Element<Colleague>> localAuthorityColleagues) {
        this.localAuthorityColleagues = localAuthorityColleagues;
    }

    @JsonIgnore
    public DynamicList buildLocalAuthorityColleaguesList() {
        UUID mainContact = getMainContact();
        return asDynamicList(addMissingIds(localAuthorityColleagues), mainContact, Colleague::getFullName);
    }

    @JsonIgnore
    public UUID getMainContact() {
        if (isEmpty(localAuthorityColleagues)) {
            return null;
        }
        return localAuthorityColleagues.stream()
            .filter(x -> Objects.equals(x.getValue().getMainContact(), "Yes"))
            .map(Element::getId)
            .findFirst()
            .orElse(null);
    }

    @JsonIgnore
    public void setMainContact(UUID mainContactId) {
        localAuthorityColleagues.forEach(x -> {
            x.getValue().setMainContact(YesNo.from(Objects.equals(x.getId(), mainContactId)).getValue());
        });

        System.out.println("UPDATED " + localAuthorityColleagues);
    }

}
