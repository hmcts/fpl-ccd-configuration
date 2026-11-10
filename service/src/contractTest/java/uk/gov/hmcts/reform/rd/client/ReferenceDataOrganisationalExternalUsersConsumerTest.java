package uk.gov.hmcts.reform.rd.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.rd.model.Organisation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@PactTestFor(providerName = "referenceData_organisationalExternalUsers", port = "8892")
@TestPropertySource(
    properties = {"rd_professional.api.url=localhost:8892", "rd_judicial.api.url=localhost:8893",
        "rd_staff.api.url=localhost:8894"})
public class ReferenceDataOrganisationalExternalUsersConsumerTest extends ReferenceDataConsumerTestBase {


    @Pact(provider = "referenceData_organisationalExternalUsers", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForGetUserOrganisation(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Organisation with Id exists")
            .uponReceiving("A Request to get organisation for user")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations")
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetUserOrganisation")
    public void verifyUserOrganisation() {
        Organisation userOrganisation = organisationApi.findUserOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN);
        assertThat(userOrganisation, is(notNullValue()));
        assertThat(userOrganisation.getOrganisationIdentifier(), is("someOrganisationIdentifier"));
    }

}
