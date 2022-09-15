package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class HearingInfo {
    private final String familyManCaseNumber;
    private final String ccdNumber;
    private final String dateSubmitted;
    private final String lastHearing;
    private final String nextHearing;
    private final String ageInWeeks;
    private final String ploStage;
    private final String expectedFinalHearing;
    private final Court cOurt;
}
