package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final UUID GENERATED_ORDER_ID_1 = UUID.randomUUID();
    private static final UUID GENERATED_ORDER_ID_2 = UUID.randomUUID();
    private static final UUID GENERATED_ORDER_ID_3 = UUID.randomUUID();
    private static final UUID GENERATED_ORDER_ID_4 = UUID.randomUUID();
    private static final GeneratedOrder ORDER = GeneratedOrder.builder().build();

    private static final Element<GeneratedOrder> orderOne = element(GENERATED_ORDER_ID_1, ORDER);
    private static final Element<GeneratedOrder> orderTwo = element(GENERATED_ORDER_ID_2, ORDER);
    private static final Element<GeneratedOrder> orderThree = element(GENERATED_ORDER_ID_3, ORDER);
    private static final Element<GeneratedOrder> orderFour = element(GENERATED_ORDER_ID_4, ORDER);

    @Nested
    class Fpla2480 {
        String familyManNumber = "CF20C50072";
        String migrationId = "FPLA-2623";

        @Test
        void shouldRemoveFourthOrder() {
            List<Element<GeneratedOrder>> orders = List.of(orderOne, orderTwo, orderThree, orderFour);
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, orders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            List<Element<GeneratedOrder>> modifiedOrders = List.of(orderOne, orderTwo, orderThree);

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(modifiedOrders);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            List<Element<GeneratedOrder>> orders = List.of(orderOne, orderTwo, orderThree, orderFour);
            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, orders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orders);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            List<Element<GeneratedOrder>> orders = List.of(orderOne, orderTwo, orderThree, orderFour);
            CaseDetails caseDetails = caseDetails(migrationId, invalidFamilyManNumber, orders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orders);
        }

        @Test
        void shouldNotModifyOrdersCollectionIfOrderToRemoveIndexIsBiggerThanCollectionSize() {
            List<Element<GeneratedOrder>> orders = List.of(orderOne, orderTwo, orderThree);
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, orders);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOrderCollection()).isEqualTo(orders);

        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<GeneratedOrder>> orders) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .orderCollection(orders)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }
}
