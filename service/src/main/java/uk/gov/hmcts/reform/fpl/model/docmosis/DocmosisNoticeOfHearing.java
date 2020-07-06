package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode
public class DocmosisNoticeOfHearing implements DocmosisData {
    private final List<DocmosisChild> children;
    private final DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private String postingDate;
    private String hearingType;
    private String hearingVenue;
    private String hearingDate;
    private String hearingStartTime;
    private String hearingEndTime;
}
