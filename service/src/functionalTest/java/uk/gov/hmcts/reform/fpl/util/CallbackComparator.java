package uk.gov.hmcts.reform.fpl.util;

import org.json.JSONObject;
import org.skyscreamer.jsonassert.ArrayValueMatcher;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.util.Set;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.getKeys;

public class CallbackComparator extends CustomComparator {
    private static final JSONCompareMode MODE = JSONCompareMode.NON_EXTENSIBLE;

    private CallbackComparator() {
        super(MODE, customizations());
    }

    public static CustomComparator callbackComparator() {
        return new CallbackComparator();
    }

    private static Customization[] customizations() {
        return new Customization[]{
            ignore("data_classification"),
            ignore("security_classification"),
            ignore("data.confidentialOthers", "id")
        };
    }

    private static Customization ignore(String propertyName) {
        return new Customization(propertyName, (o1, o2) -> true);
    }

    private static Customization ignore(String arrayName, String propertyName) {
        return new Customization(arrayName,
            new ArrayValueMatcher<>(new CustomComparator(MODE, ignore(arrayName + "[*]." + propertyName))));
    }

    @Override
    protected void checkJsonObjectKeysActualInExpected(
        String prefix,
        JSONObject expected,
        JSONObject actual,
        JSONCompareResult result) {
        Set<String> actualKeys = getKeys(actual);
        actualKeys.removeAll(Set.of("data_classification", "security_classification"));

        for (String key : actualKeys) {
            if (!expected.has(key)) {
                result.unexpected(prefix, key);
            }
        }
    }
}
