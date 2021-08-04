package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.State.DELETED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3262 {
        final String migrationId = "FPLA-3262";

        @Test
        void shouldRemoveDraftCMOIfPresent() {
            UUID id = UUID.randomUUID();
            List<Element<HearingBooking>> cancelledHearings =
                List.of(element(HearingBooking.builder().caseManagementOrderId(id).build()));
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "familyManCaseNumber", "PE21C50004",
                    "draftUploadedCMOs", List.of(element(id, HearingOrder.builder().title("remove me").build())),
                    "cancelledHearingDetails", cancelledHearings,
                    "migrationId", migrationId))
                .build();

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            cancelledHearings.get(0).getValue().setCaseManagementOrderId(null);

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEmpty();
            assertThat(extractedCaseData.getCancelledHearingDetails()).isEqualTo(cancelledHearings);
        }

        @Test
        void shouldThrowExceptionCMOIdNotFoundInCancelledHearing() {
            UUID id = UUID.randomUUID();
            List<Element<HearingBooking>> cancelledHearings =
                List.of(element(HearingBooking.builder().caseManagementOrderId(UUID.randomUUID()).build()));
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "familyManCaseNumber", "PE21C50004",
                    "draftUploadedCMOs", List.of(element(id, HearingOrder.builder().title("remove me").build())),
                    "cancelledHearingDetails", cancelledHearings,
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails));
        }

        @Test
        void shouldRemoveMigrationIdWhenNoDraftCMOs() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "familyManCaseNumber", "PE21C50004",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails));
        }
    }

    @Nested
    class Fpla3294 {
        final String migrationId = "FPLA-3294";

        @Test
        void shouldRemoveAllDataAndMoveStateToDeleted() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Closed")
                .data(Map.of(
                    "familyManCaseNumber", "SA21C50091",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getState()).isEqualTo(DELETED.getValue());
        }
    }
}
