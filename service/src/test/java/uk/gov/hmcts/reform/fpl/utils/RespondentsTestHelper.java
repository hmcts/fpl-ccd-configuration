package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class RespondentsTestHelper {

    private static final Organisation TEST_ORGANISATION = Organisation.organisation(UUID.randomUUID().toString());

    private RespondentsTestHelper() {
    }

    public static List<Element<Respondent>> respondents() {
        return wrapElements(
            respondent("James", "Daniels"),
            respondent("Bob", "Martyn"),
            respondent("Rachel", "Daniels")
        );
    }

    public static Respondent respondent(String firstName, String lastName) {
        return respondent(firstName, lastName, null);
    }

    public static Respondent respondent(String firstName, String lastName, List<Element<UUID>> representedBy) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .representedBy(representedBy)
            .build();
    }

    public static Respondent respondentWithSolicitor() {
        return respondentWithSolicitor(LocalDate.now(), UUID.randomUUID() + "@example.com");
    }

    public static Respondent respondentWithSolicitor(LocalDate respondentDateOfBirth, String solicitorEmail) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(respondentDateOfBirth)
                .build())
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email(solicitorEmail)
                .organisation(TEST_ORGANISATION)
                .build())
            .build();
    }

}
