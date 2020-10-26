package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReason;

import java.io.IOException;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@JsonComponent
public class HearingCancellationReasonDeserializer extends JsonDeserializer<HearingCancellationReason> {

    @Override
    public HearingCancellationReason deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        TreeNode rootNode = parser.getCodec().readTree(parser);
        String type = getProperty(rootNode, "type");
        String reason = defaultIfNull(
            getProperty(rootNode, "reason-" + type),
            getProperty(rootNode, "reason"));

        return HearingCancellationReason
            .builder()
            .type(type)
            .reason(reason)
            .build();
    }

    private String getProperty(TreeNode root, String name) {
        final TreeNode node = root.get(name);
        return node instanceof TextNode ? ((TextNode) node).asText() : null;
    }
}
