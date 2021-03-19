package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla2774 {
        String familyManNumber = "NE21C50007";
        String migrationId = "FPLA-2774";
        UUID hearingIdOne = UUID.randomUUID();
        UUID hearingIdTwo = UUID.randomUUID();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        @Test
        void shouldRemoveSecondHearing() {
            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            Element<HearingBooking> hearingTwo = element(hearingIdTwo, hearingBooking);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(hearingOne));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            Element<HearingBooking> hearingTwo = element(hearingIdTwo, hearingBooking);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);
            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            Element<HearingBooking> hearingTwo = element(hearingIdTwo, hearingBooking);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);
            CaseDetails caseDetails = caseDetails(migrationId, invalidFamilyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsFewerHearingsThanExpected() {
            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected 2 hearings in the case but found 1");
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainHearings() {
            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No hearings in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2889 {
        String familyManNumber = "PO20C50010";
        String migrationId = "FPLA-2898";

        @Test
        void shouldThrowExceptionWhenUnexpectedFamilyManNumber() {
            CaseDetails caseData = caseDetails(migrationId, "test", emptyList());

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Unexpected FMN test");
        }

        @Test
        void shouldThrowExceptionWhenHearingFurtherEvidenceBundleIsMissing() {

            HearingFurtherEvidenceBundle bundle = HearingFurtherEvidenceBundle.builder()
                .hearingName("test")
                .build();

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, wrapElements(bundle));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Issues Resolution/Early Final hearing, 5 March 2021");
        }


        @Test
        void shouldThrowExceptionWhenNotAllDocumentsFound() {

            SupportingEvidenceBundle supportingDoc1 = SupportingEvidenceBundle.builder()
                .name("Placement application")
                .document(testDocumentReference())
                .build();

            SupportingEvidenceBundle supportingDoc2 = SupportingEvidenceBundle.builder()
                .name("Statement of facts")
                .document(testDocumentReference())
                .build();

            HearingFurtherEvidenceBundle evidenceBundle = HearingFurtherEvidenceBundle.builder()
                .hearingName("Issues Resolution/Early Final hearing, 5 March 2021")
                .supportingEvidenceBundle(wrapElements(supportingDoc1, supportingDoc2))
                .build();

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, wrapElements(evidenceBundle));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Unexpected number of found documents: 2");
        }

        @Test
        void shouldRemoveEvidenceBundleWhenAllDocumentsRemoved() {

            SupportingEvidenceBundle supportingDoc1 = SupportingEvidenceBundle.builder()
                .name("Placement application")
                .document(testDocumentReference())
                .build();

            SupportingEvidenceBundle supportingDoc2 = SupportingEvidenceBundle.builder()
                .name("Statement of facts")
                .document(testDocumentReference())
                .build();

            SupportingEvidenceBundle supportingDoc3 = SupportingEvidenceBundle.builder()
                .name("CPR")
                .document(testDocumentReference())
                .build();

            SupportingEvidenceBundle supportingDocNotRelated = SupportingEvidenceBundle.builder()
                .name("CPR")
                .document(testDocumentReference())
                .build();


            Element<HearingFurtherEvidenceBundle> bundle = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Issues Resolution/Early Final hearing, 5 March 2021")
                .supportingEvidenceBundle(wrapElements(supportingDoc1, supportingDoc2, supportingDoc3))
                .build());

            Element<HearingFurtherEvidenceBundle> bundle2 = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Not related")
                .supportingEvidenceBundle(wrapElements(supportingDocNotRelated))
                .build());

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, List.of(bundle, bundle2));

            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

            assertThat(updatedCaseData.getHearingFurtherEvidenceDocuments()).containsExactly(bundle2);
        }

        @Test
        void shouldRemoveDocumentFormBundle() {

            Element<SupportingEvidenceBundle> supportingDoc1 = element(SupportingEvidenceBundle.builder()
                .name("Placement application")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc2 = element(SupportingEvidenceBundle.builder()
                .name("Statement of facts")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc3 = element(SupportingEvidenceBundle.builder()
                .name("CPR")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc4 = element(SupportingEvidenceBundle.builder()
                .name("Unaffected")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc5 = element(SupportingEvidenceBundle.builder()
                .name("Not related")
                .document(testDocumentReference())
                .build());


            Element<HearingFurtherEvidenceBundle> bundle1 = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Issues Resolution/Early Final hearing, 5 March 2021")
                .supportingEvidenceBundle(List.of(supportingDoc1, supportingDoc2, supportingDoc3, supportingDoc4))
                .build());

            Element<HearingFurtherEvidenceBundle> bundle2 = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Other")
                .supportingEvidenceBundle(List.of(supportingDoc5))
                .build());

            Element<HearingFurtherEvidenceBundle> expectedBundle = element(bundle1.getId(),
                bundle1.getValue().toBuilder()
                    .supportingEvidenceBundle(List.of(supportingDoc4))
                    .build());

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, List.of(bundle1, bundle2));

            CaseData updated = extractCaseData(postAboutToSubmitEvent(caseData));

            assertThat(updated.getHearingFurtherEvidenceDocuments()).containsExactly(expectedBundle, bundle2);
        }


        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<HearingFurtherEvidenceBundle>> bundles) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .hearingFurtherEvidenceDocuments(bundles)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }
}
