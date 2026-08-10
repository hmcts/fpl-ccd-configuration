package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ReportType", generate = true)
@Getter
@RequiredArgsConstructor
public enum CaseProgressionReportType {
    @CCD(label = "At risk")
    AT_RISK("At risk"),
    @CCD(label = "Not meeting timetable")
    MISSING_TIMETABLE("Missing timetable");
    private final String type;
}
