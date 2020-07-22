package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SelectorType.SELECTED;

@JsonComponent
public class SelectorDeserializer extends JsonDeserializer<Selector> {

    private static final String OPTION_BASE_NAME = "option";
    private static final String OPTIONS_COUNT_NAME = "optionCount";
    private static final String OPTION_NAME_REGEXP = "option\\d+$";

    // Cannot deserialise option1Hidden etc as they are hidden fields and therefore aren't sent back to use by CCD
    @Override
    public Selector deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        TreeNode rootNode = parser.getCodec().readTree(parser);
        Selector selector = Selector.builder().build();

        List<Integer> selected = new ArrayList<>();
        Iterator<String> fieldNames = rootNode.fieldNames();

        fieldNames.forEachRemaining(fieldName -> {
            if (OPTIONS_COUNT_NAME.equals(fieldName)) {
                selector.setCount(getCountContainer(rootNode));
            } else if (isOptionNode(fieldName) && isSelected(rootNode.get(fieldName))) {
                int i = Integer.parseInt(fieldName.replace(OPTION_BASE_NAME, ""));
                selected.add(i);
            }
        });

        selector.setSelected(selected);

        return selector;
    }

    private String getCountContainer(TreeNode treeNode) {
        TreeNode node = treeNode.get(OPTIONS_COUNT_NAME);
        return isNodeNull(node) ? "" : ((TextNode) node).asText();
    }

    private boolean isOptionNode(String fieldName) {
        return fieldName.matches(OPTION_NAME_REGEXP);
    }

    private boolean isSelected(TreeNode node) {
        return !isNodeNull(node) && (
            (node.isArray() && containsSelected((ArrayNode) node))
                || node.isValueNode() && YesNo.YES.getValue().equals(((ValueNode) node).asText()));
    }

    private boolean containsSelected(ArrayNode node) {
        return node.size() == 1 && (SELECTED.name().equals(node.get(0).asText())
            || YesNo.YES.getValue().equals(node.get(0).asText()));
    }

    private boolean isNodeNull(TreeNode node) {
        return node == null || node instanceof NullNode;
    }
}
