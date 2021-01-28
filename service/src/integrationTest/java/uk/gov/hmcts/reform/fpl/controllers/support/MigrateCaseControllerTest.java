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
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractControllerTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private final UUID orderToBeRemovedId = UUID.randomUUID();
    private final UUID orderTwoId = UUID.randomUUID();
    private final CaseManagementOrder cmo = CaseManagementOrder.builder().build();

    @Nested
    class Fpla2640 {
        String familyManNumber = "NE20C50006";
        String migrationId = "FPLA-2640";

        @Test
        void shouldRemoveFirstDraftCaseManagementOrder() {
            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, draftCaseManagementOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "LE30C500231";

            Element<CaseManagementOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<CaseManagementOrder> additionalOrder = element(orderTwoId, cmo);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, draftCaseManagementOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }
    }

    private CaseDetails caseDetails(String migrationId,
                                    String familyManNumber,
                                    List<Element<CaseManagementOrder>> draftCaseManagementOrders) {
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .familyManCaseNumber(familyManNumber)
            .draftUploadedCMOs(draftCaseManagementOrders)
            .build());

        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
