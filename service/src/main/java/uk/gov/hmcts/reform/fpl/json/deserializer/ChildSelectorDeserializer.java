package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import java.io.IOException;

// TODO: 31/01/2020 Test me
@JsonComponent
public class ChildSelectorDeserializer extends JsonDeserializer<ChildSelector> {
    @Override
    public ChildSelector deserialize(JsonParser parser,
                                     DeserializationContext context) throws IOException {
        TreeNode treeNode = parser.getCodec().readTree(parser);
        String childCount = getChildCountContainer(treeNode);
        return ChildSelector.builder()
            .childCountContainer(childCount)
            .child1(readChildNode(treeNode, 1))
            .child2(readChildNode(treeNode, 2))
            .child3(readChildNode(treeNode, 3))
            .child4(readChildNode(treeNode, 4))
            .child5(readChildNode(treeNode, 5))
            .child6(readChildNode(treeNode, 6))
            .child7(readChildNode(treeNode, 7))
            .child8(readChildNode(treeNode, 8))
            .child9(readChildNode(treeNode, 9))
            .child10(readChildNode(treeNode, 10))
            .build();
    }

    private String getChildCountContainer(TreeNode treeNode) {
        TreeNode node = treeNode.get("childCountContainer");
        return isNodeNull(node) ? "" : ((TextNode) node).asText();
    }

    private boolean readChildNode(TreeNode treeNode, int child) {
        return toBoolean((ArrayNode) treeNode.get("child" + child));
    }

    private boolean toBoolean(ArrayNode arrayNode) {
        return !isNodeNull(arrayNode) && arrayNode.size() != 0;
    }

    private boolean isNodeNull(TreeNode node) {
        return node == null || node instanceof NullNode;
    }
}
