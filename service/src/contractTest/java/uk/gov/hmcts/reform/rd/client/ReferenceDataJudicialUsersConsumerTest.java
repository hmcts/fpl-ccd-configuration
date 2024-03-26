package uk.gov.hmcts.reform.rd.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

@PactTestFor(providerName = "referenceData_judicialv2", port = "8893")
public class ReferenceDataJudicialUsersConsumerTest extends ReferenceDataConsumerTestBase {

    @Pact(provider = "referenceData_judicialv2", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForFindUsers(PactDslWithProvider builder) {
        return builder
            .given("return judicial user profiles v2 along with their active appointments and authorisations")
            .uponReceiving("the api returns judicial user profiles based on the provided list of user ids")
            .method("POST")
            .headers(getHttpHeaders().toSingleValueMap())
            .path("/refdata/judicial/users")
            .body(buildJudicialUserProfileRequestDsl())
            .willRespondWith()
            .body(buildJudicialUserProfileResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForFindUsers")
    public void verifyUserProfile() {
        List<JudicialUserProfile> profiles = judicialApi.findUsers(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, 1,
            ELINKS_ACCEPTS_VALUE, JudicialUserRequest.builder()
                    .ccdServiceName("PUBLICLAW")
                .build());
        JudicialUserProfile expected = JudicialUserProfile.builder()
            .sidamId("sidam-id")
            .knownAs("first last")
            .title("mr")
            .surname("last")
            .fullName("first last")
            .postNominals("post")
            .emailId("email@email.com")
            .personalCode("000")
            .build();
        assertThat(profiles, is(notNullValue()));
        assertThat(profiles.get(0), samePropertyValuesAs(expected));
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN);
        headers.add(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN);
        headers.add(HttpHeaders.ACCEPT, ELINKS_ACCEPTS_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return headers;
    }

}
