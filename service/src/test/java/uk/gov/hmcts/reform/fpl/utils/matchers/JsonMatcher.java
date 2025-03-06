package uk.gov.hmcts.reform.fpl.utils.matchers;

import org.json.JSONObject;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.ContainsExtraTypeInfo;
import org.mockito.internal.matchers.text.ValuePrinter;
import org.skyscreamer.jsonassert.JSONCompare;

import java.io.Serializable;

import static org.mockito.ArgumentMatchers.argThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

public class JsonMatcher implements ArgumentMatcher<Object>, ContainsExtraTypeInfo, Serializable {

    private final Object wanted;

    @SuppressWarnings("unchecked")
    public static <T> T eqJson(T value) {
        return (T) argThat(new JsonMatcher(value));
    }

    private JsonMatcher(Object wanted) {
        this.wanted = wanted;
    }

    public boolean matches(Object actual) {
        return JSONCompare.compareJSON(new JSONObject(actual), new JSONObject(wanted), NON_EXTENSIBLE).passed();
    }

    public String toString() {
        return describe(wanted);
    }

    private String describe(Object object) {
        return ValuePrinter.print(object);
    }

    public String toStringWithType(String className) {
        return "(" + className + ") " + this.describe(this.wanted);
    }

    public boolean typeMatches(Object target) {
        return wanted != null && target != null && target.getClass() == wanted.getClass();
    }

    public final Object getWanted() {
        return this.wanted;
    }
}
