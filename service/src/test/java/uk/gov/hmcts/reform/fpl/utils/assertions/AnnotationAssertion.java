package uk.gov.hmcts.reform.fpl.utils.assertions;

import org.assertj.core.api.AbstractAssert;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

public class AnnotationAssertion extends AbstractAssert<AnnotationAssertion, Class<?>> {

    AnnotationAssertion(Class<?> actual) {
        super(actual, AnnotationAssertion.class);
    }

    public static AnnotationAssertion assertClass(Class<?> actual) {
        return new AnnotationAssertion(actual);
    }

    public AnnotationAssertion hasAsyncMethods(String... expectedMethods) {
        isNotNull();

        var actualAsyncMethods = findMethods(actual, method -> nonNull(method.getAnnotation(Async.class)))
            .stream()
            .map(Method::getName)
            .collect(toSet());

        var expectedAsyncMethod = Set.of(expectedMethods);

        if (!Objects.equals(actualAsyncMethods, expectedAsyncMethod)) {
            failWithMessage("Expecting \n\t%s\n to have @Async methods:\n\t %s\n but the following were found: \n\t %s",
                actual, expectedAsyncMethod, actualAsyncMethods);
        }

        return this;
    }
}
