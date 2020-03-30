package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class DocmosisStandardDirectionOrder extends AbstractDocmosisOrder {

    @Builder
    public DocmosisStandardDirectionOrder(DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor,
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
                                          String courtseal) {
        super(judgeAndLegalAdvisor, courtName, familyManCaseNumber, dateOfIssue, complianceDeadline, respondents,
            children, respondentsProvided, applicantName, hearingBooking, directions, draftbackground, courtseal);
    }
}
