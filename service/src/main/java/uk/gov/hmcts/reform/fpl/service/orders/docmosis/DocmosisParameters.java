package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;

import java.time.LocalDate;
import java.util.List;

@Value
@NonFinal
@SuperBuilder(toBuilder = true)
public abstract class DocmosisParameters {
    // common fields
    String orderTitle;
    String childrenAct;
    String familyManCaseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatterHelper.DATE)
    LocalDate dateOfIssue;
    DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    String courtName;
    List<DocmosisChild> children;

    // images
    String draftbackground;
    String courtseal;
    String crest;

    // lombok cannot know if all subclasses have toBuilder=true so to get around this we manually enforce it
    // see https://stackoverflow.com/a/61651054
    public abstract DocmosisParametersBuilder<?, ?> toBuilder();
}
