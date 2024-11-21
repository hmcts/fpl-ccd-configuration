package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiHearing;

import java.util.List;
import java.util.Optional;

@Service
public class CafcassApiHearingDetailsConverter implements CafcassApiCaseDataConverter {
    private static final List<String> SOURCE = List.of("data.hearingDetails");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.hearingDetails(getCafcassApiHearing(caseData));
    }

    private List<CafcassApiHearing> getCafcassApiHearing(CaseData caseData) {
        return caseData.getAllHearings().stream()
            .map(hearingBookingElement -> {
                HearingBooking hearingBooking = hearingBookingElement.getValue();
                return CafcassApiHearing.builder()
                    .id(hearingBookingElement.getId().toString())
                    .type(hearingBooking.getType())
                    .typeDetails(hearingBooking.getTypeDetails())
                    .venue(hearingBooking.getVenue())
                    .status(hearingBooking.getStatus())
                    .startDate(hearingBooking.getStartDate())
                    .endDate(hearingBooking.getEndDate())
                    .attendance(hearingBooking.getAttendance())
                    .cancellationReason(hearingBooking.getCancellationReason())
                    .preAttendanceDetails(hearingBooking.getPreAttendanceDetails())
                    .attendanceDetails(hearingBooking.getAttendanceDetails())
                    .build();
            })
            .toList();
    }
}
