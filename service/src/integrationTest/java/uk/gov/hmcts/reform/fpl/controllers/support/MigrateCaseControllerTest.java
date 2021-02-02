package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
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
    private final HearingOrder cmo = HearingOrder.builder()
        .type(HearingOrderType.AGREED_CMO)
        .title("Agreed CMO discussed at hearing")
        .build();
    private static final UUID HEARING_ID_1 = UUID.randomUUID();
    private static final UUID HEARING_ID_2 = UUID.randomUUID();
    private static final UUID HEARING_ID_3 = UUID.randomUUID();
    private static final UUID HEARING_ID_4 = UUID.randomUUID();
    private static final HearingBooking HEARING = HearingBooking.builder().build();

    @Nested
    class Fpla2640 {
        String familyManNumber = "NE20C50006";
        String migrationId = "FPLA-2640";

        @Test
        void shouldRemoveFirstDraftCaseManagementOrder() {
            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, draftCaseManagementOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }


        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2651 {

        String familyManNumber = "NE21C50001";
        String migrationId = "FPLA-2651";

        @Test
        void shouldRemoveFirstHearing() {
            Element<HearingBooking> hearingOne = element(HEARING_ID_1, HEARING);
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, HEARING);
            Element<HearingBooking> hearingThree = element(HEARING_ID_3, HEARING);
            Element<HearingBooking> hearingFour = element(HEARING_ID_4, HEARING);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo,
                hearingThree, hearingFour);
            CaseDetails caseDetails = caseDetailsWithHearings(migrationId, familyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(hearingTwo, hearingThree, hearingFour));
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
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private CaseDetails caseDetailsWithHearings(String migrationId,
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
    class IsCorrectCaseAndMigration {

        String familyManNumber = "NE21C50001";
        String migrationId = "FPLA-2651";

        @Test
        void shouldNotChangeCaseIfNotExpectedCaseNumber() {
            String incorrectFamilyManNumber = "LE30C500231";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(migrationId, incorrectFamilyManNumber, draftCaseManagementOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingOrder> orderToBeRemoved = element(orderToBeRemovedId, cmo);
            Element<HearingOrder> additionalOrder = element(orderTwoId, cmo);

            List<Element<HearingOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, draftCaseManagementOrders);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
        }


        private CaseDetails caseDetails(String migrationId,
                                        String familyManNumber,
                                        List<Element<HearingOrder>> draftCaseManagementOrders) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .draftUploadedCMOs(draftCaseManagementOrders)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2654 {
        String familyManNumber = "NE20C50011";
        String migrationId = "FPLA-2654";
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
}
