package uk.gov.hmcts.reform.fpl.utils.extension;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TestLogsExtension implements AfterTestExecutionCallback, TestInstancePostProcessor,
    TestInstancePreDestroyCallback {

    private List<TestLogger> loggers;

    @Override
    public void afterTestExecution(ExtensionContext context) {
        loggers.forEach(TestLogger::reset);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        loggers = FieldUtils.getFieldsListWithAnnotation(testInstance.getClass(), TestLogs.class).stream()
            .map(field -> {
                try {
                    boolean isAccessible = field.canAccess(testInstance);
                    if (!isAccessible) {
                        field.setAccessible(true);
                    }

                    TestLogger testLogger = (TestLogger) field.get(testInstance);

                    if (!isAccessible) {
                        field.setAccessible(false);
                    }

                    return testLogger;

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(toList());
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) {
        loggers.forEach(TestLogger::close);
    }
}
