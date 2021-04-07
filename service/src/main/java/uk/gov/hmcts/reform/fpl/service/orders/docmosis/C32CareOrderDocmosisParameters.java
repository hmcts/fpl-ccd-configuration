package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;

@Value
@Builder
public class C32CareOrderDocmosisParameters implements DocmosisParameters {

    @Builder.Default
    String orderTitle = "Care order";
    @Builder.Default
    String childrenAct = "Section 31 Children Act 1989";
    @Builder.Default
    GeneratedOrderType orderType = CARE_ORDER;

    String familyManCaseNumber;
    String dateOfIssue;
    DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    String courtName;
    List<DocmosisChild> children;
    String orderDetails;
    String furtherDirections;
    String localAuthorityName;

    // images
    String draftbackground;
    String courtseal;
    String crest;
}
