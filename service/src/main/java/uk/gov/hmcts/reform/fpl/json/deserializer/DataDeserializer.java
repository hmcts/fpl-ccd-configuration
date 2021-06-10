package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;

import java.io.IOException;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@JsonComponent
public class DataDeserializer extends JsonDeserializer<GatekeepingOrderEventData> {

    @Autowired
    private OrdersLookupService ordersLookupService;

    @Override
    public GatekeepingOrderEventData deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {

        GatekeepingOrderEventData sd = parser.readValueAs(GatekeepingOrderEventData.class);

        String xxx = parser.getParsingContext().getCurrentName().replace("sdoDirection-", "");
        System.out.println("Field name " + xxx);
        DirectionType dt = DirectionType.valueOf(xxx);

        System.out.println("DT " + dt);

        DirectionConfiguration directionConfig = ordersLookupService.getDirectionConfiguration(dt);

        return null;
    }

    private String getProperty(TreeNode root, String name) {
        final TreeNode node = root.get(name);
        return node instanceof TextNode ? ((TextNode) node).asText() : null;
    }
}
