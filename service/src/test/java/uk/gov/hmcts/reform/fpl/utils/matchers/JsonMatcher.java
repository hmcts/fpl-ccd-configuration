package uk.gov.hmcts.reform.fpl.utils.matchers;

import org.json.JSONObject;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.ContainsExtraTypeInfo;
import org.mockito.internal.matchers.text.ValuePrinter;
import org.skyscreamer.jsonassert.JSONCompare;

import java.io.Serializable;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class JsonMatcher implements ArgumentMatcher<Map<String, Object>>, ContainsExtraTypeInfo, Serializable {

    private final Map<String, Object> wanted;

    public static Map<String, Object> eqJson(Map<String, Object> value) {
        return argThat(new JsonMatcher(value));
    }

    private JsonMatcher(Map<String, Object> wanted) {
        this.wanted = wanted;
    }

    public boolean matches(Map<String, Object> actual) {
        return JSONCompare.compareJSON(new JSONObject(actual), new JSONObject(wanted), STRICT).passed();
    }

    public String toString() {
        return describe(wanted);
    }

    private String describe(Object object) {
        return ValuePrinter.print(object);
    }

    public String toStringWithType() {
        return "(" + wanted.getClass().getSimpleName() + ") " + describe(wanted);
    }

    public boolean typeMatches(Object target) {
        return wanted != null && target != null && target.getClass() == wanted.getClass();
    }
}
