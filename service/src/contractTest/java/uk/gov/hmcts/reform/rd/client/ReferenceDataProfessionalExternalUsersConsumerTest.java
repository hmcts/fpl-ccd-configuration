package uk.gov.hmcts.reform.rd.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;
import uk.gov.hmcts.reform.rd.model.Status;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@PactTestFor(providerName = "referenceData_professionalExternalUsers", port = "8892")
@TestPropertySource(
    properties = "rd_professional.api.url=localhost:8892")
public class ReferenceDataProfessionalExternalUsersConsumerTest extends ReferenceDataConsumerTestBase {

    private static final String USER_EMAIL = "UserEmail";
    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    @Pact(provider = "referenceData_professionalExternalUsers", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForGetOrganisationUsers(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Professional users exist for an Active organisation")
            .uponReceiving("A Request to get users for an active organisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations/users")
            .willRespondWith()
            .body(buildOrganisationsResponsePactDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetOrganisationUsers")
    public void verifyGetOrganisationalUsers() {
        OrganisationUsers usersInOrganisation =
            organisationApi.findUsersInCurrentUserOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN,
                Status.ACTIVE, Boolean.FALSE);
        assertThat(usersInOrganisation.getUsers(), is(not(empty())));
        assertThat(usersInOrganisation.getUsers().get(0).getUserIdentifier(), is("userId"));

    }

}
