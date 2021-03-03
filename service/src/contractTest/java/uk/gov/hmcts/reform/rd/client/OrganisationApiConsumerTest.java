package uk.gov.hmcts.reform.rd.client;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;
import uk.gov.hmcts.reform.rd.model.Status;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "referenceData_professionalExternalUsers", port = "8892")
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrganisationApiConsumerApplication.class)
@TestPropertySource(
    properties = "rd_professional.api.url=localhost:8892")
public class OrganisationApiConsumerTest {

    @Autowired
    OrganisationApi organisationApi;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_EMAIL = "UserEmail";
    private static final long CASE_ID = 1583841721773828L;
    private static final String USER_ID = "userId";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String PROFESSIONAL_USER_ID = "123456";
    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";


    @Pact(provider = "referenceData_professionalExternalUsers", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForGetOrganisationUsers(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Professional users exist for an Active organisation")
            .uponReceiving("A Request to get users for an active organisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations/users")
            .query("status=ACTIVE&returnRoles=false")
            .willRespondWith()
            .body(buildOrganisationsResponsePactDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(provider = "referenceData_professionalExternalUsers", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForGetOrganisationUserByEmail(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Professional users exist for an Active organisation")
            .uponReceiving("A Request to get user by email")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, USER_EMAIL, ORGANISATION_EMAIL)
            .path("/refdata/external/v1/organisations/users/accountId")
            .willRespondWith()
            .body(buildOrganisationUserResponsePactDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetOrganisationUsers")
    public void verifyGetOrganisationalUsers() {
        OrganisationUsers usersInOrganisation =
            organisationApi.findUsersInOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, Status.ACTIVE, Boolean.FALSE);

    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetOrganisationUserByEmail")
    public void verifyGetOrganisationalUserByEmail() {
        OrganisationUser organisationUser  =
            organisationApi.findUserByEmail(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_EMAIL);

    }

    private DslPart buildOrganisationsResponsePactDsl() {
        //{"users":[{"userIdentifier":"userId"}]}
        return newJsonBody(ob -> ob
            .array("users", pa ->
                pa.object(u -> u.stringType("userIdentifier", "userId"))
            ))
            .build();
    }

    private DslPart buildOrganisationUserResponsePactDsl() {
        return newJsonBody(u -> u.stringType("userIdentifier", "userId"))
            .build();
    }

}
