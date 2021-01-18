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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final UUID CMO_ID_1 = UUID.randomUUID();
    private static final UUID CMO_ID_2 = UUID.randomUUID();
    private static final UUID HEARING_ID_1 = UUID.randomUUID();
    private static final UUID HEARING_ID_2 = UUID.randomUUID();
    private static final CaseManagementOrder CMO = CaseManagementOrder.builder().build();

    @Nested
    class Fpla2480 {
        String familyManNumber = "LE20C50003";
        String migrationId = "FPLA-2480";

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<CaseManagementOrder> orderToBeRemoved = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> additionalOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingToBeRemoved = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> additionalHearing = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(HEARING_ID_1, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> orderToBeRemoved = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> additionalOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingToBeRemoved = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> additionalHearing = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> orderToBeRemoved = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> additionalOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingToBeRemoved = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> additionalHearing = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, invalidFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<CaseManagementOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private HearingBooking hearing(UUID cmoId) {
            return HearingBooking.builder()
                .caseManagementOrderId(cmoId)
                .build();
        }
    }

    @Nested
    class Fpla2535 {
        Long caseNumber = 1595320156232721L;
        String migrationId = "FPLA-2608";
        UUID orderOneId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        UUID orderThreeId = UUID.randomUUID();
        UUID orderFourId = UUID.randomUUID();
        UUID orderFiveId = UUID.randomUUID();
        UUID orderSixId = UUID.randomUUID();
        UUID orderSevenId = UUID.randomUUID();
        UUID orderEightId = UUID.randomUUID();

        UUID childrenId = UUID.randomUUID();

        @Test
        void shouldRemoveGeneratedOrdersSixAndSevenWithoutAlteringChildren() {
            Element<Child> childElement = element(childrenId, Child.builder()
                .party(ChildParty.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .build())
                .finalOrderIssuedType("Yes")
                .finalOrderIssued("Yes")
                .build());

            List<Element<Child>> children = newArrayList(childElement);

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(CARE_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSeven = element(orderSevenId, generateOrder(BLANK_ORDER, children));
            Element<GeneratedOrder> orderEight = element(orderEightId, generateOrder(BLANK_ORDER));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderOne,
                orderTwo,
                orderThree,
                orderFour,
                orderFive,
                orderSix,
                orderSeven,
                orderEight);

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, orderCollection, children);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(
                orderOne,
                orderTwo,
                orderThree,
                orderFour,
                orderFive,
                orderEight
            ));

            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
            assertThat(extractedCaseData.getHiddenOrders()).isEqualTo(List.of());
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

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(CARE_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSeven = element(orderSevenId, generateOrder(EMERGENCY_PROTECTION_ORDER,
                children));
            
            Element<GeneratedOrder> orderEight = element(orderEightId, generateOrder(BLANK_ORDER));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderOne,
                orderTwo,
                orderThree,
                orderFour,
                orderFive,
                orderSix,
                orderSeven,
                orderEight);

            List<Element<GeneratedOrder>> hiddenOrders = newArrayList(
                element(GeneratedOrder.builder().build()));

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, orderCollection, children);

            caseDetails.getData().put("hiddenOrders", hiddenOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(List.of(
                orderOne,
                orderTwo,
                orderThree,
                orderFour,
                orderFive,
                orderEight
            ));

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

            Element<GeneratedOrder> orderToBeRemoved = element(orderOneId, generateOrder(UPLOAD));
            Element<GeneratedOrder> additionalOrder = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<Child> childElement = element(childrenId, Child.builder()
                .finalOrderIssuedType("Test")
                .finalOrderIssued("Test")
                .build());

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<Child>> children = newArrayList(childElement);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, caseNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orderCollection);
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            Long incorrectCaseNumber = 1599470847274973L;

            Element<GeneratedOrder> orderToBeRemoved = element(orderOneId, generateOrder(UPLOAD));
            Element<GeneratedOrder> additionalOrder = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<Child> childElement = element(childrenId, Child.builder()
                .finalOrderIssuedType("Test")
                .finalOrderIssued("Test")
                .build());

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<Child>> children = newArrayList(childElement);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectCaseNumber, orderCollection, children);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orderCollection);
            assertThat(extractedCaseData.getChildren1()).isEqualTo(children);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsLessThanSevenGeneratedOrders() {
            List<Element<Child>> children = newArrayList(newArrayList());

            Element<GeneratedOrder> orderOne = element(orderOneId, generateOrder(CARE_ORDER));
            Element<GeneratedOrder> orderTwo = element(orderTwoId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderThree = element(orderThreeId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFour = element(orderFourId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderFive = element(orderFiveId, generateOrder(BLANK_ORDER));
            Element<GeneratedOrder> orderSix = element(orderSixId, generateOrder(BLANK_ORDER));

            List<Element<GeneratedOrder>> orderCollection = newArrayList(
                orderOne,
                orderTwo,
                orderThree,
                orderFour,
                orderFive,
                orderSix);

            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, orderCollection, children);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected to have at least 8 generated orders but found 6");
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainGeneratedOrders() {
            List<Element<Child>> children = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, caseNumber, null, children);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected to have at least 8 generated orders but found 0");
        }

        private CaseDetails caseDetails(String migrationId,
                                        Long caseNumber,
                                        List<Element<GeneratedOrder>> orders,
                                        List<Element<Child>> children) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .id(caseNumber)
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
