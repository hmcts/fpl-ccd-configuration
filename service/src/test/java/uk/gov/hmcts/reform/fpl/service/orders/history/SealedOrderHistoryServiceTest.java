package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.WORD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

class SealedOrderHistoryServiceTest {

    private static final Judge JUDGE = mock(Judge.class);
    private static final Order ORDER_TYPE = Order.C32A_CARE_ORDER;
    private static final LocalDate TODAY = LocalDate.of(2012, 12, 22);
    private static final LocalDateTime NOW = TODAY.atStartOfDay();
    private static final LocalDate APPROVAL_DATE = LocalDate.of(2010, 11, 6);
    private static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2010, 11, 6, 11, 10, 10);
    private static final String CHILD_1_FULLNAME = "child1fullname";
    private static final String CHILD_2_FULLNAME = "child1fullname";
    private static final JudgeAndLegalAdvisor JUDGE_AND_LEGAL_ADVISOR = mock(JudgeAndLegalAdvisor.class);
    private static final JudgeAndLegalAdvisor TAB_JUDGE_AND_LEGAL_ADVISOR = mock(JudgeAndLegalAdvisor.class);
    private static final UUID UUID_1 = java.util.UUID.randomUUID();
    private static final Element<GeneratedOrder> ORDER_APPROVED_IN_THE_PAST = element(UUID_1,
        GeneratedOrder.builder()
            .approvalDate(APPROVAL_DATE.minusDays(1))
            .build());
    private static final Element<GeneratedOrder> ORDER_APPROVED_IN_THE_FUTURE = element(UUID_1,
        GeneratedOrder.builder()
            .approvalDate(APPROVAL_DATE.plusDays(1))
            .build());
    private static final Element<GeneratedOrder> ORDER_APPROVED_DATE_TIME_FOR_SAME_DAY = element(UUID_1,
        GeneratedOrder.builder()
            .approvalDateTime(APPROVAL_DATE_TIME)
            .dateTimeIssued(NOW.minusSeconds(1))
            .build());
    private static final Element<GeneratedOrder> ORDER_APPROVED_LEGACY = element(UUID_1,
        GeneratedOrder.builder()
            .approvalDate(null)
            .build());
    private static final UUID GENERATED_ORDER_UUID = java.util.UUID.randomUUID();
    private static final DocumentReference SEALED_PDF_DOCUMENT = mock(DocumentReference.class);
    private static final DocumentReference PLAIN_WORD_DOCUMENT = mock(DocumentReference.class);
    private static final String EXTRA_TITLE = "ExtraTitle";
    private final Child child1 = mock(Child.class);
    private final Child child2 = mock(Child.class);

    private final ChildrenService childrenService = mock(ChildrenService.class);
    private final IdentityService identityService = mock(IdentityService.class);
    private final OrderCreationService orderCreationService = mock(OrderCreationService.class);
    private final Time time = mock(Time.class);
    private final ManageOrdersClosedCaseFieldGenerator manageOrdersClosedCaseFieldGenerator = mock(
        ManageOrdersClosedCaseFieldGenerator.class);
    private final SealedOrderHistoryExtraTitleGenerator extraTitleGenerator =
        mock(SealedOrderHistoryExtraTitleGenerator.class);

    private static final UUID LINKED_APPLICATION_ID = UUID.randomUUID();
    private static final DynamicList SELECTED_LINKED_APPLICATION_LIST = buildDynamicList(0,
        Pair.of(LINKED_APPLICATION_ID, "My test application"));

    private final SealedOrderHistoryService underTest = new SealedOrderHistoryService(
        identityService,
        childrenService,
        orderCreationService,
        extraTitleGenerator,
        time,
        manageOrdersClosedCaseFieldGenerator
    );

    @Nested
    class Generate {

        @BeforeEach
        void setUp() {
            when(child1.asLabel()).thenReturn(CHILD_1_FULLNAME);
            when(child2.asLabel()).thenReturn(CHILD_2_FULLNAME);
            when(time.now()).thenReturn(NOW);
            when(identityService.generateId()).thenReturn(GENERATED_ORDER_UUID);
        }

        @Test
        void generateWhenNoPreviousOrders() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData().build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder().build())
                    )
                ));
            }
        }

        @Test
        void generateWithOtherClosingExtras() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData().build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));
                when(manageOrdersClosedCaseFieldGenerator.generate(caseData)).thenReturn(
                    Map.of("somethingClose", "closeCaseValue")
                );

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder().build())
                    ),
                    "somethingClose", "closeCaseValue"
                ));
            }
        }

        @Test
        void generateWithNoChildrenDescriptionWhenOrderAppliesToAllChildren() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData().orderAppliesToAllChildren("Yes").build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1, child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder()
                            .childrenDescription(null)
                            .children(wrapElements(child1, child1))
                            .build())
                    )));
            }
        }

        @Test
        void generateWithNoChildrenDescriptionWhenOnlyOneChildInCase() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData()
                    .orderAppliesToAllChildren("No")
                    .children1(wrapElements(child1))
                    .childSelector(Selector.builder().selected(List.of(1)).build())
                    .build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder()
                            .childrenDescription(null)
                            .children(wrapElements(child1))
                            .build())
                    )));
            }
        }

        @Test
        void generateWhenNoPreviousOrdersWithMultipleChildren() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData().build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1, child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder()
                            .children(wrapElements(child1, child1))
                            .childrenDescription(String.format("%s, %s", CHILD_1_FULLNAME, CHILD_2_FULLNAME))
                            .build())
                    )));
            }
        }

        @Test
        void generateWithPreviousOrdersWithPastApprovalDate() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData()
                    .orderCollection(newArrayList(
                        ORDER_APPROVED_IN_THE_PAST
                    )).build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder().build()),
                        ORDER_APPROVED_IN_THE_PAST
                    )
                ));
            }
        }

        @Test
        void generateWithPreviousOrdersWithLaterApprovalDate() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData()
                    .orderCollection(newArrayList(
                        ORDER_APPROVED_IN_THE_FUTURE
                    )).build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        ORDER_APPROVED_IN_THE_FUTURE,
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder().build())
                    )
                ));
            }
        }

        @Test
        void generateWithPreviousOrdersWithSameApprovalDate() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseData()
                    .orderCollection(newArrayList(
                        ORDER_APPROVED_DATE_TIME_FOR_SAME_DAY,
                        ORDER_APPROVED_IN_THE_FUTURE,
                        ORDER_APPROVED_LEGACY
                    )).build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        ORDER_APPROVED_IN_THE_FUTURE,
                        ORDER_APPROVED_DATE_TIME_FOR_SAME_DAY,
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrder().build()),
                        ORDER_APPROVED_LEGACY
                    )
                ));
            }
        }

        @Test
        void generateWithPreviousLegacyOrdersWithoutApprovalDate_WithLinkedApplication() {
            try (MockedStatic<JudgeAndLegalAdvisorHelper> jalMock =
                     Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
                mockHelper(jalMock);
                CaseData caseData = caseDataWithLinkedApplication()
                    .orderCollection(newArrayList(
                        ORDER_APPROVED_LEGACY
                    )).build();
                mockDocumentUpload(caseData);
                mockExtraTitleGenerator(caseData);
                when(childrenService.getSelectedChildren(caseData)).thenReturn(wrapElements(child1));

                Map<String, Object> actual = underTest.generate(caseData);

                assertThat(actual).isEqualTo(Map.of(
                    "orderCollection", List.of(
                        element(GENERATED_ORDER_UUID, expectedGeneratedOrderWithLinkedApplication().build()),
                        ORDER_APPROVED_LEGACY
                    )
                ));
            }
        }
    }

    @Nested
    class LastGeneratedOrder {

        @Test
        void testEmptyElements() {
            assertThrows(IllegalStateException.class, () -> underTest.lastGeneratedOrder(CaseData.builder().build()));
        }

        @Test
        void testSingleElement() {
            GeneratedOrder order = GeneratedOrder.builder()
                .dateTimeIssued(NOW)
                .build();

            GeneratedOrder actual = underTest.lastGeneratedOrder(CaseData.builder()
                .orderCollection(wrapElements(order)).build());

            assertThat(actual).isEqualTo(order);
        }

        @Test
        void testElementsInThePast() {
            GeneratedOrder order = GeneratedOrder.builder()
                .dateTimeIssued(NOW)
                .build();
            GeneratedOrder anotherPastOrder = GeneratedOrder.builder()
                .dateTimeIssued(NOW.minusSeconds(1))
                .build();

            GeneratedOrder actual = underTest.lastGeneratedOrder(CaseData.builder()
                .orderCollection(wrapElements(order, anotherPastOrder)).build());

            assertThat(actual).isEqualTo(order);
        }

        @Test
        void testLegacyElementsInThePast() {
            GeneratedOrder order = GeneratedOrder.builder()
                .dateTimeIssued(NOW)
                .build();
            GeneratedOrder anotherPastOrder = GeneratedOrder.builder()
                .dateTimeIssued(null)
                .build();

            GeneratedOrder actual = underTest.lastGeneratedOrder(CaseData.builder()
                .orderCollection(wrapElements(order, anotherPastOrder)).build());

            assertThat(actual).isEqualTo(order);
        }
    }

    private void mockDocumentUpload(CaseData caseData) {
        when(orderCreationService.createOrderDocument(caseData, OrderStatus.SEALED, PDF)).thenReturn(
            SEALED_PDF_DOCUMENT);
        when(orderCreationService.createOrderDocument(caseData, OrderStatus.PLAIN, WORD)).thenReturn(
            PLAIN_WORD_DOCUMENT);
    }

    private CaseData.CaseDataBuilder caseData() {
        return startCommonCaseDataBuilder(startBuildingCommonEventData());
    }

    private CaseData.CaseDataBuilder caseDataWithLinkedApplication() {
        return startCommonCaseDataBuilder(
            startBuildingCommonEventData().manageOrdersLinkedApplication(SELECTED_LINKED_APPLICATION_LIST)
        );
    }

    private CaseData.CaseDataBuilder startCommonCaseDataBuilder(
        ManageOrdersEventData.ManageOrdersEventDataBuilder manageOrdersEventData) {
        return CaseData.builder()
            .allocatedJudge(JUDGE)
            .judgeAndLegalAdvisor(JUDGE_AND_LEGAL_ADVISOR)
            .manageOrdersEventData(manageOrdersEventData.build());
    }

    private ManageOrdersEventData.ManageOrdersEventDataBuilder startBuildingCommonEventData() {
        return ManageOrdersEventData.builder()
            .manageOrdersType(ORDER_TYPE)
            .manageOrdersApprovalDate(APPROVAL_DATE);
    }

    private GeneratedOrder.GeneratedOrderBuilder expectedGeneratedOrder() {
        return startCommonExpectedGeneratedOrderBuilder();
    }

    private GeneratedOrder.GeneratedOrderBuilder expectedGeneratedOrderWithLinkedApplication() {
        return startCommonExpectedGeneratedOrderBuilder().linkedApplicationId(LINKED_APPLICATION_ID.toString());
    }

    private GeneratedOrder.GeneratedOrderBuilder startCommonExpectedGeneratedOrderBuilder() {
        return GeneratedOrder.builder()
            .orderType(ORDER_TYPE.name())
            .type(ORDER_TYPE.getHistoryTitle())
            .title(EXTRA_TITLE)
            .judgeAndLegalAdvisor(TAB_JUDGE_AND_LEGAL_ADVISOR)
            .children(wrapElements(child1))
            .childrenDescription(CHILD_1_FULLNAME)
            .approvalDate(APPROVAL_DATE)
            .document(SEALED_PDF_DOCUMENT)
            .unsealedDocumentCopy(PLAIN_WORD_DOCUMENT)
            .dateTimeIssued(NOW);
    }

    private void mockExtraTitleGenerator(CaseData caseData) {
        when(extraTitleGenerator.generate(caseData)).thenReturn(EXTRA_TITLE);
    }

    private void mockHelper(MockedStatic<JudgeAndLegalAdvisorHelper> jalMock) {
        jalMock.when(() -> JudgeAndLegalAdvisorHelper.getJudgeForTabView(JUDGE_AND_LEGAL_ADVISOR, JUDGE))
            .thenReturn(TAB_JUDGE_AND_LEGAL_ADVISOR);
    }

}
