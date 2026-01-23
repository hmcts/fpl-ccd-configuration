package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.controllers.support.MigrateCaseController.MIGRATION_ID_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
    }

    @Nested
    class Dfpl2773 {
        private static final String MIGRATION_ID = "DFPL-2773";
        private static final String ROLLBACK_ID = "DFPL-2773-rollback";
        private static final DocumentReference ORDER_DOCUMENT = testDocumentReference();
        private static final UUID HEARING_ORDER_ID = UUID.randomUUID();

        @Test
        void shouldMigrateRefusedOrder() {
            CaseData after = extractCaseData(postAboutToSubmitEvent(CaseDetails.builder()
                .data(Map.of("refusedHearingOrders",
                    List.of(element(HEARING_ORDER_ID, HearingOrder.builder().order(ORDER_DOCUMENT)
                        .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())),
                    MIGRATION_ID_KEY, MIGRATION_ID))
                .build()));

            assertThat(after.getRefusedHearingOrders())
                .isEqualTo(List.of(element(HEARING_ORDER_ID,
                    HearingOrder.builder().refusedOrder(ORDER_DOCUMENT)
                        .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "CTSC", "LA", "Resp0", "Child0", "Resp1", "Child1", "Resp2", "Child2", "Resp3", "Child3", "Resp4", "Child4",
            "Resp5", "Child5", "Resp6", "Child6", "Resp7", "Child7", "Resp8", "Child8", "Resp9", "Child9",
            "Child10", "Child11", "Child12", "Child13", "Child14"
        })
        void shouldMigrateConfidentialRefusedOrder(String suffix) {
            CaseData after = extractCaseData(postAboutToSubmitEvent(CaseDetails.builder()
                .data(Map.of("refusedHearingOrders" + suffix,
                    List.of(element(HEARING_ORDER_ID, HearingOrder.builder().order(ORDER_DOCUMENT)
                        .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())),
                    MIGRATION_ID_KEY, MIGRATION_ID))
                .build()));

            after.getConfidentialRefusedOrders().processAllConfidentialOrders((suffixAfter, orders) -> {
                if (suffixAfter.equals(suffix)) {
                    assertThat(orders).isEqualTo(List.of(element(HEARING_ORDER_ID,
                        HearingOrder.builder().refusedOrder(ORDER_DOCUMENT)
                            .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())));
                } else {
                    assertThat(orders).isNull();
                }
            });
        }

        @Test
        void shouldNotMigrateIfEmptyOrNull() {
            CaseData after = extractCaseData(postAboutToSubmitEvent(CaseDetails.builder()
                .data(Map.of(MIGRATION_ID_KEY, MIGRATION_ID))
                .build()));

            assertThat(after.getRefusedHearingOrders()).isNull();

            after.getConfidentialRefusedOrders().processAllConfidentialOrders((suffixAfter, orders) -> {
                assertThat(orders).isNull();
            });
        }

        @Test
        void shouldRollbackRefusedOrder() {
            CaseData after = extractCaseData(postAboutToSubmitEvent(CaseDetails.builder()
                .data(Map.of("refusedHearingOrders",
                    List.of(element(HEARING_ORDER_ID, HearingOrder.builder().refusedOrder(ORDER_DOCUMENT)
                        .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())),
                    MIGRATION_ID_KEY, ROLLBACK_ID))
                .build()));

            assertThat(after.getRefusedHearingOrders())
                .isEqualTo(List.of(element(HEARING_ORDER_ID,
                    HearingOrder.builder().order(ORDER_DOCUMENT)
                        .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "CTSC", "LA", "Resp0", "Child0", "Resp1", "Child1", "Resp2", "Child2", "Resp3", "Child3", "Resp4", "Child4",
            "Resp5", "Child5", "Resp6", "Child6", "Resp7", "Child7", "Resp8", "Child8", "Resp9", "Child9",
            "Child10", "Child11", "Child12", "Child13", "Child14"
        })
        void shouldRollbackConfidentialRefusedOrder(String suffix) {
            CaseData after = extractCaseData(postAboutToSubmitEvent(CaseDetails.builder()
                .data(Map.of("refusedHearingOrders" + suffix,
                    List.of(element(HEARING_ORDER_ID, HearingOrder.builder().refusedOrder(ORDER_DOCUMENT)
                        .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())),
                    MIGRATION_ID_KEY, ROLLBACK_ID))
                .build()));

            after.getConfidentialRefusedOrders().processAllConfidentialOrders((suffixAfter, orders) -> {
                if (suffixAfter.equals(suffix)) {
                    assertThat(orders).isEqualTo(List.of(element(HEARING_ORDER_ID,
                        HearingOrder.builder().order(ORDER_DOCUMENT)
                            .documentAcknowledge(List.of("ACK_RELATED_TO_CASE")).build())));
                } else {
                    assertThat(orders).isNull();
                }
            });
        }

        @Test
        void shouldNotRollbackIfEmptyOrNull() {
            CaseData after = extractCaseData(postAboutToSubmitEvent(CaseDetails.builder()
                .data(Map.of(MIGRATION_ID_KEY, ROLLBACK_ID))
                .build()));

            assertThat(after.getRefusedHearingOrders()).isNull();

            after.getConfidentialRefusedOrders().processAllConfidentialOrders((suffixAfter, orders) -> {
                assertThat(orders).isNull();
            });
        }
    }
}
