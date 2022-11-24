package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.EventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
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

    @MockBean
    private EventService eventPublisher;

    @Captor
    private ArgumentCaptor<DraftOrdersRemovedEvent> draftOrdersRemovedEventCaptor;


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
                    buildListElement(draftCMOOne.getId(),
                        format("Draft case management order sent on %s, %s",
                            formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"),
                            draftCMOOne.getValue().getDocument().getFilename())),
                    buildListElement(draftOrderOne.getId(), format("Draft order sent on %s for %s, %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"),
                        draftOrderOne.getValue().getTitle(),
                        draftOrderOne.getValue().getDocument().getFilename())),
                    buildListElement(draftCMOTwo.getId(), format("Draft case management order sent on %s, %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"),
                        draftCMOTwo.getValue().getDocument().getFilename())),
                    buildListElement(agreedCMO.getId(), format("Agreed case management order sent on %s, %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"),
                        agreedCMO.getValue().getDocument().getFilename())),
                    buildListElement(draftOrderTwo.getId(), format("Draft order sent on %s for %s, %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"),
                        draftOrderTwo.getValue().getTitle(),
                        draftOrderTwo.getValue().getDocument().getFilename())),
                    buildListElement(draftCMOThree.getId(), format("Draft case management order sent on %s, %s",
                        formatLocalDateToString(dateNow().minusDays(1), "d MMMM yyyy"),
                        draftCMOThree.getValue().getDocument().getFilename()))
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
                .removalToolData(RemovalToolData.builder().removableOrderList(dynamicList).build())
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
                .removalToolData(RemovalToolData.builder().removableOrderList(DynamicList.builder()
                    .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020"))
                    .build()).build())
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

    @Nested
    class SubmittedTest {
        @Test
        void shouldTriggerEvent() {
            Long caseId = 1L;
            UUID removedOrderId = UUID.randomUUID();
            UUID additionalOrderId = UUID.randomUUID();
            UUID hearingOrderBundleId = UUID.randomUUID();

            Element<HearingOrder> orderToBeRemoved = element(removedOrderId, HearingOrder.builder()
                .status(DRAFT)
                .type(HearingOrderType.DRAFT_CMO)
                .build());

            List<Element<HearingOrder>> caseManagementOrdersBefore = newArrayList(
                orderToBeRemoved,
                element(additionalOrderId, HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build()));

            List<Element<HearingBooking>> hearingBookings = List.of(
                element(HearingBooking.builder()
                    .caseManagementOrderId(removedOrderId)
                    .build()));

            RemovalToolData removalData = RemovalToolData.builder()
                .removableOrderList(DynamicList.builder()
                    .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020")).build())
                .reasonToRemoveOrder("Removal reason").build();

            CaseData caseDataBefore = CaseData.builder()
                .id(caseId)
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(hearingOrderBundleId, HearingOrdersBundle.builder()
                        .orders(caseManagementOrdersBefore).build())
                ))
                .hearingDetails(hearingBookings)
                .removalToolData(removalData)
                .build();


            List<Element<HearingOrder>> caseManagementOrdersAfter = newArrayList(
                element(additionalOrderId, HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build()));

            CaseData caseDataAfter = CaseData.builder()
                .id(caseId)
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(hearingOrderBundleId,
                        HearingOrdersBundle.builder().orders(caseManagementOrdersAfter).build())
                ))
                .hearingDetails(hearingBookings)
                .removalToolData(removalData)
                .build();

            postSubmittedEvent(toCallBackRequest(caseDataAfter, caseDataBefore));

            verify(eventPublisher).publishEvent(draftOrdersRemovedEventCaptor.capture());

            assertThat(draftOrdersRemovedEventCaptor.getValue().getCaseData().getHearingOrdersBundlesDrafts())
                .isEqualTo(caseDataAfter.getHearingOrdersBundlesDrafts());
            assertThat(draftOrdersRemovedEventCaptor.getValue().getCaseDataBefore().getHearingOrdersBundlesDrafts())
                .isEqualTo(caseDataBefore.getHearingOrdersBundlesDrafts());
            assertThat(draftOrdersRemovedEventCaptor.getValue().getDraftOrderRemoved())
                .isEqualTo(orderToBeRemoved);
            assertThat(draftOrdersRemovedEventCaptor.getValue().getRemovalReason())
                .isEqualTo(removalData.getReasonToRemoveOrder());
        }
    }

    private HearingOrder buildPastHearingOrder(HearingOrderType type) {
        return HearingOrder.builder()
            .type(type)
            .title("test order")
            .order(DocumentReference.builder().filename("order.doc").build())
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
