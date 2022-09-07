package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(RemovalToolController.class)
@OverrideAutoConfiguration(enabled = true)
public class RemoveDraftOrdersControllerTest extends AbstractCallbackTest {

    protected RemoveDraftOrdersControllerTest() {
        super("remove-draft-orders");
    }

    @Nested
    class AboutToStartTest {
        @Test
        void shouldPopulateInitialData() {
            Element<HearingOrder> draftCMOOne = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
            Element<HearingOrder> draftCMOTwo = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
            Element<HearingOrder> draftCMOThree = element(UUID.randomUUID(), buildPastHearingOrder(DRAFT_CMO));
            Element<HearingOrder> agreedCMO = element(UUID.randomUUID(), buildPastHearingOrder(AGREED_CMO));
            Element<HearingOrder> draftOrderOne = element(UUID.randomUUID(), buildPastHearingOrder(C21));
            Element<HearingOrder> draftOrderTwo = element(UUID.randomUUID(), buildPastHearingOrder(C21));

            CaseData caseData = CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .draftUploadedCMOs(newArrayList(draftCMOOne, draftCMOThree))
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(HearingOrdersBundle.builder()
                        .orders(newArrayList(draftCMOOne, draftOrderOne))
                        .build()),
                    element(HearingOrdersBundle.builder()
                        .orders(newArrayList(draftCMOTwo))
                        .build()),
                    element(HearingOrdersBundle.builder()
                        .orders(newArrayList(agreedCMO, draftOrderTwo))
                        .build())
                )).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

            DynamicList builtDynamicList = mapper.convertValue(
                response.getData().get("removableOrderList"), DynamicList.class
            );
            DynamicList expectedList = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(List.of(
                    buildListElement(draftCMOOne.getId(), format("Draft case management order sent on %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                    buildListElement(draftOrderOne.getId(), format("Draft order sent on %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                    buildListElement(draftCMOTwo.getId(), format("Draft case management order sent on %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                    buildListElement(agreedCMO.getId(), format("Agreed case management order sent on %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                    buildListElement(draftOrderTwo.getId(), format("Draft order sent on %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"))),
                    buildListElement(draftCMOThree.getId(), format("Draft case management order sent on %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy")))
                )).build();

            assertThat(builtDynamicList).isEqualTo(expectedList);
        }
    }

    @Nested
    class MidEventTest {
        @Test
        void shouldExtractDraftOrderFields() {
            final UUID selectedRemoveOrderId = UUID.randomUUID();
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
                    .code(selectedRemoveOrderId)
                    .label("Draft order sent on 12 March 1234")
                    .build())
                .build();

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(HearingOrdersBundle.builder()
                        .orders(newArrayList(element(selectedRemoveOrderId, draftOrder)))
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
    }

    @Nested
    class AboutToSubmitTest {
        @Test
        void shouldRemoveTemporaryFieldsAndSelectedDraftOrder() {
            Map<String, Object> fields = new HashMap<>();

            fields.put("orderTitleToBeRemoved", "dummy data");
            fields.put("orderToBeRemoved", "dummy data");
            fields.put("showRemoveCMOFieldsFlag", "dummy data");
            fields.put("showReasonFieldFlag", "dummy data");

            UUID removedOrderId = UUID.randomUUID();
            UUID additionalOrderId = UUID.randomUUID();
            UUID hearingOrderBundleId = UUID.randomUUID();

            Element<HearingOrder> orderToBeRemoved = element(removedOrderId, HearingOrder.builder()
                .status(DRAFT)
                .type(HearingOrderType.DRAFT_CMO)
                .build());

            List<Element<HearingOrder>> caseManagementOrders = newArrayList(
                orderToBeRemoved,
                element(additionalOrderId, HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build()));

            List<Element<HearingBooking>> hearingBookings = List.of(
                element(HearingBooking.builder()
                    .caseManagementOrderId(removedOrderId)
                    .build()));

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(caseManagementOrders).build())
                ))
                .hearingDetails(hearingBookings)
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020"))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

            CaseData responseData = extractCaseData(response);
            HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

            assertThat(responseData.getHearingOrdersBundlesDrafts()).isEqualTo(newArrayList(
                element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(newArrayList(
                    element(additionalOrderId, HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build())
                )).build())
            ));

            assertNull(unlinkedHearing.getCaseManagementOrderId());

            assertThat(response.getData()).doesNotContainKeys(
                "orderTitleToBeRemoved",
                "orderToBeRemoved",
                "showRemoveCMOFieldsFlag",
                "showReasonFieldFlag"
            );
        }
    }

    private HearingOrder buildPastHearingOrder(HearingOrderType type) {
        return HearingOrder.builder()
            .type(type)
            .status((type == AGREED_CMO || type == C21) ? SEND_TO_JUDGE : DRAFT)
            .dateSent(dateNow().minusDays(1))
            .dateIssued(dateNow())
            .build();
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }
}
