package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@SuppressWarnings("unchecked")
@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
public class DraftOrdersAboutToSubmitControllerTest extends AbstractControllerTest {

    private final byte[] pdf = {1, 2, 3, 4, 5};

    private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
    private static final String DRAFT_ORDER_FILE_NAME = "draft-standard-directions-order.pdf";

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    DraftOrdersAboutToSubmitControllerTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument(SEALED_ORDER_FILE_NAME, pdf);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
    }

    @Test
    void aboutToSubmitShouldReturnErrorsWhenNoHearingDetailsExistsForSealedOrder() {
        given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, SEALED_ORDER_FILE_NAME))
            .willReturn(document());

        Order order = buildOrderWithStatus(SEALED);

        CallbackRequest request = buildCallbackRequestWithNoHearingDetails(order);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        assertThat(response.getErrors())
            .containsOnly("This standard directions order does not have a hearing associated with it. "
                + "Please enter a hearing date and resubmit the SDO");
    }

    @Test
    void aboutToSubmitShouldNotReturnErrorsWhenHearingDetailsExistsForSealedOrder() {
        given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, SEALED_ORDER_FILE_NAME))
            .willReturn(document());

        Order order = buildOrderWithStatus(SEALED);

        CallbackRequest request = buildCallbackRequestWithHearingDetails(order);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldNotReturnErrorsWhenNoHearingDetailsExistsForDraftOrder() {
        given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, DRAFT_ORDER_FILE_NAME))
            .willReturn(document());

        Order order = buildOrderWithStatus(DRAFT);

        CallbackRequest request = buildCallbackRequestWithNoHearingDetails(order);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldNotReturnErrorsWhenHearingDetailsExistsForDraftOrder() {
        given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, DRAFT_ORDER_FILE_NAME))
            .willReturn(document());

        Order order = buildOrderWithStatus(DRAFT);

        CallbackRequest request = buildCallbackRequestWithHearingDetails(order);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        assertThat(response.getErrors()).isEmpty();
    }

    private Order buildOrderWithStatus(final OrderStatus orderStatus) {
        return Order.builder()
            .orderStatus(orderStatus)
            .build();
    }

    private CallbackRequest buildCallbackRequestWithNoHearingDetails(final Order order) {
        CaseDetails caseDetails = buildCaseDetails(order);

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    private CallbackRequest buildCallbackRequestWithHearingDetails(final Order order) {
        CaseDetails caseDetails = buildCaseDetails(order);

        Map<String, Object> caseDataMap = caseDetails.getData();

        CaseDetails updatedCaseDetails = caseDetails.toBuilder()
            .data(ImmutableMap.<String, Object>builder()
                .putAll(caseDataMap)
                .putAll(Map.of(HEARING_DETAILS_KEY, createHearingBookings(LocalDateTime.now())))
                .build())
            .build();

        return CallbackRequest.builder()
            .caseDetails(updatedCaseDetails)
            .build();
    }

    private CaseDetails buildCaseDetails(final Order order) {
        return CaseDetails.builder()
            .data(createCaseDataMap(buildDirectionWithShowHideValuesRemoved(UUID.randomUUID()))
                .put("standardDirectionOrder", order)
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .build())
              .build();
    }

    private ImmutableMap.Builder createCaseDataMap(final List<Element<Direction>> directions) {
        ImmutableMap.Builder builder = ImmutableMap.<String, Object>builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), directions)
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }

    private List<Element<Direction>> buildDirections(final Direction direction) {
        return List.of(Element.<Direction>builder()
            .id(UUID.randomUUID())
            .value(direction.toBuilder().directionType("Direction").build())
            .build());
    }

    private List<Element<Direction>> buildDirectionWithShowHideValuesRemoved(final UUID uuid) {
        return List.of(Element.<Direction>builder()
            .id(uuid)
            .value(Direction.builder()
                .directionType("Identify alternative carers")
                .assignee(LOCAL_AUTHORITY)
                .readOnly("Yes")
                .build())
            .build());
    }
}
