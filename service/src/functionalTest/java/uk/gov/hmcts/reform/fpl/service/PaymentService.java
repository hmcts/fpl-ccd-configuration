package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.model.payment.Payment;
import uk.gov.hmcts.reform.fnp.model.payment.Payments;
import uk.gov.hmcts.reform.fpl.model.User;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.hmcts.reform.fpl.util.Poller.poll;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentService {

    private final AuthenticationService authenticationService;

    public List<Payment> pollPayments(Long caseId, User user) {
        return pollPayments(caseId, user, ObjectUtils::isNotEmpty);
    }

    private List<Payment> pollPayments(Long caseId, User user, Predicate<Payment> paymentPredicate) {
        return pollPayment(caseId, user, emails -> emails.stream().anyMatch(paymentPredicate)).stream()
            .filter(paymentPredicate)
            .collect(toList());
    }

    private List<Payment> pollPayment(Long caseId, User user, Predicate<List<Payment>> emails) {
        return poll(() -> getPayments(caseId, user).getPayments(), emails);
    }

    private Payments getPayments(Long caseId, User user) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .get("/testing-support/case/" + caseId + "/payments")
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(Payments.class);
    }
}
