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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final Long CASE_ID = 1L;
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.builder().build();
    private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
    private static final Document DOCUMENT = document();
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.of(2020, 1, 20, 11, 11, 11);
    private static final LocalDateTime HEARING_END_DATE = LocalDateTime.of(2020, 2, 20, 11, 11, 11);

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    DraftOrdersControllerAboutToSubmitTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument(SEALED_ORDER_FILE_NAME, PDF);

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(PDF, SEALED_ORDER_FILE_NAME)).willReturn(DOCUMENT);
    }

    @Test
    void shouldPopulateHiddenCCDFieldsInStandardDirectionOrderToPersistData() {
        UUID directionId = UUID.randomUUID();

        List<Element<Direction>> fullyPopulatedDirection = List.of(
            element(directionId, Direction.builder()
                .directionType("Identify alternative carers")
                .directionText("Contact the parents to make sure there is a complete family tree showing family"
                    + " members who could be alternative carers.")
                .assignee(LOCAL_AUTHORITY)
                .directionRemovable("Yes")
                .readOnly("Yes")
                .build()));

        List<Element<Direction>> directionWithShowHideValuesRemoved = buildDirectionWithShowHideValuesRemoved(
            directionId);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(directionWithShowHideValuesRemoved)
                .put("dateOfIssue", dateNow())
                .put("standardDirectionOrder", Order.builder().orderStatus(SEALED).build())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .put("allocatedJudge", Judge.builder().build())
                .put(HEARING_DETAILS_KEY, wrapElements(HearingBooking.builder()
                    .startDate(HEARING_START_DATE)
                    .endDate(HEARING_END_DATE)
                    .venue("EXAMPLE")
                    .build()))
                .put("caseLocalAuthority", "example")
                .put("dateSubmitted", dateNow())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        List<Element<Direction>> localAuthorityDirections =
            caseData.getStandardDirectionOrder().getDirections().stream()
                .filter(direction -> direction.getValue().getAssignee() == LOCAL_AUTHORITY)
                .collect(toList());

        assertThat(localAuthorityDirections).isEqualTo(fullyPopulatedDirection);
        assertThat(caseData.getStandardDirectionOrder().getOrderDoc()).isNotNull();
        assertThat(caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor()).isNotNull();
        assertThat(caseData.getJudgeAndLegalAdvisor()).isNull();
    }

    @Test
    void shouldReturnErrorsWhenNoHearingDetailsExistsForSealedOrder() {
        UUID directionId = UUID.randomUUID();

        List<Element<Direction>> directionWithShowHideValuesRemoved = buildDirectionWithShowHideValuesRemoved(
            directionId);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(createCaseDataMap(directionWithShowHideValuesRemoved)
                .put("standardDirectionOrder", Order.builder().orderStatus(SEALED).build())
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .put("allocatedJudge", Judge.builder().build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getErrors()).containsOnly("You need to enter a hearing date.");
    }

    @Test
    void shouldReturnErrorsWhenNoAllocatedJudgeExistsForSealedOrder() {
        CallbackRequest request = buildCallbackRequest();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        assertThat(response.getErrors())
            .containsOnly("You need to enter the allocated judge.");
    }

    private List<Element<Direction>> buildDirectionWithShowHideValuesRemoved(UUID uuid) {
        return List.of(element(uuid, Direction.builder()
            .directionType("Identify alternative carers")
            .directionText("Contact the parents to make sure there is a complete family tree showing family"
                + " members who could be alternative carers.")
            .assignee(LOCAL_AUTHORITY)
            .readOnly("Yes")
            .directionRemovable("Yes")
            .build()));
    }

    private ImmutableMap.Builder<String, Object> createCaseDataMap(List<Element<Direction>> directions) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), directions)
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }

    private CallbackRequest buildCallbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of(
                    HEARING_DETAILS_KEY, List.of(
                        Element.builder()
                            .value(HearingBooking.builder()
                                .startDate(HEARING_START_DATE)
                                .endDate(HEARING_END_DATE)
                                .build())
                            .build()),
                    "respondents1", List.of(
                        Map.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(dateNow().plusDays(1))
                                    .lastName("Moley")
                                    .relationshipToChild("Uncle")
                                    .build())
                                .build()
                        )
                    ),
                    "standardDirectionOrder", Order.builder()
                        .orderStatus(OrderStatus.SEALED)
                        .orderDoc(DOCUMENT_REFERENCE)
                        .build(),
                    "caseLocalAuthority", "example"))
                .build())
            .build();
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction.toBuilder().directionType("Direction").build());
    }

}
