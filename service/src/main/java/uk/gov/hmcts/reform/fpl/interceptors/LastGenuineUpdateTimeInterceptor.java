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
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LastGenuineUpdateTimeInterceptor implements RequestBodyAdvice,
                                                          ResponseBodyAdvice<AboutToStartOrSubmitCallbackResponse> {
    private static final List<String> WHITE_LIST = List.of(
        "/callback/.+/about-to-submit"
    );
    private static final List<String> EXCLUDED_EVENTS = List.of(
        "migrateCase"
    );
    private static final List<String> EXCLUDED_CASE_STATE = List.of(
        State.OPEN.getValue(),
        State.DELETED.getValue(),
        State.RETURNED.getValue()
    );

    private final Time time;
    private final RequestScopeStorage requestScopeStorage;
    private final CafcassApiSearchCaseService cafcassApiSearchCaseService;
    private final FeatureToggleService featureToggleService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(AboutToStartOrSubmitCallbackResponse.class);
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

        if (WHITE_LIST.stream().anyMatch(path::matches) && featureToggleService.isCafcassApiToggledOn()
            && shouldUpdateLastGenuineUpdateTime(body)) {

            body.getData().put("lastGenuineUpdateTime", time.now());
        }
        return body;
    }

    private boolean shouldUpdateLastGenuineUpdateTime(AboutToStartOrSubmitCallbackResponse rsp) {
        if (requestScopeStorage != null && requestScopeStorage.getCallbackRequest() != null) {
            final CallbackRequest callbackRequest = requestScopeStorage.getCallbackRequest();
            if (isNotEmpty(callbackRequest.getEventId())
                && EXCLUDED_EVENTS.stream().anyMatch(callbackRequest.getEventId()::equalsIgnoreCase)) {
                return false;
            }

            if (callbackRequest.getCaseDetails() != null) {
                final String caseStateAfter = isNotEmpty(rsp.getState())
                    ? rsp.getState() : callbackRequest.getCaseDetails().getState();

                if (isNotEmpty(caseStateAfter)
                    && EXCLUDED_CASE_STATE.stream().anyMatch(caseStateAfter::equalsIgnoreCase)) {
                    return false;
                }

                final CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
                // The caseDetailsMap of callBackRsp could be a different instance from the one in the request.
                final CaseDetails caseDetailsAfter = callbackRequest.getCaseDetails().toBuilder()
                    .state(caseStateAfter)
                    .data(rsp.getData())
                    .build();

                if (caseDetailsBefore != null) {
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

                    return !cafcassApiCaseBefore.equals(cafcassApiCaseAfter);
                }
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
