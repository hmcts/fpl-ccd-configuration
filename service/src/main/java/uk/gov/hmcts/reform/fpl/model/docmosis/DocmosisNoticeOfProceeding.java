package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisNoticeOfProceeding implements DocmosisData {
    private final String courtName;
    private final String familyManCaseNumber;
    private final String todaysDate;
    private final String applicantName;
    private final String orderTypes;
    private final String childrenNames;
    private final String judgeTitleAndName;
    private final String legalAdvisorName;
    private final String crest;
    private final String courtseal;
    private final String hearingDate;
    private final String hearingVenue;
    private final String preHearingAttendance;
    private final String hearingTime;
}
