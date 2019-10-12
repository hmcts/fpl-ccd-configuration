package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerTest {

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Test
    void aboutToStartCallbackShouldSplitDirectionsIntoSeparateCollections() throws Exception {
        String title = "example direction";

        List<Direction> directions = ImmutableList.of(
            Direction.builder().type(title).assignee(ALL_PARTIES).build(),
            Direction.builder().type(title).assignee(LOCAL_AUTHORITY).build(),
            Direction.builder().type(title).assignee(PARENTS_AND_RESPONDENTS).build(),
            Direction.builder().type(title).assignee(CAFCASS).build(),
            Direction.builder().type(title).assignee(OTHERS).build(),
            Direction.builder().type(title).assignee(COURT).build()
        );

        Order sdo = Order.builder().directions(buildDirections(directions)).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/draft-SDO/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(extractDirections(caseData.getAllParties())).containsOnly(directions.get(0));
        assertThat(extractDirections(caseData.getLocalAuthorityDirections())).containsOnly(directions.get(1));
        assertThat(extractDirections(caseData.getParentsAndRespondentsDirections())).containsOnly(directions.get(2));
        assertThat(extractDirections(caseData.getCafcassDirections())).containsOnly(directions.get(3));
        assertThat(extractDirections(caseData.getOtherPartiesDirections())).containsOnly(directions.get(4));
        assertThat(extractDirections(caseData.getCourtDirections())).containsOnly(directions.get(5));
    }

    @Test
    void midEventShouldGenerateDraftStandardDirectionDocument() throws Exception {
        byte[] pdf = {1, 2, 3, 4, 5};
        DocmosisDocument docmosisDocument = new DocmosisDocument("draft-standard-directions-order.pdf", pdf);
        Document document = document();

        given(documentGeneratorService.generateDocmosisDocument(any(), any()))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-standard-directions-order.pdf"))
            .willReturn(document);

        List<Element<Direction>> directions = buildDirections(
            ImmutableList.of(Direction.builder()
                .text("example")
                .assignee(LOCAL_AUTHORITY)
                .readOnly("No")
                .build())
        );

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(createCaseDetails(directions))
            .caseDetailsBefore(createCaseDetails(directions))
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/draft-SDO/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("callbackResponse = " + callbackResponse.getData());

        assertThat(callbackResponse.getData()).containsEntry("sdo", ImmutableMap.builder()
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", "draft-standard-directions-order.pdf")
            .put("document_url", document.links.self.href)
            .build());
    }

    @Test
    void aboutToSubmitShouldPopulateHiddenCCDFieldsInStandardDirectionOrderToPersistData() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<Element<Direction>> fullyPopulatedDirection = ImmutableList.of(Element.<Direction>builder()
            .id(uuid)
            .value(Direction.builder()
                .type("exampleDirection")
                .text("example")
                .assignee(LOCAL_AUTHORITY)
                .directionRemovable("Yes")
                .directionNeeded(null)
                .readOnly("Yes")
                .build())
            .build());

        List<Element<Direction>> directionWithShowHideValuesRemoved = ImmutableList.of(Element.<Direction>builder()
            .id(uuid)
            .value(Direction.builder()
                .type("exampleDirection")
                .assignee(LOCAL_AUTHORITY)
                .directionNeeded(null)
                .build())
            .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetailsBefore(createCaseDetails(fullyPopulatedDirection))
            .caseDetails(createCaseDetails(directionWithShowHideValuesRemoved))
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/draft-SDO/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        List<Element<Direction>> localAuthorityDirections =
            caseData.getStandardDirectionOrder().getDirections().stream()
                .filter(direction -> direction.getValue().getAssignee() == LOCAL_AUTHORITY)
                .collect(toList());

        assertThat(localAuthorityDirections).isEqualTo(fullyPopulatedDirection);
    }

    private CaseDetails createCaseDetails(List<Element<Direction>> directions) {
        return CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put(LOCAL_AUTHORITY.getValue(), directions)
                .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
                .put(PARENTS_AND_RESPONDENTS.getValue(),
                    buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
                .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
                .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
                .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()))
                .build())
            .build();
    }

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(direction -> Element.<Direction>builder()
            .id(UUID.randomUUID())
            .value(direction)
            .build())
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return ImmutableList.of(Element.<Direction>builder()
            .id(UUID.randomUUID())
            .value(direction)
            .build());
    }

    private List<Direction> extractDirections(List<Element<Direction>> directions) {
        return directions.stream().map(Element::getValue).collect(toList());
    }
}
