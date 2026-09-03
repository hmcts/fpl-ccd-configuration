package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedRefusalOrder;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfidentialGeneratedRefusalOrders implements ConfidentialOrderBundle<GeneratedRefusalOrder> {
    private List<Element<GeneratedRefusalOrder>> refusalOrdersCTSC;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersLA;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp0;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp1;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp2;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp3;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp4;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp5;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp6;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp7;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp8;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersResp9;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild0;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild1;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild2;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild3;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild4;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild5;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild6;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild7;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild8;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild9;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild10;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild11;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild12;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild13;
    private List<Element<GeneratedRefusalOrder>> refusalOrdersChild14;

    public String getFieldBaseName() {
        return "refusalOrders";
    }
}
