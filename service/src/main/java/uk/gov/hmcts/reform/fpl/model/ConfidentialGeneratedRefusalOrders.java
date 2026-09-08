package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfidentialGeneratedRefusalOrders implements ConfidentialOrderBundle<GeneratedOrder> {
    private List<Element<GeneratedOrder>> refusalOrdersCTSC;
    private List<Element<GeneratedOrder>> refusalOrdersLA;
    private List<Element<GeneratedOrder>> refusalOrdersResp0;
    private List<Element<GeneratedOrder>> refusalOrdersResp1;
    private List<Element<GeneratedOrder>> refusalOrdersResp2;
    private List<Element<GeneratedOrder>> refusalOrdersResp3;
    private List<Element<GeneratedOrder>> refusalOrdersResp4;
    private List<Element<GeneratedOrder>> refusalOrdersResp5;
    private List<Element<GeneratedOrder>> refusalOrdersResp6;
    private List<Element<GeneratedOrder>> refusalOrdersResp7;
    private List<Element<GeneratedOrder>> refusalOrdersResp8;
    private List<Element<GeneratedOrder>> refusalOrdersResp9;
    private List<Element<GeneratedOrder>> refusalOrdersChild0;
    private List<Element<GeneratedOrder>> refusalOrdersChild1;
    private List<Element<GeneratedOrder>> refusalOrdersChild2;
    private List<Element<GeneratedOrder>> refusalOrdersChild3;
    private List<Element<GeneratedOrder>> refusalOrdersChild4;
    private List<Element<GeneratedOrder>> refusalOrdersChild5;
    private List<Element<GeneratedOrder>> refusalOrdersChild6;
    private List<Element<GeneratedOrder>> refusalOrdersChild7;
    private List<Element<GeneratedOrder>> refusalOrdersChild8;
    private List<Element<GeneratedOrder>> refusalOrdersChild9;
    private List<Element<GeneratedOrder>> refusalOrdersChild10;
    private List<Element<GeneratedOrder>> refusalOrdersChild11;
    private List<Element<GeneratedOrder>> refusalOrdersChild12;
    private List<Element<GeneratedOrder>> refusalOrdersChild13;
    private List<Element<GeneratedOrder>> refusalOrdersChild14;

    public String getFieldBaseName() {
        return "refusalOrders";
    }
}
