package uk.gov.hmcts.reform.fpl.utils.matchers;

import com.launchdarkly.sdk.AttributeRef;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.ContainsExtraTypeInfo;
import org.mockito.internal.matchers.text.ValuePrinter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LDUserMatcher implements ArgumentMatcher<LDContext>, ContainsExtraTypeInfo, Serializable {
    private final Map<String, LDValue> wanted;

    public static LDUserBuilder ldUser(String env) {
        return new LDUserBuilder().withEnv(env);
    }

    private LDUserMatcher(Map<String, LDValue> wanted) {
        this.wanted = wanted;
    }

    public boolean matches(LDContext actual) {
        return wanted.entrySet().stream()
            .allMatch(entry -> entry.getValue().equals(actual.getValue(AttributeRef.fromLiteral(entry.getKey()))));
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

    public static class LDUserBuilder {
        private Map<String, LDValue> attrs = new HashMap<>();

        public LDUserBuilder withEnv(String env) {
            attrs.put("environment", LDValue.of(env));
            return this;
        }

        public LDUserBuilder withLocalAuthority(String localAuthorityName) {
            attrs.put("localAuthorityName", LDValue.of(localAuthorityName));
            return this;
        }

        public LDUserBuilder with(String name, String value) {
            attrs.put(name, LDValue.of(value));
            return this;
        }

        public LDUserMatcher build() {
            return new LDUserMatcher(attrs);
        }
    }
}
