package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;

import static java.util.Optional.ofNullable;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class Colleague {

    private final ColleagueRole role;
    private final String title;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String dx;
    private final String reference;
    private final String notificationRecipient;
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
}
