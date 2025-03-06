package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Colleague {

    private final ColleagueRole role;
    private String title;
    @Deprecated
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String alternativePhone;
    private String dx;
    private String reference;
    private String notificationRecipient;
    private String mainContact;

    @JsonIgnore
    public String getJobTitle() {
        if (role == ColleagueRole.OTHER) {
            return title;
        }
        return ofNullable(role)
            .map(ColleagueRole::getLabel)
            .orElse(null);
    }

    @JsonIgnore
    public String buildFullName() {
        return (isNotEmpty(firstName) && isNotEmpty(lastName))
            ? StringUtils.joinWith(" ", firstName, lastName) : fullName;
    }

    @JsonIgnore
    // cannot name this method as isMainContact as it will override the default getter method when serialising
    public boolean checkIfMainContact() {
        return YES.getValue().equals(mainContact);
    }
}
