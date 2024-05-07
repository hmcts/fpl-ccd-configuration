package uk.gov.hmcts.reform.am.client;

import au.com.dius.pact.consumer.dsl.DslPart;
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
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleRequest;
import uk.gov.hmcts.reform.am.model.RoleType;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_createAssignment", port = "8894")
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RoleAssignmentServiceConsumerApplication.class)
@TestPropertySource(properties = "am_role_assignment.api.url=localhost:8894")
public class RoleAssignmentServiceConsumerTest {

    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AmApi amApi;

    private final ZonedDateTime now = ZonedDateTime.now();

    // create
    @Pact(provider = "am_roleAssignment_createAssignment", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragmentForCreate(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("The assignment request is valid with one requested role and replaceExisting flag as false")
            .uponReceiving("A request to add a role")
            .method("POST")
            .path("/am/role-assignments")
            .headers("Content-Type", "application/json")
            .body(objectMapper.writeValueAsString(buildAssignmentRequest()))
            .willRespondWith()
            .status(HttpStatus.SC_CREATED)
            .body(buildCreateResponsePactDsl())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCreate")
    public void verifyAssignRole() {
        RoleAssignmentRequestResource res = amApi.createRoleAssignment(AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN, buildAssignmentRequest());

        assertThat(res.getRoleAssignmentResponse().getRequestedRoles()).asList().hasSize(1);
        assertThat(res.getRoleAssignmentResponse().getRequestedRoles().get(0).getStatus()).isEqualTo("APPROVED");
    }

    private AssignmentRequest buildAssignmentRequest() {
        return AssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                .assignerId("assignerId")
                .process("process")
                .reference("reference")
                .replaceExisting(false)
                .build())
            .requestedRoles(List.of(
                RoleAssignment.builder()
                    .actorId("actorId")
                    .actorIdType("actorIdType")
                    .status("status")
                    .beginTime(now)
                    .endTime(now.plusDays(1))
                    .grantType(GrantType.STANDARD)
                    .roleName("roleName")
                    .roleType(RoleType.CASE)
                    .roleCategory(RoleCategory.LEGAL_OPERATIONS)
                    .build()
            ))
            .build();
    }

    protected DslPart buildCreateResponsePactDsl() {
        return newJsonBody(ob -> ob
            .object("roleAssignmentResponse", pa -> pa
                .array("requestedRoles", ra -> ra
                    .object(r -> r
                        .stringType("actorId", "actorId")
                        .stringType("actorIdType", "actorIdType")
                        .stringType("roleName", "roleName")
                        .stringType("roleType", "CASE")
                        .stringType("classification", "PUBLIC")
                        .stringType("grantType", "STANDARD")
                        .stringType("roleCategory", "LEGAL_OPERATIONS")
                        .stringType("status", "APPROVED")
                        .stringType("beginTime", now.toString())
                        .stringType("endTime", now.plusDays(1).toString())
                    )
                )
                .object("roleRequest", rr -> rr
                    .stringType("assignerId", "assignerId")
                    .stringType("process", "process")
                    .stringType("reference", "reference")
                    .booleanType("replaceExisting", false)
                ))
        ).build();
    }

    // query

    // delete
}
