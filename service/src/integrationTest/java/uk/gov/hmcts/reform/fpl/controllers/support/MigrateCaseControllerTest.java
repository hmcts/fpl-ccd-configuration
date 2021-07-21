package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3126 {
        final String migrationId = "FPLA-3126";

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
                    "familyManCaseNumber", "NE21C50026",
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
                    "familyManCaseNumber", "NE21C50026",
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
                    "familyManCaseNumber", "NE21C50026",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3239 {
        final String migrationId = "FPLA-3239";

        @Test
        void shouldReplaceC110aWithCorrespondenceDoc() {

            DocumentReference redacted = testDocumentReference();
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "familyManCaseNumber", "DE21C50042",
                    "submittedForm", testDocumentReference(),
                    "correspondenceDocuments", List.of(element(
                        SupportingEvidenceBundle.builder()
                            .name("Redacted C110a")
                            .document(redacted).build())),
                    "migrationId", migrationId))
                .build();

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getSubmittedForm()).isEqualTo(redacted);
            assertThat(extractedCaseData.getCorrespondenceDocuments()).isEmpty();
        }

        @Test
        void shouldRemoveMigrationIdWhenNoC110a() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "familyManCaseNumber", "DE21C50042",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails).getData());
        }
    }
}
