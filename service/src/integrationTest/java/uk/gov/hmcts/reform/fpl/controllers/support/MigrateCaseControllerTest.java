package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
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

    private static final UUID CMO_ID_1 = UUID.randomUUID();
    private static final UUID CMO_ID_2 = UUID.randomUUID();
    private static final UUID CMO_ID_3 = UUID.randomUUID();
    private static final UUID HEARING_ID_1 = UUID.randomUUID();
    private static final UUID HEARING_ID_2 = UUID.randomUUID();
    private static final UUID HEARING_ID_3 = UUID.randomUUID();
    private static final CaseManagementOrder CMO = CaseManagementOrder.builder().build();


    @Nested
    class Fpla2589 {
        String errorMessage = "Expected 2 draft case management orders but found %s";
        String familyManNumber = "PO20C50026";
        String migrationId = "FPLA-2589";

        @Test
        void shouldRemoveFirstTwoDraftCaseManagementOrdersAndUnlinkHearings() {
            Element<CaseManagementOrder> draftCmoOne = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> draftCmoTwo = element(CMO_ID_2, CMO);
            Element<CaseManagementOrder> draftCmoThree = element(CMO_ID_3, CMO);

            Element<HearingBooking> hearingOne = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, hearing(CMO_ID_2));
            Element<HearingBooking> hearingThree = element(HEARING_ID_3, hearing(CMO_ID_3));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                draftCmoOne,
                draftCmoTwo,
                draftCmoThree);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo, hearingThree);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(draftCmoThree));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(HEARING_ID_1, hearing(null)),
                element(HEARING_ID_2, hearing(null)),
                hearingThree));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> draftCmoOne = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> draftCmoTwo = element(CMO_ID_2, CMO);
            Element<CaseManagementOrder> draftCmoThree = element(CMO_ID_3, CMO);

            Element<HearingBooking> hearingOne = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, hearing(CMO_ID_2));
            Element<HearingBooking> hearingThree = element(HEARING_ID_3, hearing(CMO_ID_3));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                draftCmoOne,
                draftCmoTwo,
                draftCmoThree);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo, hearingThree);

            CaseDetails caseDetails = caseDetails(
                incorrectMigrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> draftCmoOne = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> draftCmoTwo = element(CMO_ID_2, CMO);
            Element<CaseManagementOrder> draftCmoThree = element(CMO_ID_3, CMO);

            Element<HearingBooking> hearingOne = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, hearing(CMO_ID_2));
            Element<HearingBooking> hearingThree = element(HEARING_ID_3, hearing(CMO_ID_3));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                draftCmoOne,
                draftCmoTwo,
                draftCmoThree);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo, hearingThree);

            CaseDetails caseDetails = caseDetails(
                migrationId, invalidFamilyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsOnlyOneDraftCaseManagementOrder() {
            Element<CaseManagementOrder> draftCmoOne = element(CMO_ID_1, CMO);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(draftCmoOne);

            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 1));
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 0));
        }
    }

    @Nested
    class Fpla2593 {
        String errorMessage = "Expected 2 draft case management orders but found %s";
        String familyManNumber = "CF20C50030";
        String migrationId = "FPLA-2593";

        @Test
        void shouldRemoveSecondDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<CaseManagementOrder> firstOrder = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> secondOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingOne = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                firstOrder,
                secondOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(firstOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                hearingOne,
                element(HEARING_ID_2, hearing(null))));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> firstOrder = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> secondOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingOne = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                firstOrder,
                secondOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(
                incorrectMigrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> firstOrder = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> secondOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingOne = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> hearingTwo = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                firstOrder,
                secondOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(
                migrationId, invalidFamilyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsOnlyOneDraftCaseManagementOrder() {
            Element<CaseManagementOrder> draftCmoOne = element(CMO_ID_1, CMO);

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(draftCmoOne);

            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 1));
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(errorMessage, 0));
        }
    }

    @Nested
    class Fpla2599 {
        String familyManNumber = "SA20C50016";
        String migrationId = "FPLA-2599";

        @Test
        void shouldRemoveFirstDraftCaseManagementOrderAndUnlinkItsHearing() {
            Element<CaseManagementOrder> orderToBeRemoved = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> additionalOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingToBeRemoved = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> additionalHearing = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, draftCaseManagementOrders, hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(List.of(additionalOrder));
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(
                element(HEARING_ID_1, hearing(null)),
                additionalHearing));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<CaseManagementOrder> orderToBeRemoved = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> additionalOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingToBeRemoved = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> additionalHearing = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
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
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<CaseManagementOrder> orderToBeRemoved = element(CMO_ID_1, CMO);
            Element<CaseManagementOrder> additionalOrder = element(CMO_ID_2, CMO);
            Element<HearingBooking> hearingToBeRemoved = element(HEARING_ID_1, hearing(CMO_ID_1));
            Element<HearingBooking> additionalHearing = element(HEARING_ID_2, hearing(CMO_ID_2));

            List<Element<CaseManagementOrder>> draftCaseManagementOrders = newArrayList(
                orderToBeRemoved,
                additionalOrder);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingToBeRemoved, additionalHearing);

            CaseDetails caseDetails = caseDetails(migrationId, invalidFamilyManNumber, draftCaseManagementOrders,
                hearingBookings);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEqualTo(draftCaseManagementOrders);
            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainDraftCaseManagementOrders() {
            List<Element<HearingBooking>> hearingBookings = newArrayList(newArrayList());

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, null, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No draft case management orders in the case");
        }
    }

    private CaseDetails caseDetails(String migrationId,
                                    String familyManCaseNumber,
                                    List<Element<CaseManagementOrder>> draftCaseManagementOrders,
                                    List<Element<HearingBooking>> hearingBookings) {
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .familyManCaseNumber(familyManCaseNumber)
            .draftUploadedCMOs(draftCaseManagementOrders)
            .hearingDetails(hearingBookings)
            .build());

        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    private HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .caseManagementOrderId(cmoId)
            .build();
    }
}
