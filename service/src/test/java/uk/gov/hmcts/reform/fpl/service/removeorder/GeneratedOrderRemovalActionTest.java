package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.RemovableOrderOrApplicationNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ConfidentialGeneratedOrders;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

class GeneratedOrderRemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID ALREADY_REMOVED_ORDER_ID = UUID.randomUUID();
    private static final String REASON = "Reason";
    private static final UUID CHILD_ONE_ID = java.util.UUID.randomUUID();
    private static final UUID CHILD_TWO_ID = java.util.UUID.randomUUID();
    private static final GeneratedOrderType NON_FINAL_ORDER = SUPERVISION_ORDER;
    private static final Order NON_FINAL_ORDER_NEW = Order.C21_BLANK_ORDER;
    private static final GeneratedOrderType FINAL_ORDER = EMERGENCY_PROTECTION_ORDER;
    private static final Order FINAL_ORDER_NEW = Order.C32A_CARE_ORDER;
    private static final LocalDateTime DATE_OF_ISSUE = LocalDateTime.of(2020, 6, 15, 1, 2);

    private final GeneratedOrderRemovalAction underTest = new GeneratedOrderRemovalAction();

    @Test
    void isAcceptedIfGeneratedOrder() {
        assertThat(underTest.isAccepted(mock(GeneratedOrder.class))).isTrue();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Nested
    class GeneratedOrders {

        @Test
        void shouldGetRemovedOrderWhenNotFinal() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ), YesNo.NO.getValue()
            );

            JudgeAndLegalAdvisor emptyJudge = JudgeAndLegalAdvisor.builder().build();
            GeneratedOrder order = generatedOrder.toBuilder().judgeAndLegalAdvisor(emptyJudge).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, order)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, order);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER_NEW,
                        "order 1",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON, YesNo.NO.getValue()
                    ))
                )
            ));

        }

        @Test
        void shouldThrowExceptionIfOrderNotFound() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ), YesNo.NO.getValue()
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, ALREADY_REMOVED_ORDER_ID,
                generatedOrder))
                .isInstanceOf(RemovableOrderOrApplicationNotFoundException.class)
                .hasMessage("Removable order or application with id %s not found", ALREADY_REMOVED_ORDER_ID);
        }

        @Test
        void shouldGetRemovedOrderWhenNotFinalAndOtherAlreadyHidden() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ), YesNo.NO.getValue()
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .hiddenOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER_NEW,
                        "order 1 deleted",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("No")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ), YesNo.NO.getValue()
                    ))))
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(ALREADY_REMOVED_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER_NEW,
                        "order 1 deleted",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("No")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ), YesNo.NO.getValue()
                    )),
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER_NEW,
                        "order 1",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON, YesNo.NO.getValue()
                    ))
                )
            ));

        }

        @Test
        void shouldGetRemovedOrderAndRevertChildrenMarkersWhenFinal() {
            GeneratedOrder generatedOrder = buildOrder(
                FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ), YesNo.YES.getValue()
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());


            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued(null)
                        .finalOrderIssuedType(null)
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued(null)
                        .finalOrderIssuedType(null)
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        FINAL_ORDER_NEW,
                        "order 1",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON, YesNo.YES.getValue()
                    ))
                )
            ));

        }

        @Test
        void shouldGetRemovedOrderAndRevertOnlyRelatedChildrenMarkersWhenFinal() {
            GeneratedOrder generatedOrder = buildOrder(
                FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                newArrayList(
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ), YesNo.YES.getValue()
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued(null)
                        .finalOrderIssuedType(null)
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        FINAL_ORDER_NEW,
                        "order 1",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON, YesNo.YES.getValue()
                    ))
                )
            ));
        }

        @Test
        void shouldPopulateGeneratedOrderCaseFieldsFromRemovedFinalOrder() {
            GeneratedOrder generatedOrder = buildOrder(
                FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                YesNo.YES.getValue()
            );

            DocumentReference document = DocumentReference.builder().build();
            generatedOrder = generatedOrder.toBuilder().document(document).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap)
                .extracting("orderToBeRemoved",
                    "orderTitleToBeRemoved",
                    "orderIssuedDateToBeRemoved",
                    "orderDateToBeRemoved")
                .containsExactly(document,
                    generatedOrder.getTitle(),
                    "15 June 2020",
                    generatedOrder.getRemovalReason());
        }

        @Test
        void shouldPopulateGeneratedOrderCaseFieldsFromRemovedUploadedOrder() {
            String orderType = "Appointment of a children's guardian (C47A)";

            GeneratedOrder generatedOrder = GeneratedOrder.builder()
                .type(orderType)
                .orderType(Order.C46A_APPOINTMENT_OF_A_GUARDIAN.name())
                .title(null)
                .dateTimeIssued(DATE_OF_ISSUE)
                .build();

            DocumentReference document = DocumentReference.builder().build();
            generatedOrder = generatedOrder.toBuilder().document(document).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap)
                .extracting("orderToBeRemoved",
                    "orderTitleToBeRemoved",
                    "orderIssuedDateToBeRemoved",
                    "orderDateToBeRemoved")
                .containsExactly(document,
                    orderType,
                    "15 June 2020",
                    generatedOrder.getRemovalReason());
        }

        @Test
        void shouldPopulateGeneratedOrderCaseFieldsFromConfidentialOrder() {
            String orderType = "Appointment of a children's guardian (C47A)";

            GeneratedOrder generatedOrder = GeneratedOrder.builder()
                .type(orderType)
                .title(null)
                .dateOfIssue("15 June 2020")
                .build();

            DocumentReference document = DocumentReference.builder().build();
            generatedOrder = generatedOrder.toBuilder().document(document).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .confidentialOrders(ConfidentialGeneratedOrders.builder()
                    .orderCollectionCTSC(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                    .build())
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap)
                .extracting("orderToBeRemoved",
                    "orderTitleToBeRemoved",
                    "orderIssuedDateToBeRemoved",
                    "orderDateToBeRemoved")
                .containsExactly(document,
                    orderType,
                    generatedOrder.getDateOfIssue(),
                    generatedOrder.getRemovalReason());
        }

        @Test
        void shouldRemoveConfidentialOrder() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER_NEW,
                "order 1",
                DATE_OF_ISSUE,
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ), YesNo.NO.getValue()
            );

            JudgeAndLegalAdvisor emptyJudge = JudgeAndLegalAdvisor.builder().build();
            GeneratedOrder order = generatedOrder.toBuilder().judgeAndLegalAdvisor(emptyJudge).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .confidentialOrders(ConfidentialGeneratedOrders.builder()
                    .orderCollectionCTSC(newArrayList(element(TO_REMOVE_ORDER_ID, order)))
                    .orderCollectionChild0(newArrayList(element(TO_REMOVE_ORDER_ID, order)))
                    .build())
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, order);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER_NEW,
                        "order 1",
                        DATE_OF_ISSUE,
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON, YesNo.NO.getValue()
                    )))
            ));

        }

        private GeneratedOrder buildOrder(Order type, String title, LocalDateTime dateOfIssue, String markedFinal) {
            return GeneratedOrder.builder()
                .type(type.getHistoryTitle())
                .markedFinal(markedFinal)
                .orderType(type.name())
                .title(title)
                .dateTimeIssued(dateOfIssue)
                .build();
        }

        private GeneratedOrder buildOrder(Order type, String title, LocalDateTime dateOfIssue,
                                          List<Element<Child>> children, String markedFinal) {
            return buildOrder(type, title, dateOfIssue, markedFinal).toBuilder()
                .children(children)
                .build();
        }

        private GeneratedOrder buildOrder(Order type, String title, LocalDateTime dateOfIssue,
                                          List<Element<Child>> children, String removalReason, String markedFinal) {
            return buildOrder(type, title, dateOfIssue, markedFinal).toBuilder()
                .children(children)
                .removalReason(removalReason)
                .build();
        }
    }

    @Nested
    class LegacyGeneratedOrders {

        @Test
        void shouldGetRemovedOrderWhenNotFinal() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER,
                "order 1",
                "15 June 2020",
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                )
            );

            JudgeAndLegalAdvisor emptyJudge = JudgeAndLegalAdvisor.builder().build();
            GeneratedOrder order = generatedOrder.toBuilder().judgeAndLegalAdvisor(emptyJudge).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, order)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, order);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER,
                        "order 1",
                        "15 June 2020",
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON
                    ))
                )
            ));

        }

        @Test
        void shouldThrowExceptionIfOrderNotFound() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER,
                "order 1",
                "15 June 2020",
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                )
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, ALREADY_REMOVED_ORDER_ID,
                generatedOrder))
                .isInstanceOf(RemovableOrderOrApplicationNotFoundException.class)
                .hasMessage("Removable order or application with id %s not found", ALREADY_REMOVED_ORDER_ID);
        }

        @Test
        void shouldGetRemovedOrderWhenNotFinalAndOtherAlreadyHidden() {
            GeneratedOrder generatedOrder = buildOrder(
                NON_FINAL_ORDER,
                "order 1",
                "15 June 2020",
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                )
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .hiddenOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER,
                        "order 1 deleted",
                        "15 June 2020",
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("No")
                                .finalOrderIssuedType("Some type")
                                .build())
                        )
                    ))))
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))

                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(ALREADY_REMOVED_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER,
                        "order 1 deleted",
                        "15 June 2020",
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("No")
                                .finalOrderIssuedType("Some type")
                                .build())
                        )
                    )),
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        NON_FINAL_ORDER,
                        "order 1",
                        "15 June 2020",
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON
                    ))
                )
            ));

        }

        @Test
        void shouldGetRemovedOrderAndRevertChildrenMarkersWhenFinal() {
            GeneratedOrder generatedOrder = buildOrder(
                FINAL_ORDER,
                "order 1",
                "15 June 2020",
                newArrayList(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                )
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());


            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued(null)
                        .finalOrderIssuedType(null)
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued(null)
                        .finalOrderIssuedType(null)
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        FINAL_ORDER,
                        "order 1",
                        "15 June 2020",
                        newArrayList(
                            element(CHILD_ONE_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build()),
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON
                    ))
                )
            ));

        }

        @Test
        void shouldGetRemovedOrderAndRevertOnlyRelatedChildrenMarkersWhenFinal() {
            GeneratedOrder generatedOrder = buildOrder(
                FINAL_ORDER,
                "order 1",
                "15 June 2020",
                newArrayList(
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                )
            );

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .children1(List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build())
                ))
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_ONE_ID, Child.builder()
                        .finalOrderIssued("Yes")
                        .finalOrderIssuedType("Some type")
                        .build()),
                    element(CHILD_TWO_ID, Child.builder()
                        .finalOrderIssued(null)
                        .finalOrderIssuedType(null)
                        .build())
                ),
                "hiddenOrders", List.of(
                    element(TO_REMOVE_ORDER_ID, buildOrder(
                        FINAL_ORDER,
                        "order 1",
                        "15 June 2020",
                        newArrayList(
                            element(CHILD_TWO_ID, Child.builder()
                                .finalOrderIssued("Yes")
                                .finalOrderIssuedType("Some type")
                                .build())
                        ),
                        REASON
                    ))
                )
            ));
        }

        @Test
        void shouldPopulateGeneratedOrderCaseFieldsFromRemovedFinalOrder() {
            GeneratedOrder generatedOrder = buildOrder(
                FINAL_ORDER,
                "order 1",
                "15 June 2020"
            );

            DocumentReference document = DocumentReference.builder().build();
            generatedOrder = generatedOrder.toBuilder().document(document).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap)
                .extracting("orderToBeRemoved",
                    "orderTitleToBeRemoved",
                    "orderIssuedDateToBeRemoved",
                    "orderDateToBeRemoved")
                .containsExactly(document,
                    generatedOrder.getTitle(),
                    generatedOrder.getDateOfIssue(),
                    generatedOrder.getRemovalReason());
        }

        @Test
        void shouldPopulateGeneratedOrderCaseFieldsFromRemovedUploadedOrder() {
            String orderType = "Appointment of a children's guardian (C47A)";

            GeneratedOrder generatedOrder = GeneratedOrder.builder()
                .type(orderType)
                .title(null)
                .dateOfIssue("15 June 2020")
                .build();

            DocumentReference document = DocumentReference.builder().build();
            generatedOrder = generatedOrder.toBuilder().document(document).build();

            CaseData caseData = CaseData.builder()
                .removalToolData(RemovalToolData.builder()
                    .reasonToRemoveOrder(REASON)
                    .build())
                .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
                .build();

            CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
                .data(Map.of())
                .build());

            underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, generatedOrder);

            assertThat(caseDetailsMap)
                .extracting("orderToBeRemoved",
                    "orderTitleToBeRemoved",
                    "orderIssuedDateToBeRemoved",
                    "orderDateToBeRemoved")
                .containsExactly(document,
                    orderType,
                    generatedOrder.getDateOfIssue(),
                    generatedOrder.getRemovalReason());
        }

        private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String dateOfIssue) {
            return GeneratedOrder.builder()
                .type(getFullOrderType(type))
                .title(title)
                .dateOfIssue(dateOfIssue)
                .build();
        }

        private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String dateOfIssue,
                                          List<Element<Child>> children) {
            return buildOrder(type, title, dateOfIssue).toBuilder()
                .children(children)
                .build();
        }

        private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String dateOfIssue,
                                          List<Element<Child>> children, String removalReason) {
            return buildOrder(type, title, dateOfIssue).toBuilder()
                .children(children)
                .removalReason(removalReason)
                .build();
        }
    }

}
