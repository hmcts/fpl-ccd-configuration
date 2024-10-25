package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class SendOrderReminderServiceTest {

    private final SendOrderReminderService underTest = new SendOrderReminderService();

    @Test
    void shouldReturnEmptyListIfNoHearingsMissingCMOs() {
        LocalDateTime startDate = now().minusDays(5);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(startDate)
                    .endDate(startDate.plusHours(1))
                    .caseManagementOrderId(UUID.randomUUID())
                    .build())
            ))
            .build();

        List<HearingBooking> actual = underTest.getPastHearingBookingsWithoutCMOs(caseData);

        assertThat(actual).isEqualTo(emptyList());
    }

    @Test
    void shouldReturnListOfHearingsMissingCMOs() {
        LocalDateTime startDate = now().minusDays(5);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(startDate)
                    .endDate(startDate.plusHours(1))
                    .build())
            ))
            .build();

        List<HearingBooking> actual = underTest.getPastHearingBookingsWithoutCMOs(caseData);

        assertThat(actual).isEqualTo(caseData.getHearingDetails().stream()
            .map(Element::getValue)
            .collect(Collectors.toList()));
    }

    @Test
    void shouldReturnEmptyListIfNoHearingsMissingCMOsButNoCMOIdPresent() {
        LocalDateTime startDate = LocalDateTime.of(2020, Month.JUNE, 21, 0, 0);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(startDate)
                    .endDate(startDate.plusHours(1))
                    .build())
            ))
            .sealedCMOs(List.of(
                element(HearingOrder.builder()
                    .hearing("Case management hearing, 21 June 2020")
                    .build())
            ))
            .build();

        List<HearingBooking> actual = underTest.getPastHearingBookingsWithoutCMOs(caseData);

        assertThat(actual).isEqualTo(emptyList());
    }

}
