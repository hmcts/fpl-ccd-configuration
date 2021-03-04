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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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
}
