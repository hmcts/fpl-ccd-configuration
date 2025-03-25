package uk.gov.hmcts.reform.fpl.interceptors;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LastGenuineUpdateTimedInterceptor implements RequestBodyAdvice,
                                                          ResponseBodyAdvice<AboutToStartOrSubmitCallbackResponse> {
    private static final List<String> WHITE_LIST = List.of(
        "/callback/.+/about-to-submit"
    );
    private static final List<String> EXCLUDED_LIST = List.of(
        "/callback/migrate-case/about-to-submit",
        "/callback/message-judge/about-to-submit",
        "/callback/reply-message-judge/about-to-submit",
        "/callback/add-note/about-to-submit"
    );
    private static final List<String> EXCLUDED_CASE_STATE = List.of(
        State.OPEN.getValue(),
        State.DELETED.getValue(),
        State.RETURNED.getValue()
    );


    private final RequestScopeStorage requestScopeStorage;
    private final CafcassApiSearchCaseService cafcassApiSearchCaseService;


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getGenericParameterType().equals(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.getGenericParameterType().equals(CallbackRequest.class);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse beforeBodyWrite(AboutToStartOrSubmitCallbackResponse body,
                                                                MethodParameter returnType,
                                                                MediaType selectedContentType,
                                                                Class<? extends HttpMessageConverter<?>>
                                                                        selectedConverterType,
                                                                ServerHttpRequest request,
                                                                ServerHttpResponse response) {
        final String path = request.getURI().getPath();

        if (WHITE_LIST.stream().anyMatch(path::matches) && EXCLUDED_LIST.stream().noneMatch(path::matches)) {
            if (shouldUpdateLastGenuineUpdateTimed()) {
                body.getData().put("lastGenuineUpdateTimed", LocalDateTime.now());
            }
        }

        return body;
    }

    private boolean shouldUpdateLastGenuineUpdateTimed() {
        if (requestScopeStorage != null && requestScopeStorage.getCallbackRequest() != null
            && requestScopeStorage.getCallbackRequest().getCaseDetailsBefore() != null
            && requestScopeStorage.getCallbackRequest().getCaseDetails() != null) {

            final CaseDetails caseDetailsBefore = requestScopeStorage.getCallbackRequest().getCaseDetailsBefore();
            final CaseDetails caseDetailsAfter = requestScopeStorage.getCallbackRequest().getCaseDetails();

            if (isNotEmpty(caseDetailsAfter.getState())
                && EXCLUDED_CASE_STATE.stream().anyMatch(caseDetailsAfter.getState()::equalsIgnoreCase)) {
                return false;
            }

            CafcassApiCase cafcassApiCaseBefore = cafcassApiSearchCaseService
                .convertToCafcassApiCase(caseDetailsBefore)
                .toBuilder()
                .lastModified(null)
                .build();

            CafcassApiCase cafcassApiCaseAfter = cafcassApiSearchCaseService
                .convertToCafcassApiCase(caseDetailsAfter)
                .toBuilder()
                .lastModified(null)
                .build();

            if (cafcassApiCaseBefore.equals(cafcassApiCaseAfter)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        if (body instanceof CallbackRequest callbackRequest) {
            requestScopeStorage.setCallbackRequest(callbackRequest);
        }

        return body;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        // Do nothing
        return inputMessage;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Do nothing
        return body;
    }

    /**
     * A bean for storing the incoming request. It is managed by Spring under request scope,
     * which mean new instance is instantiated independently for each incoming HTTP request.
     */
    @Component
    @RequestScope
    @Data
    static class RequestScopeStorage {
        private CallbackRequest callbackRequest;
    }
}
