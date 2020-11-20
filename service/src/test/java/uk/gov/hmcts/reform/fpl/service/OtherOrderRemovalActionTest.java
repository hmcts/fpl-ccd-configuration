package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

class OtherOrderRemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID ALREADY_REMOVED_ORDER_ID = UUID.randomUUID();
    private static final String REASON = "Reason";
    private static final UUID CHILD_ONE_ID = java.util.UUID.randomUUID();
    private static final UUID CHILD_TWO_ID = java.util.UUID.randomUUID();
    private static final GeneratedOrderType NON_FINAL_ORDER = SUPERVISION_ORDER;
    private static final GeneratedOrderType FINAL_ORDER = EMERGENCY_PROTECTION_ORDER;

    private final OtherOrderRemovalAction underTest = new OtherOrderRemovalAction();

    @Test
    void isAcceptedIfGeneratedOrder() {
        assertThat(underTest.isAccepted(mock(GeneratedOrder.class))).isTrue();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

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

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
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

        Map<String, Object> data = Maps.newHashMap();

        underTest.action(caseData, data, TO_REMOVE_ORDER_ID, generatedOrder);

        assertThat(data).isEqualTo(Map.of(
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
            .reasonToRemoveOrder(REASON)
            .orderCollection(newArrayList(element(TO_REMOVE_ORDER_ID, generatedOrder)))
            .build();

        Map<String, Object> data = Maps.newHashMap();

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> underTest.action(caseData, data, ALREADY_REMOVED_ORDER_ID, generatedOrder));

        assertThat(exception.getMessage()).isEqualTo(
            format("Failed to find order matching id %s", ALREADY_REMOVED_ORDER_ID)
        );
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
            .reasonToRemoveOrder(REASON)
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
            .build();

        Map<String, Object> data = Maps.newHashMap();

        underTest.action(caseData, data, TO_REMOVE_ORDER_ID, generatedOrder);

        assertThat(data).isEqualTo(Map.of(
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
            .reasonToRemoveOrder(REASON)
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

        Map<String, Object> data = Maps.newHashMap();

        underTest.action(caseData, data, TO_REMOVE_ORDER_ID, generatedOrder);

        assertThat(data).isEqualTo(Map.of(
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
            .reasonToRemoveOrder(REASON)
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

        Map<String, Object> data = Maps.newHashMap();

        underTest.action(caseData, data, TO_REMOVE_ORDER_ID, generatedOrder);

        assertThat(data).isEqualTo(Map.of(
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
