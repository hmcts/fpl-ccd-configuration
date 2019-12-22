package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
public class ChildControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldPrepopulateChildrenDataWhenNoChildExists() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("data", "some data"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "about-to-start");

        assertThat(callbackResponse.getData()).containsKey("children1");
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenFutureDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("children1", ImmutableList.of(
                    createChildrenElement(LocalDate.now().plusDays(1)))))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "mid-event");

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenThereIsMultipleChildren() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "children1", ImmutableList.of(
                        createChildrenElement(LocalDate.now().plusDays(1)),
                        createChildrenElement(LocalDate.now().plusDays(1))
                    )))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "mid-event");

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorWhenValidDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("children1", ImmutableList.of(
                    createChildrenElement(LocalDate.now().minusDays(1)))))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "mid-event");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsWhenCaseDataIsEmpty() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "mid-event");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldAddOnlyConfidentialChildrenToCaseDataWhenConfidentialChildrenExist() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(callbackRequest(), "about-to-submit");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getConfidentialChildren()).hasSize(1);
        assertThat(caseData.getConfidentialChildren()).isEqualTo(buildExpectedConfidentialChildren());
    }

    //TODO double check: maybe cleaner to create a smaller request in above test instead of using callback-request.json?
    private List<Element<Child>> buildExpectedConfidentialChildren() {
        List<Element<Child>> confidentialChildren = new ArrayList<>();
        confidentialChildren.add(Element.<Child>builder()
            .id(UUID.fromString("3b0b9640-2894-41eb-bef2-a031d18c8457"))
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Reeves")
                    .adoption("Yes")
                    .dateOfBirth(LocalDate.of(2018, 06, 15))
                    .keyDates("child starting primary school or taking GCSEs")
                    .gender("Boy")
                    .fathersName("Rob Reeves")
                    .mothersName("Isbella Reeves")
                    .detailsHidden("Yes")
                    .detailsHiddenReason("History of domestic violence with relatives")
                    .fathersResponsibility("Yes")
                    .additionalNeedsDetails("Autism")
                    .genderIdentification("Boy")
                    .placementOrderApplication("Yes")
                    .livingSituation("Living with respondents")
                    .litigationIssues("DONT_KNOW")
                    .addressChangeDate(LocalDate.of(2018, 11, 8))
                    .careAndContactPlan("Place baby in local authority foster care")
                    .placementCourt("Central London County Court")
                    .additionalNeeds("Yes")
                    .socialWorkerName("Helen Green")
                    .socialWorkerTelephoneNumber(Telephone.builder()
                        .telephoneNumber("0123456789").build())
                    .address(Address.builder()
                        .county("Omagh")
                        .country("Northern Ireland")
                        .postcode("SE22 6SB")
                        .postTown("BT11 1234")
                        .addressLine1("Appartment 21")
                        .addressLine2("22 Wesley Drive")
                        .addressLine3("Wesley").build()).build()).build()).build());

        return confidentialChildren;
    }

    private Map<String, Object> createChildrenElement(LocalDate dateOfBirth) {
        return ImmutableMap.of(
            "id", "",
            "value", Child.builder()
                .party(ChildParty.builder()
                    .dateOfBirth(dateOfBirth)
                    .build())
                .build());
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request, String endpoint)
        throws Exception {
        MvcResult mvc = mockMvc
            .perform(post("/callback/enter-children/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(mvc.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
