package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SelectorType.SELECTED;

public abstract class SelectorDeserializer<T extends Selector> extends JsonDeserializer<T> {

    final String fieldBaseName;
    final String fieldCountName;
    final String fieldNodeRegexp;

    public SelectorDeserializer(String fieldBaseName) {
        this.fieldBaseName = fieldBaseName;
        this.fieldCountName = fieldBaseName + "Count";
        this.fieldNodeRegexp = fieldBaseName + "\\d+$";
    }

    // Cannot deserialise child1Hidden etc as they are hidden fields and therefore aren't sent back to use by CCD
    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        TreeNode rootNode = parser.getCodec().readTree(parser);
        T selector = newSelector();

        List<Integer> selected = new ArrayList<>();
        Iterator<String> fieldNames = rootNode.fieldNames();

        fieldNames.forEachRemaining(fieldName -> {
            if (fieldCountName.equals(fieldName)) {
                selector.setCount(getCountContainer(rootNode));
            } else if (isChildNode(fieldName) && isSelected(rootNode.get(fieldName))) {
                int i = Integer.parseInt(fieldName.replace(fieldBaseName, ""));
                selected.add(i);
            }
        });

        selector.setSelected(selected);

        return selector;
    }

    abstract T newSelector();

    private String getCountContainer(TreeNode treeNode) {
        TreeNode node = treeNode.get(fieldCountName);
        return isNodeNull(node) ? "" : ((TextNode) node).asText();
    }

    private boolean isChildNode(String fieldName) {
        return fieldName.matches(fieldNodeRegexp);
    }

    private boolean isSelected(TreeNode node) {
        return !isNodeNull(node) && node.isArray() && containsSelected((ArrayNode) node);
    }

    private boolean containsSelected(ArrayNode node) {
        return node.size() == 1 && SELECTED.name().equals(node.get(0).asText());
    }

    private boolean isNodeNull(TreeNode node) {
        return node == null || node instanceof NullNode;
    }
}
