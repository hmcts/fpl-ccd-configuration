package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

import java.util.List;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DocmosisGeneratedOrder extends DocmosisOrder {
    private final String orderTitle;
    private final String childrenAct;
    private final String orderDetails;
    private final String localAuthorityName;
    private final String childrenDescription;
    private final EPOType epoType;
    private final String includePhrase;
    private final String removalAddress;
    private final String epoStartDateTime;
    private final String epoEndDateTime;
    private final GeneratedOrderType orderType;
    private final String familyManCaseNumber;
    private final String courtName;
    private final String dateOfIssue;
    private final String judgeTitleAndName;
    private final String legalAdvisorName;
    private final List<DocmosisChild> children;
    private final Integer childrenCount;
    private final String furtherDirections;
    private final String crest;
    private final String draftbackground;
    private final String courtseal;
}
