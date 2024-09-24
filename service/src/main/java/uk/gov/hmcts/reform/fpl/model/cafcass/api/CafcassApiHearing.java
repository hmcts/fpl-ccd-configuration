package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
