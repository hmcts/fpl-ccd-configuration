package uk.gov.hmcts.reform.fpl.util;

import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Email;
import uk.gov.hmcts.reform.fpl.model.Emails;
import uk.gov.hmcts.reform.fpl.model.User;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailService {

    private final AuthenticationService authenticationService;

    public List<Email> pollEmail(Long caseId, User user, Predicate<Email> emailPredicate) {
        return pollEmails(caseId, user, emails -> emails.stream().anyMatch(emailPredicate)).stream()
            .filter(emailPredicate)
            .collect(Collectors.toList());
    }

    public List<Email> pollEmails(Long caseId, User user) {
        return pollEmails(caseId, user, ObjectUtils::isNotEmpty);
    }

    public List<Email> pollEmails(Long caseId, User user, int amount) {
        return pollEmails(caseId, user, emails -> emails.size() > amount);
    }

    private List<Email> pollEmails(Long caseId, User user, Predicate<List<Email>> emails) {
        return await()
            .pollDelay(1, TimeUnit.SECONDS)
            .pollInterval(10, TimeUnit.SECONDS)
            .atMost(1, TimeUnit.MINUTES)
            .until(() -> getEmails(caseId, user).getEmails(), emails);
    }

    private Emails getEmails(Long caseId, User user) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .when()
            .get("/testing-support/case/" + caseId + "/emails")
            .then()
            .statusCode(200)
            .and()
            .extract()
            .as(Emails.class);
    }
}
