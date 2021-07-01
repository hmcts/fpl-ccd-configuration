package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.RemoveOrderController.CMO_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.OrderOrApplication.APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerMidEventTest extends AbstractCallbackTest {
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID REMOVE_ORDER_ID = UUID.randomUUID();
    private Element<GeneratedOrder> selectedOrder;

    RemoveOrderControllerMidEventTest() {
        super("remove-order");
    }

    @BeforeEach
    void initialise() {
        selectedOrder = element(buildOrder());
    }

    @Test
    void shouldExtractSelectedGeneratedOrderFields() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(selectedOrder.getValue().getDocument(),
                new TypeReference<Map<String, Object>>() {
                }),
            "orderTitleToBeRemoved", selectedOrder.getValue().getTitle(),
            "orderIssuedDateToBeRemoved", selectedOrder.getValue().getDateOfIssue(),
            "orderDateToBeRemoved", selectedOrder.getValue().getDate()
        );

        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldExtractSelectedSealedCaseManagementOrderFields() {
        DocumentReference documentReference = DocumentReference.builder().build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now())
            .caseManagementOrderId(REMOVE_ORDER_ID)
            .build();

        HearingOrder caseManagementOrder = HearingOrder.builder()
            .type(HearingOrderType.AGREED_CMO)
            .status(APPROVED)
            .order(documentReference)
            .dateIssued(now().toLocalDate())
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(REMOVE_ORDER_ID)
                .label("Case management order - 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .sealedCMOs(List.of(element(REMOVE_ORDER_ID, caseManagementOrder)))
            .hearingDetails(List.of(element(hearingBooking)))
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(caseManagementOrder.getOrder(),
                new TypeReference<Map<String, Object>>() {
                }),
            "orderTitleToBeRemoved", "Sealed case management order",
            "hearingToUnlink", hearingBooking.toLabel()
        );

        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldExtractDraftSealedCaseManagementOrderFields() {
        DocumentReference documentReference = DocumentReference.builder().build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now())
            .caseManagementOrderId(REMOVE_ORDER_ID)
            .build();

        HearingOrder caseManagementOrder = HearingOrder.builder()
            .type(HearingOrderType.DRAFT_CMO)
            .status(DRAFT)
            .order(documentReference)
            .dateSent(now().toLocalDate())
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(REMOVE_ORDER_ID)
                .label("Case management order - 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(newArrayList(
                element(HearingOrdersBundle.builder()
                    .orders(newArrayList(element(REMOVE_ORDER_ID, caseManagementOrder)))
                    .build())))
            .hearingDetails(List.of(element(hearingBooking)))
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(caseManagementOrder.getOrder(),
                new TypeReference<Map<String, Object>>() {
                }),
            "orderTitleToBeRemoved", "Draft case management order",
            "hearingToUnlink", hearingBooking.toLabel()
        );

        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldExtractDraftOrderFields() {
        DocumentReference documentReference = DocumentReference.builder().build();

        HearingOrder draftOrder = HearingOrder.builder()
            .type(HearingOrderType.C21)
            .title("Draft1")
            .status(SEND_TO_JUDGE)
            .order(documentReference)
            .dateSent(now().toLocalDate())
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(REMOVE_ORDER_ID)
                .label("Draft order sent on 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(newArrayList(
                element(HearingOrdersBundle.builder()
                    .orders(newArrayList(element(REMOVE_ORDER_ID, draftOrder)))
                    .build())))
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));
        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(draftOrder.getOrder(),
                new TypeReference<Map<String, Object>>() {
                }),
            "orderTitleToBeRemoved", draftOrder.getTitle(),
            "showRemoveCMOFieldsFlag", EMPTY,
            "showReasonFieldFlag", NO.getValue()
        );

        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldExtractSelectedStandardDirectionOrderFields() {
        DocumentReference documentReference = DocumentReference.builder().build();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(SDO_ID)
                .label("Gatekeeping order - 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(standardDirectionOrder)
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(standardDirectionOrder.getOrderDoc(),
                new TypeReference<Map<String, Object>>() {
                }),
            "orderTitleToBeRemoved", "Gatekeeping order",
            "showRemoveSDOWarningFlag", YES.getValue()
        );

        assertThat(response.getErrors()).isNull();
        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldThrowAnErrorWhenCaseManagementOrderHasNotLinkedHearing() {
        DocumentReference documentReference = DocumentReference.builder().build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now())
            .caseManagementOrderId(UUID.randomUUID())
            .build();

        HearingOrder caseManagementOrder = HearingOrder.builder()
            .type(HearingOrderType.AGREED_CMO)
            .order(documentReference)
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(REMOVE_ORDER_ID)
                .label("Case management order - 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .sealedCMOs(List.of(element(REMOVE_ORDER_ID, caseManagementOrder)))
            .hearingDetails(List.of(element(hearingBooking)))
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).isEqualTo(List.of(String.format(CMO_ERROR_MESSAGE, REMOVE_ORDER_ID)));
    }

    @Test
    void shouldRegenerateOrderDynamicList() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
        DynamicList removableOrderList = mapper.convertValue(responseData.getRemovableOrderList(), DynamicList.class);

        assertThat(removableOrderList).isEqualTo(buildRemovableOrderList(selectedOrder.getId()));
    }

    @Test
    void shouldExtractApplicationFields() {
        UUID applicationId = UUID.randomUUID();
        AdditionalApplicationsBundle application = buildCombinedApplication(C1_WITH_SUPPLEMENT, "6 May 2020");

        DynamicList dynamicList = DynamicList.builder()
            .value(buildListElement(applicationId, "C2, C1, 6 May 2020"))
            .build();

        CaseData caseData = CaseData.builder()
            .removeOrderOrApplication(APPLICATION)
            .additionalApplicationsBundle(List.of(element(applicationId, application)))
            .removableApplicationList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "applicationTypeToBeRemoved", application.toLabel(),
            "c2ApplicationToBeRemoved", mapper.convertValue(application.getC2DocumentBundle().getDocument(),
                new TypeReference<Map<String, Object>>() {
                }),
            "otherApplicationToBeRemoved", mapper.convertValue(application.getOtherApplicationsBundle().getDocument(),
                new TypeReference<Map<String, Object>>() {
                }),
            "orderDateToBeRemoved", application.getUploadedDateTime()
        );

        DynamicList removableApplicationList = mapper.convertValue(responseData.get("removableApplicationList"), DynamicList.class);

        DynamicList expectedList = DynamicList.builder()
            .value(buildListElement(applicationId, "C2, C1, 6 May 2020"))
            .listItems(List.of(buildListElement(applicationId, "C2, C1, 6 May 2020"))).build();

        assertThat(removableApplicationList).isEqualTo(expectedList);
        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    private CaseData buildCaseData(Element<GeneratedOrder> order) {
        return CaseData.builder()
            .orderCollection(List.of(order))
            .removableOrderList(buildRemovableOrderList(order.getId()))
            .build();
    }

    private DynamicList buildRemovableOrderList(UUID id) {
        return DynamicList.builder()
            .value(buildListElement(id,"order - 12 March 1234"))
            .listItems(List.of(buildListElement(id,"order - 12 March 1234")))
            .build();
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private GeneratedOrder buildOrder() {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title("order")
            .dateOfIssue("12 March 1234")
            .date("11:23am, 12 March 1234")
            .document(DocumentReference.builder().build())
            .build();
    }

    private AdditionalApplicationsBundle buildCombinedApplication(OtherApplicationType type, String date) {
        return AdditionalApplicationsBundle.builder()
            .uploadedDateTime(date)
            .c2DocumentBundle(C2DocumentBundle.builder()
                .document(testDocumentReference())
                .uploadedDateTime(date)
                .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .document(testDocumentReference())
                .applicationType(type)
                .uploadedDateTime(date)
                .build())
            .build();
    }
}
