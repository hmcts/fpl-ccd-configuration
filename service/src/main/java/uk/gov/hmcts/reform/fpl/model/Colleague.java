package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Colleague {

    private final ColleagueRole role;
    private String title;
    private String fullName;
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
    // cannot name this method as isMainContact as it will override the default getter method when serialising
    public boolean checkIfMainContact() {
        return YES.getValue().equals(mainContact);
    }
}
