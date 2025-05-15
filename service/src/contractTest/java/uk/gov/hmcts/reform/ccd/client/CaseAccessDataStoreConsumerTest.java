package uk.gov.hmcts.reform.ccd.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccdDataStoreAPI_caseAssignedUserRoles", port = "8891")
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CaseAccessDataStoreConsumerApplication.class)
@TestPropertySource(
    properties = "core_case_data.api.url=localhost:8891")
public class CaseAccessDataStoreConsumerTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final long CASE_ID = 1583841721773828L;
    private static final String USER_ID = "userId";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";


    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    CaseAssignmentApi caseAssignmentApi;


    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForDelete(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to remove a User Role")
            .method("DELETE")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users")
            .body(createJsonObject(buildCaseAssignmentUserRolesRequest()))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForAdd(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to add a User Role")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users")
            .body(createJsonObject(buildAssignmentRequest(CASE_ID, Set.of(USER_ID),
                "organisationId", CaseRole.EPSMANAGING)))
            .willRespondWith()
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForDelete")
    public void verifyRemoveRoles() {
        caseAssignmentApi.removeCaseUserRoles(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN,
            buildCaseAssignmentUserRolesRequest());

    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForAdd")
    public void verifyAddRoles() {
        caseAssignmentApi.addCaseUserRoles(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN,
            buildAssignmentRequest(CASE_ID, Set.of(USER_ID), "organisationId", CaseRole.EPSMANAGING));

    }

    private CaseAssignmentUserRolesRequest buildCaseAssignmentUserRolesRequest() {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(CaseAssignmentUserRoleWithOrganisation.builder()
                .userId(USER_ID)
                .caseRole(CREATOR.formattedName())
                .caseDataId(Long.toString(CASE_ID))
                .build()))
            .build();
    }

    private CaseAssignmentUserRolesRequest buildAssignmentRequest(Long caseId, Set<String> userIds, String orgId,
                                                                   CaseRole caseRole) {
        final List<CaseAssignmentUserRoleWithOrganisation> caseAssignedRoles = userIds.stream()
            .map(userId -> CaseAssignmentUserRoleWithOrganisation.builder()
                .caseDataId(caseId.toString())
                .userId(userId)
                .organisationId(orgId)
                .caseRole(caseRole.formattedName())
                .build())
            .collect(Collectors.toList());

        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(caseAssignedRoles)
            .build();
    }

    private String createJsonObject(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
