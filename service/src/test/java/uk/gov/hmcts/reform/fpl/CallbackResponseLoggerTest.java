package uk.gov.hmcts.reform.fpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.utils.TestLoggerAppender;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.util.ReflectionUtils.findMethod;

@ExtendWith(MockitoExtension.class)
class CallbackResponseLoggerTest {

    @Mock
    private ServerHttpRequest httpRequest;

    private final CallbackResponseLogger callbackLogger = new CallbackResponseLogger();

    @Test
    void shouldActivateAdviceOnCcdCallback() {
        final Method ccdCallbackMethod = findMethod(this.getClass(), "ccdCallback");
        final MethodParameter methodParameter = new MethodParameter(ccdCallbackMethod, -1);

        boolean activate = callbackLogger.supports(methodParameter, null);

        assertThat(activate).isTrue();
    }

    @Test
    void shouldNotActivateAdviceOnNonCcdCallback() {
        final Method nonCcdCallbackMethod = findMethod(this.getClass(), "nonCcdCallback");
        final MethodParameter methodParameter = new MethodParameter(nonCcdCallbackMethod, -1);

        boolean activate = callbackLogger.supports(methodParameter, null);

        assertThat(activate).isFalse();
    }

    @Test
    void shouldLogCallbackDetailsWhenErrorPresents() throws Exception {

        when(httpRequest.getURI()).thenReturn(new URI("http://test.com/callback/test/mid-event"));

        try (TestLoggerAppender logsAppender = new TestLoggerAppender(CallbackResponseLogger.class)) {

            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("error 1", "error 2"))
                .build();

            Object res = callbackLogger.beforeBodyWrite(response, null, null, null, httpRequest, null);

            assertThat(res).isEqualTo(response);

            assertThat(logsAppender.getInfos()).containsExactly(
                "Callback(/callback/test/mid-event) ended with errors=[error 1, error 2] warnings=[]");

            assertThat(logsAppender.getWarns()).isEmpty();
            assertThat(logsAppender.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldLogCallbackDetailsWhenWarningsPresents() throws Exception {

        when(httpRequest.getURI()).thenReturn(new URI("http://test.com/callback/test/mid-event"));

        try (TestLoggerAppender logsAppender = new TestLoggerAppender(CallbackResponseLogger.class)) {

            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
                .warnings(List.of("warning 1", "warning 2"))
                .build();

            Object res = callbackLogger.beforeBodyWrite(response, null, null, null, httpRequest, null);

            assertThat(res).isEqualTo(response);

            assertThat(logsAppender.getInfos()).containsExactly(
                "Callback(/callback/test/mid-event) ended with errors=[] warnings=[warning 1, warning 2]");

            assertThat(logsAppender.getWarns()).isEmpty();
            assertThat(logsAppender.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldNotLogCallbackDetailsWhenNoErrorsNorWarnings() {

        try (TestLoggerAppender logsAppender = new TestLoggerAppender(CallbackResponseLogger.class)) {

            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
                .build();

            callbackLogger.beforeBodyWrite(response, null, null, null, httpRequest, null);

            assertThat(logsAppender.get()).isEmpty();
        }
    }

    @Test
    void shouldLogErrorWhenLogCannotBeCreated() {

        try (TestLoggerAppender logs = new TestLoggerAppender(CallbackResponseLogger.class)) {
            callbackLogger.beforeBodyWrite(null, null, null, null, null, null);

            assertThat(logs.getWarns()).containsExactly("Can not log case details");
        }
    }

    @PostMapping("/mid-event")
    AboutToStartOrSubmitCallbackResponse ccdCallback() {
        return null;
    }

    @PostMapping("/mid-event")
    String nonCcdCallback() {
        return null;
    }

}
