package uk.gov.hmcts.reform.fpl.controllers.support;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla2693 {
        String migrationId = "FPLA-2693";
        String familyManNumber = "SA20C50008";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderOneId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID orderThreeId = UUID.randomUUID();
        UUID orderFourId = UUID.randomUUID();
        UUID orderFiveId = UUID.randomUUID();
        UUID orderSixId = UUID.randomUUID();
        UUID orderSevenId = UUID.randomUUID();
        UUID orderEightId = UUID.randomUUID();
        UUID orderNineId = UUID.randomUUID();
        UUID childrenId = UUID.randomUUID();

        @Test
        void shouldRemoveTenthGeneratedOrderAndNotModifyChildren() {
            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssuedType("Yes")
                .finalOrderIssued("Yes")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSeven = element(orderSevenId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderEight = element(orderEightId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderNine = element(orderNineId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTen = element(orderToBeRemovedId, generateOrder(CARE_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree, orderFour,
                orderFive, orderSix, orderSeven, orderEight, orderNine, orderTen);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(orderOne, orderTwo, orderThree,
                orderFour, orderFive, orderSix, orderSeven, orderEight, orderNine));
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
            assertThat(extractedCaseData.getHiddenOrders()).isEmpty();
        }

        @Test
        void shouldUnsetFinalChildrenPropertiesWhenRemovingFinalOrder() {
            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssuedType("Yes")
                .finalOrderIssued("Yes")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSeven = element(orderSevenId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderEight = element(orderEightId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderNine = element(orderNineId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTen = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree, orderFour,
                orderFive, orderSix, orderSeven, orderEight, orderNine, orderTen);

            List<Element<GeneratedOrder>> hiddenOrders = newArrayList(
                element(GeneratedOrder.builder().build()));

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, orderCollection, children);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(orderOne, orderTwo, orderThree,
                orderFour, orderFive, orderSix, orderSeven, orderEight, orderNine));

            assertThat(extractedCaseData.getChildren1()).isEqualTo(List.of(
                element(childrenId, Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Tom")
                        .lastName("Wilson")
                        .build())
                    .finalOrderIssuedType(null)
                    .finalOrderIssued(null)
                    .build())));
            assertThat(extractedCaseData.getHiddenOrders()).isEqualTo(hiddenOrders);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssued("Test")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSeven = element(orderSevenId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderEight = element(orderEightId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderNine = element(orderNineId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTen = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree, orderFour,
                orderFive, orderSix, orderSeven, orderEight, orderNine, orderTen);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orderCollection);
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "CF20C50071";

            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssued("Test")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSeven = element(orderSevenId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderEight = element(orderEightId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderNine = element(orderNineId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTen = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree, orderFour,
                orderFive, orderSix, orderSeven, orderEight, orderNine, orderTen);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orderCollection);
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsTwoGeneratedOrders() {
            List<Element<Child>> children = newArrayList(newArrayList());
            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, List.of(orderOne, orderTwo), children);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least ten orders but found 2");
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainGeneratedOrders() {
            List<Element<Child>> children = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null, children);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least ten orders but found 0");
        }
    }

    @Nested
    class Fpla2716 {
        String migrationId = "FPLA-2716";
        String familyManNumber = "CF21C50009";
        UUID ID_ONE = UUID.randomUUID();
        UUID ID_TWO = UUID.randomUUID();

        @Test
        void shouldRemoveC21FromFirstHearingBundle() {
            HearingOrder cmoOrder = HearingOrder.builder()
                .type(HearingOrderType.DRAFT_CMO)
                .status(CMOStatus.DRAFT)
                .order(testDocumentReference())
                .build();

            HearingOrder orderToBeRemoved = HearingOrder.builder()
                .type(HearingOrderType.C21)
                .status(CMOStatus.SEND_TO_JUDGE)
                .order(testDocumentReference())
                .build();


            Element<HearingOrdersBundle> hearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
                .orders(Lists.newArrayList(element(ID_ONE, cmoOrder),
                    element(ID_TWO, orderToBeRemoved))).build());

            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = List.of(hearingBundle);

            CaseDetails caseDetails = caseDetailsWithHearingOrdersBundle(migrationId, familyManNumber, hearingOrdersBundlesDrafts);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingOrdersBundlesDrafts().get(0).getValue().getOrders()).isEqualTo(Lists.newArrayList(element(ID_ONE, cmoOrder)));
        }

        @Test
        void shouldThrowAnExceptionIfFirstHearingBundleDoesNotContainTwoOrders() {
            HearingOrder cmoOrder = HearingOrder.builder()
                .type(HearingOrderType.DRAFT_CMO)
                .status(CMOStatus.DRAFT)
                .order(testDocumentReference())
                .build();

            Element<HearingOrdersBundle> hearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
                .orders(Lists.newArrayList(element(ID_ONE, cmoOrder))).build());

            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = List.of(hearingBundle);

            CaseDetails caseDetails = caseDetailsWithHearingOrdersBundle(migrationId, familyManNumber, hearingOrdersBundlesDrafts);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least 2 orders in hearing order bundle but found 1");
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-2997";

            HearingOrder cmoOrder = HearingOrder.builder()
                .type(HearingOrderType.DRAFT_CMO)
                .status(CMOStatus.DRAFT)
                .order(testDocumentReference())
                .build();

            HearingOrder orderToBeRemoved = HearingOrder.builder()
                .type(HearingOrderType.C21)
                .status(CMOStatus.SEND_TO_JUDGE)
                .order(testDocumentReference())
                .build();

            Element<HearingOrdersBundle> hearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
                .orders(Lists.newArrayList(element(ID_ONE, cmoOrder),
                    element(ID_TWO, orderToBeRemoved))).build());

            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = List.of(hearingBundle);

            CaseDetails caseDetails = caseDetailsWithHearingOrdersBundle(incorrectMigrationId, familyManNumber, hearingOrdersBundlesDrafts);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingOrdersBundlesDrafts()).isEqualTo(hearingOrdersBundlesDrafts);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "CF20C50071";

            HearingOrder cmoOrder = HearingOrder.builder()
                .type(HearingOrderType.DRAFT_CMO)
                .status(CMOStatus.DRAFT)
                .order(testDocumentReference())
                .build();

            HearingOrder orderToBeRemoved = HearingOrder.builder()
                .type(HearingOrderType.C21)
                .status(CMOStatus.SEND_TO_JUDGE)
                .order(testDocumentReference())
                .build();

            Element<HearingOrdersBundle> hearingBundle = element(UUID.randomUUID(), HearingOrdersBundle.builder()
                .orders(Lists.newArrayList(element(ID_ONE, cmoOrder),
                    element(ID_TWO, orderToBeRemoved))).build());

            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = List.of(hearingBundle);

            CaseDetails caseDetails = caseDetailsWithHearingOrdersBundle(migrationId, incorrectFamilyManNumber, hearingOrdersBundlesDrafts);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingOrdersBundlesDrafts()).isEqualTo(hearingOrdersBundlesDrafts);
        }
    }

    private CaseDetails caseDetails(String migrationId,
                                    String familyManNumber,
                                    List<Element<GeneratedOrder>> orders,
                                    List<Element<Child>> children) {
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .familyManCaseNumber(familyManNumber)
            .orderCollection(orders)
            .children1(children)
            .build());

        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    private CaseDetails caseDetailsWithHearingOrdersBundle(String migrationId,
                                    String familyManNumber, List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts) {
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
            .familyManCaseNumber(familyManNumber)
            .build());

        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    private GeneratedOrder generateOrder(GeneratedOrderType type, List<Element<Child>> linkedChildren) {
        return generateOrder(type).toBuilder()
            .children(linkedChildren)
            .build();
    }

    private GeneratedOrder generateOrder(GeneratedOrderType type) {
        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .build();
    }

    private HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
            .caseManagementOrderId(cmoId)
            .build();
    }

}
