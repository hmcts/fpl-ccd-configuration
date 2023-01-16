package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseProgressionReportType {
    AT_RISK("At risk"),
    MISSING_TIMETABLE("Missing timetable");
    private final String type;
}
