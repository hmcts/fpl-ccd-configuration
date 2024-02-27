package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

@Builder
@Data
public class ConfidentialDraftOrders {
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsConfidential;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsLA;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp0;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp1;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp2;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp3;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp4;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp5;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp6;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp7;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp8;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsResp9;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild0;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild1;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild2;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild3;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild4;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild5;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild6;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild7;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild8;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild9;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild10;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild11;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild12;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild13;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftsChild14;

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public List<Element<HearingOrdersBundle>> getOrderBySuffix(String suffix) {
        return Arrays.stream(ConfidentialDraftOrders.class.getMethods())
            .filter(method -> method.getName().contains("getHearingOrdersBundlesDrafts" + suffix))
            .map(method -> {
                try {
                    return (List<Element<HearingOrdersBundle>>) method.invoke(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .findFirst().orElse(null);
    }
}
