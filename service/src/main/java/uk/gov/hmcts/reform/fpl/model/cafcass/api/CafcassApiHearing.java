package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CafcassApiHearing {
    private String id;
    private HearingType type;
    private String typeDetails;
    private String venue;
    private HearingStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<HearingAttendance> attendance;
    private String cancellationReason;
    private String preAttendanceDetails;
    private String attendanceDetails;
}
