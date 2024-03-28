package uk.gov.hmcts.reform.rd.client;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrganisationApiConsumerApplication.class)
@TestPropertySource(
    properties = {"rd_judicial.api.url=localhost:8893", "rd_professional.api.url=localhost:8892"}
)
public class ReferenceDataConsumerTestBase {

    @Autowired
    OrganisationApi organisationApi;

    @Autowired
    JudicialApi judicialApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    static final String ELINKS_ACCEPTS_HEADER = "Accept";
    static final String ELINKS_ACCEPTS_VALUE = "application/vnd.jrd.api+json;Version=2.0";
    static final String ELINKS_PAGE_SIZE_HEADER = "page_size";

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
                pa.object(u -> u.stringType("userIdentifier", "userId")
                    .stringType("firstName", "first")
                    .stringType("lastName", "last")
                    .stringType("email", "email@email.com"))
            ))
            .build();
    }

    protected DslPart buildOrganisationUserResponsePactDsl() {
        return newJsonBody(u -> u.stringType("userIdentifier", "userId"))
            .build();
    }

    protected DslPart buildJudicialUserProfileResponseDsl() {
        return newJsonArray(a -> a.object(o -> o
            .stringType("sidam_id", "sidam-id")
            .stringType("known_as", "first last")
            .stringType("title", "mr")
            .stringType("surname", "last")
            .stringType("full_name", "first last")
            .stringType("post_nominals", "post")
            .stringType("email_id", "email@email.com")
            .stringType("personalCode", "000")
        )).build();
    }

    protected DslPart buildJudicialUserProfileRequestDsl() {
        return newJsonBody(u -> u.stringType("ccdServiceName", "PUBLICLAW"))
            .build();
    }
}
