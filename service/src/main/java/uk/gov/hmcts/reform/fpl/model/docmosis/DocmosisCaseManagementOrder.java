package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.util.List;

@Getter
public class DocmosisCaseManagementOrder extends DocmosisOrder {
    private final List<DocmosisRepresentative> representatives;
    private final boolean scheduleProvided;
    private final Schedule schedule;
    private final List<DocmosisRecital> recitals;
    private final boolean recitalsProvided;
    private final DocmosisJudge allocatedJudge;

    @Builder
    public DocmosisCaseManagementOrder(DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                       String courtName,
                                       String familyManCaseNumber,
                                       String dateOfIssue,
                                       String complianceDeadline,
                                       List<DocmosisRespondent> respondents,
                                       List<DocmosisChild> children,
                                       boolean respondentsProvided,
                                       String applicantName,
                                       DocmosisHearingBooking hearingBooking,
                                       List<DocmosisDirection> directions,
                                       String draftbackground,
                                       String courtseal,
                                       String crest,
                                       List<DocmosisRepresentative> representatives,
                                       Schedule schedule,
                                       boolean scheduleProvided,
                                       List<DocmosisRecital> recitals,
                                       boolean recitalsProvided,
                                       DocmosisJudge allocatedJudge,
                                       int numberOfChildren) {
        super(judgeAndLegalAdvisor, courtName, familyManCaseNumber, dateOfIssue,
            complianceDeadline, respondents, children, respondentsProvided, applicantName, hearingBooking, directions,
            draftbackground, courtseal, crest);
        this.representatives = representatives;
        this.schedule = schedule;
        this.scheduleProvided = scheduleProvided;
        this.recitals = recitals;
        this.recitalsProvided = recitalsProvided;
        this.allocatedJudge = allocatedJudge;
    }
}
