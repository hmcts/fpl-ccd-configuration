package uk.gov.hmcts.reform.rd.client;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrganisationApiConsumerApplication.class)
public class ReferenceDataConsumerTestBase {

    @Autowired
    OrganisationApi organisationApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    protected DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o.stringType("name", "theKCompany")
                .stringType("organisationIdentifier", "BJMSDFDS80808")
                .stringType("companyNumber", "companyNumber")
                .stringType("organisationIdentifier", "someOrganisationIdentifier")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", Boolean.TRUE)
                .stringType("status", "ACTIVE")
                .minArrayLike("contactInformation", 1, 1,
                    sh -> {
                        sh.stringType("addressLine1", "addressLine1")
                            .stringType("addressLine2", "addressLine2")
                            .stringType("country", "UK")
                            .stringType("postCode", "SM12SX");

                    });
        }).build();
    }

    protected DslPart buildOrganisationsResponsePactDsl() {
        return newJsonBody(ob -> ob
            .array("users", pa ->
                pa.object(u -> u.stringType("userIdentifier", "userId"))
            ))
            .build();
    }

    protected DslPart buildOrganisationUserResponsePactDsl() {
        return newJsonBody(u -> u.stringType("userIdentifier", "userId"))
            .build();
    }
}
