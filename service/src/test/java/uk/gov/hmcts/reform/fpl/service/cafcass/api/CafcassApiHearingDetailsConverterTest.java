package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiHearing;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class CafcassApiHearingDetailsConverterTest extends CafcassApiConverterTestBase {
    CafcassApiHearingDetailsConverterTest() {
        super(new CafcassApiHearingDetailsConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.hearingDetails", "data.cancelledHearingDetails"));
    }

    @Test
    void shouldConvertHearingDetails() {
        HearingBooking vacatedHearing = HearingBooking.builder()
            .type(HearingType.OTHER)
            .typeDetails("typeDetails")
            .venue("venue")
            .status(HearingStatus.VACATED)
            .startDate(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
            .endDate(LocalDateTime.of(2024, 1, 1, 15, 0, 0))
            .attendance(Arrays.asList(HearingAttendance.values()))
            .cancellationReason("cancellationReason")
            .preAttendanceDetails("preAttendanceDetails")
            .attendanceDetails("attendanceDetails")
            .build();
        Element<HearingBooking> vacatedHearingElement = element(vacatedHearing);

        HearingBooking caseManagementHearing = HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .venue("venue")
            .startDate(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
            .endDate(LocalDateTime.of(2024, 1, 1, 15, 0, 0))
            .attendance(Arrays.asList(HearingAttendance.values()))
            .build();
        Element<HearingBooking> caseManagementHearingElement = element(caseManagementHearing);

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(caseManagementHearingElement))
            .cancelledHearingDetails(List.of(vacatedHearingElement))
            .build();

        CafcassApiHearing expectedVacatedHearing = CafcassApiHearing.builder()
            .id(vacatedHearingElement.getId().toString())
            .type(HearingType.OTHER)
            .typeDetails("typeDetails")
            .venue("venue")
            .status(HearingStatus.VACATED)
            .startDate(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
            .endDate(LocalDateTime.of(2024, 1, 1, 15, 0, 0))
            .attendance(Arrays.asList(HearingAttendance.values()))
            .cancellationReason("cancellationReason")
            .preAttendanceDetails("preAttendanceDetails")
            .attendanceDetails("attendanceDetails")
            .build();

        CafcassApiHearing expectedCaseManagementHearing = CafcassApiHearing.builder()
            .id(caseManagementHearingElement.getId().toString())
            .type(HearingType.CASE_MANAGEMENT)
            .venue("venue")
            .startDate(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
            .endDate(LocalDateTime.of(2024, 1, 1, 15, 0, 0))
            .preAttendanceDetails(DEFAULT_PRE_ATTENDANCE)
            .attendance(Arrays.asList(HearingAttendance.values()))
            .build();

        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .hearingDetails(List.of(expectedCaseManagementHearing, expectedVacatedHearing))
            .build();

        testConvert(caseData, expected);
    }

    @Test
    void shouldReturnEmptyListIfNullOrEmpty() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().hearingDetails(List.of()).build();

        testConvert(CaseData.builder().hearingDetails(null).cancelledHearingDetails(null).build(), expected);
        testConvert(CaseData.builder().hearingDetails(List.of()).cancelledHearingDetails(List.of()).build(), expected);
    }
}
