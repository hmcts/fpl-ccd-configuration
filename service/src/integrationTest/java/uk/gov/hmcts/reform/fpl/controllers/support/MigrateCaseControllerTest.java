package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla2684 {
        String migrationId = "FPLA-2684";
        String familyManNumber = "CF20C50070";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderOneId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID childrenId = UUID.randomUUID();

        @Test
        void shouldRemoveThirdGeneratedOrderAndNotModifyChildren() {
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
            Element<GeneratedOrder> orderThree = element(orderToBeRemovedId, generateOrder(CARE_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(orderOne, orderTwo));
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
            Element<GeneratedOrder> orderThree = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree);

            List<Element<GeneratedOrder>> hiddenOrders = newArrayList(
                element(GeneratedOrder.builder().build()));

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, orderCollection, children);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(orderOne, orderTwo));
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
            Element<GeneratedOrder> orderThree = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree);

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
            Element<GeneratedOrder> orderThree = element(orderToBeRemovedId,
                generateOrder(EMERGENCY_PROTECTION_ORDER, children));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(orderOne, orderTwo, orderThree);

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
                .hasMessage("Expected at least three orders but found 2");
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainGeneratedOrders() {
            List<Element<Child>> children = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null, children);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least three orders but found 0");
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
    }
}
