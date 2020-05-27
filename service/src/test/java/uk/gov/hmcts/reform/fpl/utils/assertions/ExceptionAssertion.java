package uk.gov.hmcts.reform.fpl.utils.assertions;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class ExceptionAssertion extends AbstractAssert<ExceptionAssertion, Throwable> {

    ExceptionAssertion(Throwable actual) {
        super(actual, ExceptionAssertion.class);
    }

    public static ExceptionAssertion assertException(Throwable actual) {
        return new ExceptionAssertion(actual);
    }

    public ExceptionAssertion isCausedBy(Throwable expectedRootCause) {
        isNotNull();

        final Throwable actualRootCause = ExceptionUtils.getRootCause(actual);

        if (!Objects.equals(actualRootCause, expectedRootCause)) {
            failWithMessage("Expecting \n\t%s\n to be casued by :\n\t %s\n but was caused by: \n\t %s",
                actual, expectedRootCause, actualRootCause);
        }

        return this;
    }
}
