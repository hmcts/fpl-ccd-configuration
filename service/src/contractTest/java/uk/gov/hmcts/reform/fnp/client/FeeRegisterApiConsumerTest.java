package uk.gov.hmcts.reform.fnp.client;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "feeRegister_lookUp", port = "8889")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FeesRegisterApiConsumerApplication.class, FeeService.class, FeesConfig.class})
@TestPropertySource(
    properties = {"fees-register.api.url=localhost:8889", "payment.api.url=localhost:8889"}
)
@PactFolder("pacts")
public class FeeRegisterApiConsumerTest {
    @Autowired
    FeesRegisterApi feesRegisterApi;

    @Autowired
    FeeService feeService;

    public static final String CHANNEL = "default";
    public static final String MISC_EVENT = "miscellaneous";
    public static final String ISSUE_EVENT = "issue";
    public static final String JURISDICTION_1 = "family";
    public static final String JURISDICTION_2 = "family court";
    public static final String PRIVATE_LAW_SERVICE = "private law";
    public static final String OTHER_KEYWORD = "VariationDischarge";
    public static final String PLACEMENT_KEYWORD = "Placement";
    public static final String ADOPTION_SERVICE = "adoption";
    public static final String EPO_KEYWORD = "EPO";
    public static final String SUPERVISION_KEYWORD = "CareOrder";
    public static final String PUBLIC_LAW_SERVICE = "public law";

    @Pact(provider = "feeRegister_lookUp", consumer = "fpl_ccdConfiguration")
    private RequestResponsePact generateOtherFeesPactFragment(PactDslWithProvider builder) throws JSONException {
        return getRequestResponsePact(builder,
            OTHER_KEYWORD,
            PRIVATE_LAW_SERVICE,
            "FEE0328",
            "Variation or discharge etc of care and supervision orders (section 39)",
            215.00,
            MISC_EVENT);
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "fpl_ccdConfiguration")
    private RequestResponsePact generatePlacementFeesPactFragment(PactDslWithProvider builder) throws JSONException {
        return getRequestResponsePact(builder,
            PLACEMENT_KEYWORD,
            ADOPTION_SERVICE,
            "FEE0310",
            "Application for a placement order (under Section 22)",
            455.00,
            MISC_EVENT);
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "fpl_ccdConfiguration")
    private RequestResponsePact generateEpoFeesPactFragment(PactDslWithProvider builder) throws JSONException {
        return getRequestResponsePact(builder,
            EPO_KEYWORD,
            PRIVATE_LAW_SERVICE,
            "FEE0326",
            "Emergency protection orders (sections 44, 45 and 46)",
            215.00,
            MISC_EVENT);
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "fpl_ccdConfiguration")
    private RequestResponsePact generateSupervisionFeesPactFragment(PactDslWithProvider builder) throws JSONException {
        return getRequestResponsePact(builder,
            SUPERVISION_KEYWORD,
            PUBLIC_LAW_SERVICE,
            "FEE0314",
            "Application for proceedings under Section 31 of Act",
            2055.00,
            ISSUE_EVENT);
    }

    private RequestResponsePact getRequestResponsePact(PactDslWithProvider builder, String keyword, String service,
                                                       String code, String description,
                                                       double feeAmount, String event) {
        return builder
            .given("Fees exist for CCD")
            .uponReceiving("a request for CCD fees")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("service", service, service)
            .matchQuery("jurisdiction1", "family", "family")
            .matchQuery("jurisdiction2", "family court", "family court")
            .matchQuery("channel", "default", "default")
            .matchQuery("event", event, event)
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBodyDsl(code, description, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private PactDslJsonBody buildFeesResponseBodyDsl(String code, String description, double feeAmount) {
        return new PactDslJsonBody()
            .stringType("code",code)
            .stringType("description", description)
            .numberType("version", 1)
            .decimalType("fee_amount", feeAmount);
    }

    @Test
    @PactTestFor(pactMethod = "generateOtherFeesPactFragment")
    public void verifyOtherFeesServicePact() {
        FeeResponse feeResponse = feesRegisterApi.findFee(
            CHANNEL, MISC_EVENT, JURISDICTION_1, JURISDICTION_2, OTHER_KEYWORD, PRIVATE_LAW_SERVICE
        );
        assertEquals("FEE0328", feeResponse.getCode());
    }

    @Test
    @PactTestFor(pactMethod = "generatePlacementFeesPactFragment")
    public void verifyPlacementFeesServicePact() {
        FeeResponse feeResponse = feesRegisterApi.findFee(
            CHANNEL, MISC_EVENT, JURISDICTION_1, JURISDICTION_2, PLACEMENT_KEYWORD, ADOPTION_SERVICE
        );
        assertEquals("FEE0310", feeResponse.getCode());
    }

    @Test
    @PactTestFor(pactMethod = "generateEpoFeesPactFragment")
    public void verifyEpoFeesServicePact() {
        FeeResponse feeResponse = feesRegisterApi.findFee(
            CHANNEL, MISC_EVENT, JURISDICTION_1, JURISDICTION_2, EPO_KEYWORD, PRIVATE_LAW_SERVICE
        );
        assertEquals("FEE0326", feeResponse.getCode());
    }

    @Test
    @PactTestFor(pactMethod = "generateSupervisionFeesPactFragment")
    public void verifySupervisionFeesServicePact() {
        FeeResponse feeResponse = feesRegisterApi.findFee(
            CHANNEL, ISSUE_EVENT, JURISDICTION_1, JURISDICTION_2, SUPERVISION_KEYWORD, PUBLIC_LAW_SERVICE
        );
        assertEquals("FEE0314", feeResponse.getCode());
    }
}
