package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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
    class Fpla2710 {
        final String migrationId = "FPLA-2710";

        final Long caseId1 = 1597234670803750L;
        final Long caseId2 = 1611831571219051L;
        final Long caseId3 = 1611831571219051L;

        final UUID cmoId1 = randomUUID();
        final UUID cmoId2 = randomUUID();

        final DocumentReference order = testDocumentReference();

        @Test
        void shouldCreateANewHearingBundleWhenHearingExistsForDraftCMO() {
            Element<HearingBooking> hearing1 = element(buildHearing(now().plusDays(1), cmoId1));
            Element<HearingOrder> cmoToMigrate = buildCMO(cmoId1, hearing1.getValue().toLabel(), SEND_TO_JUDGE);

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .hearingDetails(newArrayList(hearing1))
                .draftUploadedCMOs(newArrayList(cmoToMigrate))
                .id(caseId2)
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            List<Element<HearingOrdersBundle>> migratedHearingBundles =
                extractedCaseData.getHearingOrdersBundlesDrafts();

            assertThat(unwrapElements(migratedHearingBundles))
                .containsExactlyInAnyOrder(
                    buildDraftOrdersBundle(
                        randomUUID(), newArrayList(cmoToMigrate), hearing1, "").getValue());
        }

        @Test
        void shouldNotRemoveCMOFromDraftUploadedCMOsWhenHearingNotFound() {
            Element<HearingBooking> hearing1 = element(buildHearing(now().plusDays(1), cmoId1));
            Element<HearingBooking> hearing2 = element(buildHearing(now().plusDays(5)));

            Element<HearingOrder> cmoLinkedToHearing = buildCMO(cmoId1, hearing1.getValue().toLabel(), SEND_TO_JUDGE);
            Element<HearingOrder> cmoWithoutHearing = buildCMO(cmoId2, hearing2.getValue().toLabel(), SEND_TO_JUDGE);

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .hearingDetails(newArrayList(hearing1))
                .draftUploadedCMOs(newArrayList(cmoLinkedToHearing, cmoWithoutHearing))
                .id(caseId2)
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            List<Element<HearingOrdersBundle>> migratedHearingBundles =
                extractedCaseData.getHearingOrdersBundlesDrafts();

            assertThat(extractedCaseData.getDraftUploadedCMOs())
                .containsExactlyInAnyOrder(cmoLinkedToHearing, cmoWithoutHearing);

            assertThat(unwrapElements(migratedHearingBundles))
                .containsExactlyInAnyOrder(
                    buildDraftOrdersBundle(
                        randomUUID(), newArrayList(cmoLinkedToHearing), hearing1, "").getValue());
        }

        @Test
        void shouldNotMigrateDraftCMOsWhenHearingOrdersDraftsBundlesDataExist() {
            Element<HearingBooking> hearing1 = element(buildHearing(now().plusDays(1), cmoId1));

            Element<HearingOrder> agreedCMO = buildCMO(cmoId1, hearing1.getValue().toLabel(), SEND_TO_JUDGE);
            Element<HearingOrder> agreedCMO1 = buildCMO(cmoId2, hearing1.getValue().toLabel(), SEND_TO_JUDGE);

            List<Element<HearingOrdersBundle>> hearingBundles =
                List.of(element(HearingOrdersBundle.builder()
                    .hearingId(hearing1.getId()).orders(newArrayList(agreedCMO1)).build()));

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .hearingDetails(newArrayList(hearing1))
                .hearingOrdersBundlesDrafts(hearingBundles)
                .draftUploadedCMOs(newArrayList(agreedCMO))
                .id(caseId3)
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).containsOnly(agreedCMO);
        }

        @Test
        void shouldNotMigrateToHearingBundlesWhenCaseIdIsNotExpected() {
            Element<HearingBooking> hearing1 = element(buildHearing(now().plusDays(1), cmoId1));

            Element<HearingOrder> agreedCMO = buildCMO(cmoId1, hearing1.getValue().toLabel(), SEND_TO_JUDGE);

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .hearingDetails(newArrayList(hearing1))
                .draftUploadedCMOs(newArrayList(agreedCMO))
                .id(12345678901234566L)
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).containsOnly(agreedCMO);
            assertThat(extractedCaseData.getHearingOrdersBundlesDrafts()).isNull();
        }

        @Test
        void shouldNotMigrateToHearingBundlesWhenMigrationIdIsNotExpected() {

            Element<HearingBooking> hearing1 = element(buildHearing(now().plusDays(1), cmoId1));

            Element<HearingOrder> agreedCMO1 = buildCMO(cmoId1, hearing1.getValue().toLabel(), SEND_TO_JUDGE);

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .hearingDetails(newArrayList(hearing1))
                .draftUploadedCMOs(newArrayList(agreedCMO1))
                .id(caseId1)
                .build());

            caseDetails.getData().put("migrationId", "FPLA-2000");

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            List<Element<HearingOrdersBundle>> migratedHearingBundles =
                extractedCaseData.getHearingOrdersBundlesDrafts();
            assertThat(migratedHearingBundles).isNull();
        }

        private HearingBooking buildHearing(LocalDateTime date) {
            return buildHearing(date, null);
        }

        private HearingBooking buildHearing(LocalDateTime date, UUID cmoId) {
            return HearingBooking.builder()
                .type(HearingType.CASE_MANAGEMENT)
                .startDate(date)
                .caseManagementOrderId(cmoId)
                .build();
        }

        private Element<HearingOrder> buildCMO(UUID id, String hearing, CMOStatus status) {
            return element(id, HearingOrder.builder()
                .hearing(hearing)
                .title(hearing)
                .type(SEND_TO_JUDGE.equals(status) ? AGREED_CMO : DRAFT_CMO)
                .order(order)
                .status(status)
                .judgeTitleAndName("Her Honour Judge Judy").build());
        }

        private Element<HearingOrdersBundle> buildDraftOrdersBundle(
            UUID bundleId, List<Element<HearingOrder>> draftOrders,
            Element<HearingBooking> hearingElement) {

            return buildDraftOrdersBundle(bundleId, draftOrders, hearingElement, "Her Honour Judge Judy");
        }

        private Element<HearingOrdersBundle> buildDraftOrdersBundle(
            UUID bundleId, List<Element<HearingOrder>> draftOrders,
            Element<HearingBooking> hearingElement, String judgeTitle) {

            return element(bundleId, HearingOrdersBundle.builder()
                .hearingName(hearingElement.getValue().toLabel())
                .orders(draftOrders)
                .hearingId(hearingElement.getId())
                .judgeTitleAndName(judgeTitle != null ? judgeTitle : "").build());
        }

    }

    @Nested
    class Fpla2684 {
        String migrationId = "FPLA-2684";
        String familyManNumber = "CF20C50070";
        UUID orderToBeRemovedId = randomUUID();
        UUID orderOneId = randomUUID();
        UUID orderTwoId = randomUUID();
        UUID childrenId = randomUUID();

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
    }

    @Nested
    class Fpla2687 {
        String migrationId = "FPLA-2687";
        String familyManNumber = "SN20C50009";
        UUID orderToBeRemovedId = randomUUID();
        UUID orderOneId = randomUUID();
        UUID orderTwoId = randomUUID();
        UUID childrenId = randomUUID();

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
