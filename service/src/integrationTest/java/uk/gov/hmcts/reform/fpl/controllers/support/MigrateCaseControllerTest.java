package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
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
    class Fpla2724 {
        String familyManNumber = "WR20C50007";
        String migrationId = "FPLA-2724";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();
        HearingOrder cmo = HearingOrder.builder()
            .type(HearingOrderType.AGREED_CMO)
            .title("Agreed CMO discussed at hearing")
            .build();
        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(hearingOneId, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
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
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "LE30C500231";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null,
                hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private HearingBooking hearing(UUID cmoId) {
            return HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                .caseManagementOrderId(cmoId)
                .build();
        }
    }

    @Nested
    class Fpla2705 {
        String familyManNumber = "SN20C50023";
        String migrationId = "FPLA-2705";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();

        HearingOrder cmo = HearingOrder.builder()
            .type(HearingOrderType.AGREED_CMO)
            .status(CMOStatus.SEND_TO_JUDGE)
            .title("Agreed CMO discussed at hearing")
            .build();

        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);

            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved, additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(hearingOneId, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(orderToBeRemoved);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "SE30C500231";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(orderToBeRemoved);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null,
                hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private HearingBooking hearing(UUID cmoId) {
            return HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                .caseManagementOrderId(cmoId)
                .build();
        }
    }

    @Nested
    class Fpla2715 {
        String familyManNumber = "CF20C50079";
        String migrationId = "FPLA-2715";
        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();

        HearingOrder cmo = HearingOrder.builder()
            .type(HearingOrderType.AGREED_CMO)
            .status(CMOStatus.SEND_TO_JUDGE)
            .title("Agreed CMO discussed at hearing")
            .build();

        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);

            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved, additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(hearingOneId, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(orderToBeRemoved);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "AB30C500231";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(orderToBeRemoved);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null,
                hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private HearingBooking hearing(UUID cmoId) {
            return HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                .caseManagementOrderId(cmoId)
                .build();
        }
    }

    @Nested
    class Fpla2740 {
        String familyManNumber = "ZW21C50002";
        String migrationId = "FPLA-2740";

        @Test
        void shouldRemoveFirstCaseNote() {
            Element<CaseNote> caseNoteToRemove = element(CaseNote.builder()
                .date(LocalDate.now()).note("note1").createdBy("Moley").build());

            Element<CaseNote> caseNote2 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note2").createdBy("Test").build());

            Element<CaseNote> caseNote3 = element(CaseNote.builder()
                .date(LocalDate.now().minusDays(1)).note("note3").createdBy("Test").build());

            Element<CaseNote> caseNote4 = element(CaseNote.builder()
                .date(LocalDate.now().minusDays(2)).note("note4").createdBy("Test").build());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber,
                newArrayList(caseNoteToRemove, caseNote2, caseNote3, caseNote4));

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCaseNotes()).isEqualTo(List.of(caseNote2, caseNote3, caseNote4));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowExceptionWhenCaseDataDoesNotHaveCaseNotes(List<Element<CaseNote>> caseNotes) {
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, caseNotes);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least 4 case notes but found empty");
        }

        @Test
        void shouldThrowExceptionWhenCaseHaveLessThanExpectedCaseNotes() {
            Element<CaseNote> caseNote1 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note1").createdBy("Moley").build());

            Element<CaseNote> caseNote2 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note2").createdBy("Test").build());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber,
                newArrayList(caseNote1, caseNote2));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least 4 case notes but found 2");
        }

        @Test
        void shouldThrowExceptionWhenCaseHaveMoreThanExpectedCaseNotes() {
            Element<CaseNote> caseNote1 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note1").createdBy("Moley").build());

            Element<CaseNote> caseNote2 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note2").createdBy("Test").build());

            Element<CaseNote> caseNote3 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note3").createdBy("Test").build());

            Element<CaseNote> caseNote4 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note4").createdBy("Test").build());

            Element<CaseNote> caseNote5 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note5").createdBy("Test").build());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber,
                newArrayList(caseNote1, caseNote2, caseNote3, caseNote4, caseNote5));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected at least 4 case notes but found 5");
        }

        @Test
        void shouldNotRemoveCaseNotesForTheIncorrectMigrationId() {
            String incorrectMigrationId = "FPLA-2015";

            Element<CaseNote> caseNote1 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note1").createdBy("Moley").build());

            Element<CaseNote> caseNote2 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note2").createdBy("Test").build());

            Element<CaseNote> caseNote3 = element(CaseNote.builder()
                .date(LocalDate.now().minusDays(1)).note("note3").createdBy("Test").build());

            Element<CaseNote> caseNote4 = element(CaseNote.builder()
                .date(LocalDate.now().minusDays(2)).note("note4").createdBy("Test").build());

            ArrayList<Element<CaseNote>> caseNotesList
                = newArrayList(caseNote1, caseNote2, caseNote3, caseNote4);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, caseNotesList);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCaseNotes()).isEqualTo(caseNotesList);
        }

        @Test
        void shouldNotRemoveCaseNotesForTheIncorrectCaseReference() {
            String incorrectFamilyManNumber = "ABC2150003";

            Element<CaseNote> caseNote1 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note1").createdBy("Moley").build());

            Element<CaseNote> caseNote2 = element(CaseNote.builder()
                .date(LocalDate.now()).note("note2").createdBy("Test").build());

            Element<CaseNote> caseNote3 = element(CaseNote.builder()
                .date(LocalDate.now().minusDays(1)).note("note3").createdBy("Test").build());

            Element<CaseNote> caseNote4 = element(CaseNote.builder()
                .date(LocalDate.now().minusDays(2)).note("note4").createdBy("Test").build());

            ArrayList<Element<CaseNote>> caseNotesList
                = newArrayList(caseNote1, caseNote2, caseNote3, caseNote4);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, caseNotesList);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCaseNotes()).isEqualTo(caseNotesList);
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<CaseNote>> caseNotes) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .caseNotes(caseNotes)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2706 {
        String familyManNumber = "CF20C50049";
        String migrationId = "FPLA-2706";

        UUID orderToBeRemovedId = UUID.randomUUID();
        UUID orderTwoId = UUID.randomUUID();

        HearingOrder cmo = HearingOrder.builder()
            .type(HearingOrderType.DRAFT_CMO)
            .status(CMOStatus.DRAFT)
            .title("Draft CMO")
            .build();

        UUID hearingOneId = UUID.randomUUID();
        UUID hearingTwoId = UUID.randomUUID();

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);

            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));
            Element<HearingBooking> additionalHearing = element(hearingTwoId, hearing(orderTwoId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved, additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(hearingOneId, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(orderToBeRemoved);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "SE30C500231";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingBooking> hearingToBeRemoved = element(hearingOneId, hearing(orderToBeRemovedId));

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(orderToBeRemoved);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null,
                hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }


        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private HearingBooking hearing(UUID cmoId) {
            return HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                .caseManagementOrderId(cmoId)
                .build();
        }
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
                .hasMessage("Issues Resolution/Early Final Hearing hearing, 5 March 2021");
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
                .hearingName("Issues Resolution/Early Final Hearing hearing, 5 March 2021")
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
                .hearingName("Issues Resolution/Early Final Hearing hearing, 5 March 2021")
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
                .hearingName("Issues Resolution/Early Final Hearing hearing, 5 March 2021")
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

    @Nested
    class Fpla2885 {
        String migrationId = "FPLA-2885";
        UUID idOne = UUID.randomUUID();
        UUID idTwo = UUID.randomUUID();
        UUID idThree = UUID.randomUUID();
        UUID idFour = UUID.randomUUID();

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
