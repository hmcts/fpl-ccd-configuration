package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Email;
import uk.gov.hmcts.reform.fpl.model.NewRespondent;
import uk.gov.hmcts.reform.fpl.model.Party;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.TelephoneNumber;

import java.util.UUID;

@Service
public class MigrationService {

    private String firstName;
    private String lastName;

    // Will be old case -> new case. For now oldRespondent -> newRespondent

    // Give method a Respondent object and it will return the new data structure
    public NewRespondent migrateRespondent(Respondent or) {
        if (or.getName() != null) {
            firstName = or.getName().split("\\s+")[0];
            lastName = or.getName().split("\\s+")[1];
        }

        return NewRespondent.builder()
            .party(Party.builder()
                .partyID(UUID.randomUUID().toString())
                .idamID("")
                .partyType("Individual")
                .title("")
                .firstName(firstName)
                .lastName(lastName)
                .organisationName("")
                .dateOfBirth(or.getDob())
                .address(or.getAddress())
                .email(Email.builder()
                    .email("")
                    .emailUsageType("")
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber(or.getTelephone())
                    .telephoneUsageType("")
                    .contactDirection("")
                    .build())
                .build())
            .leadRespondentIndicator("")
            .build();
    }
}
