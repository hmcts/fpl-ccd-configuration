package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import feign.Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantAboutToStartControllerTest extends AbstractControllerTest {

    private final String serviceAuthToken = RandomStringUtils.randomAlphanumeric(10);
    final String userAuthToken = "Bearer token";

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    ApplicantAboutToStartControllerTest() {
        super("enter-applicant");
    }

    @Test
    void shouldPrepopulateApplicantDataWhenNoApplicantExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken))
            .willReturn(Organisation.builder().build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("applicants");
    }

    @BeforeEach
    void setup(){
        Organisation organisation = buildOrganisation();
        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).willReturn(organisation);
    }

    @Test
    void shouldAddOrganisationDetailsToApplicantWhenOrganisationExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData data = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(data.getAllApplicants()).contains(buildApplicant());
    }

    private Organisation buildOrganisation(){
        return Organisation.builder().name("Organisation").build();
    }

    private Element<Applicant> buildApplicant(){
        return Element.<Applicant>builder()
            .value(Applicant.builder()
                .party(ApplicantParty.builder()
            .organisationName("Organisation")
                    .address(Address.builder()
                        .build())
                    .build())
                .build())
            .build();
    }
}
