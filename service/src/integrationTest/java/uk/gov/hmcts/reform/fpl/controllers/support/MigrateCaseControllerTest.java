package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla2885 {
        String migrationId = "FPLA-2885";
        UUID idOne = randomUUID();
        UUID idTwo = randomUUID();
        UUID idThree = randomUUID();
        UUID idFour = randomUUID();

        @Test
        void shouldMigrateExpectedListElementCodes() {
            Element<HearingBooking> hearingOne
                = element(idOne, hearingBookingWithCancellationReason("OT8"));

            Element<HearingBooking> hearingTwo
                = element(idTwo, hearingBookingWithCancellationReason("OT9"));

            Element<HearingBooking> hearingThree
                = element(idThree, hearingBookingWithCancellationReason("OT10"));

            Element<HearingBooking> hearingFour
                = element(idFour, hearingBookingWithCancellationReason("OT7"));

            List<Element<HearingBooking>> cancelledHearingBookings = List.of(
                hearingOne, hearingTwo, hearingThree, hearingFour);

            CaseDetails caseDetails = caseDetails(migrationId, cancelledHearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCancelledHearingDetails()).containsExactly(
                element(idOne, hearingBookingWithCancellationReason("IN1")),
                element(idTwo, hearingBookingWithCancellationReason("OT8")),
                element(idThree, hearingBookingWithCancellationReason("OT9")),
                element(idFour, hearingBookingWithCancellationReason("OT7"))
            );
        }

        @Test
        void shouldNotUpdateListElementCodesWhenMigrationIsNotRequired() {
            Element<HearingBooking> hearingOne
                = element(idOne, hearingBookingWithCancellationReason("OT1"));

            Element<HearingBooking> hearingTwo
                = element(idTwo, hearingBookingWithCancellationReason("OT3"));

            List<Element<HearingBooking>> cancelledHearingBookings = List.of(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(migrationId, cancelledHearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCancelledHearingDetails()).isEqualTo(cancelledHearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-2222";

            Element<HearingBooking> hearingOne
                = element(idOne, hearingBookingWithCancellationReason("OT8"));

            Element<HearingBooking> hearingTwo
                = element(idTwo, hearingBookingWithCancellationReason("OT9"));

            Element<HearingBooking> hearingThree
                = element(idThree, hearingBookingWithCancellationReason("OT10"));

            Element<HearingBooking> hearingFour
                = element(idFour, hearingBookingWithCancellationReason("OT7"));

            List<Element<HearingBooking>> cancelledHearingBookings = List.of(
                hearingOne, hearingTwo, hearingThree, hearingFour);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, cancelledHearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCancelledHearingDetails()).isEqualTo(cancelledHearingBookings);
        }

        @Test
        void shouldThrowAnErrorIfCaseDoesNotContainCancelledHearingBookings() {
            CaseDetails caseDetails = caseDetails(migrationId, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Case does not contain cancelled hearing bookings");
        }

        private HearingBooking hearingBookingWithCancellationReason(String reasonCode) {
            return HearingBooking.builder().cancellationReason(reasonCode).build();
        }

        private CaseDetails caseDetails(String migrationId, List<Element<HearingBooking>> cancelledHearings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .cancelledHearingDetails(cancelledHearings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

}
