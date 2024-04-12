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
public class ConfidentialGeneratedOrders implements ConfidentialOrderBundle<GeneratedOrder> {
    private List<Element<GeneratedOrder>> orderCollectionCTSC;
    private List<Element<GeneratedOrder>> orderCollectionLA;
    private List<Element<GeneratedOrder>> orderCollectionResp0;
    private List<Element<GeneratedOrder>> orderCollectionResp1;
    private List<Element<GeneratedOrder>> orderCollectionResp2;
    private List<Element<GeneratedOrder>> orderCollectionResp3;
    private List<Element<GeneratedOrder>> orderCollectionResp4;
    private List<Element<GeneratedOrder>> orderCollectionResp5;
    private List<Element<GeneratedOrder>> orderCollectionResp6;
    private List<Element<GeneratedOrder>> orderCollectionResp7;
    private List<Element<GeneratedOrder>> orderCollectionResp8;
    private List<Element<GeneratedOrder>> orderCollectionResp9;
    private List<Element<GeneratedOrder>> orderCollectionChild0;
    private List<Element<GeneratedOrder>> orderCollectionChild1;
    private List<Element<GeneratedOrder>> orderCollectionChild2;
    private List<Element<GeneratedOrder>> orderCollectionChild3;
    private List<Element<GeneratedOrder>> orderCollectionChild4;
    private List<Element<GeneratedOrder>> orderCollectionChild5;
    private List<Element<GeneratedOrder>> orderCollectionChild6;
    private List<Element<GeneratedOrder>> orderCollectionChild7;
    private List<Element<GeneratedOrder>> orderCollectionChild8;
    private List<Element<GeneratedOrder>> orderCollectionChild9;
    private List<Element<GeneratedOrder>> orderCollectionChild10;
    private List<Element<GeneratedOrder>> orderCollectionChild11;
    private List<Element<GeneratedOrder>> orderCollectionChild12;
    private List<Element<GeneratedOrder>> orderCollectionChild13;
    private List<Element<GeneratedOrder>> orderCollectionChild14;

    public String getFieldBaseName() {
        return "orderCollection";
    }
}
