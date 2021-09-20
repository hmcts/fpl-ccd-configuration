package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.UnexpectedNumberOfCMOsRemovedException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class UpdateCMOHearingTest {

    private static final UUID HEARING_BUNDLE_ID = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID ANOTHER_HEARING_ID = randomUUID();
    private static final UUID CMO_ID = randomUUID();
    private static final UUID ANOTHER_CMO_ID = randomUUID();
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.now();

    private final UpdateCMOHearing underTest = new UpdateCMOHearing();

    @Test
    void shouldReturnTheHearingForTheCMOId() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        HearingBooking hearingWithCMOId = hearing(CMO_ID, differentStartDate);

        Element<HearingOrder> linkedCMO = element(CMO_ID, HearingOrder.builder()
            .type(DRAFT_CMO)
            .status(DRAFT)
            .hearing(hearingWithCMOId.toLabel())
            .build());

        HearingOrdersBundle hearingOrdersBundle = HearingOrdersBundle.builder()
            .hearingId(HEARING_ID)
            .orders(newArrayList(linkedCMO,
                element(ANOTHER_CMO_ID, HearingOrder.builder().build())))
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HEARING_ID, hearingWithCMOId),
                element(ANOTHER_HEARING_ID, hearing(randomUUID()))))
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_BUNDLE_ID, hearingOrdersBundle)))
            .build();

        assertThat(underTest.getHearingToUnlink(caseData, CMO_ID, linkedCMO.getValue()))
            .isEqualTo(hearingWithCMOId);
    }

    @Test
    void shouldThrowExceptionWhenNoHearingFoundForTheCMOId() {
        HearingBooking hearingWithCMOId = hearing(CMO_ID, HEARING_START_DATE);

        Element<HearingOrder> sealedCMO = element(CMO_ID, HearingOrder.builder()
            .type(AGREED_CMO).status(APPROVED)
            .hearing(hearingWithCMOId.toLabel()).build());

        HearingOrder anotherCMO = HearingOrder.builder().type(AGREED_CMO).status(APPROVED).build();

        CaseData caseData = CaseData.builder()
            .sealedCMOs(newArrayList(sealedCMO))
            .hearingDetails(List.of(element(HEARING_ID, hearing(CMO_ID))))
            .build();

        Exception exception = assertThrows(UnexpectedNumberOfCMOsRemovedException.class,
            () -> underTest.getHearingToUnlink(caseData, ANOTHER_CMO_ID, anotherCMO));

        AssertionsForClassTypes.assertThat(exception).hasMessageContaining(String.format(
            "CMO %s could not be linked to hearing by CMO id and there wasn't a unique link "
                + "(%s links found) to a hearing with the same label", ANOTHER_CMO_ID, 0));
    }

    @Test
    void shouldRemoveHearingLinkedToCMO() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        HearingBooking hearingWithCMOId = hearing(CMO_ID, differentStartDate);
        HearingBooking anotherHearing = hearing(ANOTHER_CMO_ID, HEARING_START_DATE.plusDays(4));

        Element<HearingOrder> linkedCMO = element(CMO_ID, HearingOrder.builder()
            .type(DRAFT_CMO)
            .status(DRAFT)
            .hearing(hearingWithCMOId.toLabel())
            .build());

        HearingOrdersBundle hearingOrdersBundle = HearingOrdersBundle.builder()
            .hearingId(HEARING_ID)
            .orders(newArrayList(linkedCMO,
                element(ANOTHER_CMO_ID, HearingOrder.builder().build())))
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HEARING_ID, hearingWithCMOId),
                element(ANOTHER_HEARING_ID, anotherHearing)))
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_BUNDLE_ID, hearingOrdersBundle)))
            .build();

        assertThat(underTest.removeHearingLinkedToCMO(caseData, linkedCMO))
            .contains(element(ANOTHER_HEARING_ID, anotherHearing));
    }

    private HearingBooking hearing(UUID cmoId) {
        return hearing(cmoId, HEARING_START_DATE);
    }

    private HearingBooking hearing(UUID cmoId, LocalDateTime startDate) {
        return HearingBooking.builder()
            .caseManagementOrderId(cmoId)
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .build();
    }
}
