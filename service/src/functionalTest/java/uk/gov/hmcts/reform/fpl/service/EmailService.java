package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Email;
import uk.gov.hmcts.reform.fpl.model.Emails;
import uk.gov.hmcts.reform.fpl.model.User;

import java.util.List;
import java.util.function.Predicate;

import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.hmcts.reform.fpl.util.Poller.poll;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailService {

    private final AuthenticationService authenticationService;

    public List<Email> pollEmails(Long caseId, User user) {
        return pollEmails(caseId, user, ObjectUtils::isNotEmpty);
    }

    private List<Email> pollEmails(Long caseId, User user, Predicate<List<Email>> emails) {
        return poll(() -> getEmails(caseId, user).getEmails(), emails);
    }

    private Emails getEmails(Long caseId, User user) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .get("/testing-support/case/" + caseId + "/emails")
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(Emails.class);
    }
}
