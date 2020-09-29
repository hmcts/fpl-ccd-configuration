package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class DocmosisNoticeOfHearing implements DocmosisData {
    private final List<DocmosisChild> children;
    private final DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private String familyManCaseNumber;
    private String courtName;
    private String postingDate;
    private DocmosisHearingBooking hearingBooking;
    private final String additionalNotes;
    private final String courtseal;
    private final String crest;
}
