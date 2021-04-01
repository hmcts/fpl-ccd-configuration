package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

@Getter
@SuperBuilder(toBuilder = true)
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
    private final String furtherDirections;
    private final String exclusionClause;
    private final String exclusionRequirement;

}
