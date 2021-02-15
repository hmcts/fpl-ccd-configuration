package uk.gov.hmcts.reform.fpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.utils.TestLoggerAppender;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.util.ReflectionUtils.findMethod;

@ExtendWith(MockitoExtension.class)
class CallbackRequestLoggerTest {

    @Mock
    private HttpInputMessage httpInputMessage;

    @Mock
    private HttpHeaders httpHeaders;

    private CallbackRequestLogger callbackLogger = new CallbackRequestLogger();

    @Test
    void shouldActivateAdviceOnCcdCallback() {
        boolean activate = callbackLogger.supports(null, CallbackRequest.class, null);

        assertThat(activate).isTrue();
    }

    @Test
    void shouldNotActivateAdviceOnNonCcdCallback() {
        boolean activate = callbackLogger.supports(null, String.class, null);

        assertThat(activate).isFalse();
    }

    @Test
    void shouldLogCallbackDetails() {

        when(httpHeaders.getOrEmpty("user-id"))
            .thenReturn(List.of("1"));
        when(httpHeaders.getOrEmpty("user-roles"))
            .thenReturn(List.of("pui-case-manager", "caseworker-publiclaw-solicitor"));
        when(httpInputMessage.getHeaders()).thenReturn(httpHeaders);

        try (TestLoggerAppender logsAppender = new TestLoggerAppender(CallbackRequestLogger.class)) {
            final CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("OPEN")
                .data(Map.of("caseLocalAuthority", "SA"))
                .build();

            final CallbackRequest callbackRequest = CallbackRequest.builder()
                .eventId("Test event")
                .caseDetails(caseDetails)
                .build();

            MethodParameter methodParameter = new MethodParameter(findMethod(this.getClass(), "ccdCallback"), -1);

            callbackLogger.afterBodyRead(callbackRequest, httpInputMessage, methodParameter, null, null);

            assertThat(logsAppender.getInfos()).containsExactly(
                "Callback(event='Test event',type='/mid-event') "
                    + "User(id='1',roles='pui-case-manager,caseworker-publiclaw-solicitor') "
                    + "Case(id='10' state='OPEN' la='SA' case number='')");
            assertThat(logsAppender.getWarns()).isEmpty();
            assertThat(logsAppender.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldLogErrorWhenLogCannotBeCreated() {

        try (TestLoggerAppender logs = new TestLoggerAppender(CallbackRequestLogger.class)) {
            callbackLogger.afterBodyRead(null, null, null, null, null);
            assertThat(logs.getWarns()).containsExactly("Can not log case details");
        }
    }

    @PostMapping("/mid-event")
    AboutToStartOrSubmitCallbackResponse ccdCallback() {
        return null;
    }

}
