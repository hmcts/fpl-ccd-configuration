package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;

@Data
@Builder
public class ConfidentialRefusedOrders implements ConfidentialOrderBundle<HearingOrder> {

    private List<Element<HearingOrder>> refusedHearingOrdersCTSC;
    private List<Element<HearingOrder>> refusedHearingOrdersLA;
    private List<Element<HearingOrder>> refusedHearingOrdersResp0;
    private List<Element<HearingOrder>> refusedHearingOrdersResp1;
    private List<Element<HearingOrder>> refusedHearingOrdersResp2;
    private List<Element<HearingOrder>> refusedHearingOrdersResp3;
    private List<Element<HearingOrder>> refusedHearingOrdersResp4;
    private List<Element<HearingOrder>> refusedHearingOrdersResp5;
    private List<Element<HearingOrder>> refusedHearingOrdersResp6;
    private List<Element<HearingOrder>> refusedHearingOrdersResp7;
    private List<Element<HearingOrder>> refusedHearingOrdersResp8;
    private List<Element<HearingOrder>> refusedHearingOrdersResp9;
    private List<Element<HearingOrder>> refusedHearingOrdersChild0;
    private List<Element<HearingOrder>> refusedHearingOrdersChild1;
    private List<Element<HearingOrder>> refusedHearingOrdersChild2;
    private List<Element<HearingOrder>> refusedHearingOrdersChild3;
    private List<Element<HearingOrder>> refusedHearingOrdersChild4;
    private List<Element<HearingOrder>> refusedHearingOrdersChild5;
    private List<Element<HearingOrder>> refusedHearingOrdersChild6;
    private List<Element<HearingOrder>> refusedHearingOrdersChild7;
    private List<Element<HearingOrder>> refusedHearingOrdersChild8;
    private List<Element<HearingOrder>> refusedHearingOrdersChild9;
    private List<Element<HearingOrder>> refusedHearingOrdersChild10;
    private List<Element<HearingOrder>> refusedHearingOrdersChild11;
    private List<Element<HearingOrder>> refusedHearingOrdersChild12;
    private List<Element<HearingOrder>> refusedHearingOrdersChild13;
    private List<Element<HearingOrder>> refusedHearingOrdersChild14;

    @Override
    public String getFieldBaseName() {
        return "refusedHearingOrders";
    }
}
