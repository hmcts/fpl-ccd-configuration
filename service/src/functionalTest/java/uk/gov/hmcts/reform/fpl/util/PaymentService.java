package uk.gov.hmcts.reform.fpl.util;

import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.model.payment.Payment;
import uk.gov.hmcts.reform.fnp.model.payment.Payments;
import uk.gov.hmcts.reform.fpl.model.User;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentService {

    @Autowired
    private AuthenticationService authenticationService;

    public Payments getPayments(Long caseId, User user) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .when()
            .get("/testing-support/case/" + caseId + "/payments")
            .then()
            .statusCode(200)
            .and()
            .extract()
            .as(Payments.class);
    }

    public List<Payment> pollPayments(Long caseId, User user, Predicate<Payment> paymentPredicate) {
        return pollPayment(caseId, user, emails -> emails.stream().anyMatch(paymentPredicate)).stream()
            .filter(paymentPredicate)
            .collect(Collectors.toList());
    }

    public List<Payment> pollPayments(Long caseId, User user) {
        return pollPayments(caseId, user, ObjectUtils::isNotEmpty);
    }

    public List<Payment> pollPayments(Long caseId, User user, int amount) {
        return pollPayment(caseId, user, emails -> emails.size() > amount);
    }

    private List<Payment> pollPayment(Long caseId, User user, Predicate<List<Payment>> emails) {
        return await()
            .pollDelay(1, TimeUnit.SECONDS)
            .pollInterval(10, TimeUnit.SECONDS)
            .atMost(1, TimeUnit.MINUTES)
            .until(() -> getPayments(caseId, user).getPayments(), emails);
    }


}
