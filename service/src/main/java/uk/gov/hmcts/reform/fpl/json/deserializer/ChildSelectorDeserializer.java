package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildSelectorType.SELECTED;

@JsonComponent
public class ChildSelectorDeserializer extends JsonDeserializer<ChildSelector> {

    @Override
    public ChildSelector deserialize(JsonParser parser,
                                     DeserializationContext ctxt) throws IOException {

        TreeNode treeNode = parser.getCodec().readTree(parser);
        ChildSelector.ChildSelectorBuilder builder = ChildSelector.builder();

        List<Integer> selected = new ArrayList<>();
        Iterator<String> fieldNames = treeNode.fieldNames();

        fieldNames.forEachRemaining(s -> {
            if ("childCount".equals(s)) {
                builder.childCount(getChildCountContainer(treeNode));
            } else if (isChildNode(s) && isSelected(treeNode.get(s))) {
                int i = Integer.parseInt(s.replace("child", ""));
                selected.add(i);
            }
        });

        return builder.selected(selected).build();
    }

    private String getChildCountContainer(TreeNode treeNode) {
        TreeNode node = treeNode.get("childCount");
        return isNodeNull(node) ? "" : ((TextNode) node).asText();
    }

    private boolean isChildNode(String s) {
        return s.matches("child\\d+");
    }

    private boolean isSelected(TreeNode node) {
        return !isNodeNull(node) && node.isArray() && containsSelected((ArrayNode) node);
    }

    private boolean containsSelected(ArrayNode node) {
        return node.size() == 1 && node.get(0).asText().equals(SELECTED.name());
    }

    private boolean isNodeNull(TreeNode node) {
        return node == null || node instanceof NullNode;
    }
}
