package uk.gov.hmcts.reform.fpl.interceptors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LastGenuineUpdateTimedInterceptorTest {
    private static final MethodParameter HANDLE_ABOUT_TO_SUBMIT_PARAMETER_TYPE = mock(MethodParameter.class);
    private static final MethodParameter HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE = mock(MethodParameter.class);
    private static final MethodParameter DUMMY_METHOD_RETURN_TYPE = mock(MethodParameter.class);
    private static final ServerHttpRequest REQUEST_WHITELISTED = mock(ServerHttpRequest.class);
    private static final ServerHttpRequest REQUEST_EXCLUDED = mock(ServerHttpRequest.class);
    private static final ServerHttpResponse RESPONSE = mock(ServerHttpResponse.class);

    @Mock
    private LastGenuineUpdateTimedInterceptor.RequestScopeStorage requestScopeStorage;
    @Mock
    private CafcassApiSearchCaseService cafcassApiSearchCaseService;
    @InjectMocks
    private LastGenuineUpdateTimedInterceptor underTest;

    @BeforeAll
    static void init() {
        when(HANDLE_ABOUT_TO_SUBMIT_PARAMETER_TYPE.getGenericParameterType())
            .thenReturn(CallbackRequest.class);
        when(HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE.getGenericParameterType())
            .thenReturn(AboutToStartOrSubmitCallbackResponse.class);
        when(DUMMY_METHOD_RETURN_TYPE.getGenericParameterType())
            .thenReturn(String.class);
        when(REQUEST_WHITELISTED.getURI())
            .thenReturn(URI.create("http://localhost/callback/generic-update/about-to-submit"));
        when(REQUEST_EXCLUDED.getURI())
            .thenReturn(URI.create("http://localhost/callback/migrate-case/about-to-submit"));
    }

    @Nested
    class ResponseBodyAdviceTest{
        private static final Map<String, Object> CASE_MAP_BEFORE = Map.of("id", 1L);
        private static final CaseDetails CASE_DETAILS_BEFORE = CaseDetails.builder()
            .state(State.GATEKEEPING.getValue()).data(CASE_MAP_BEFORE).build();
        private static final Map<String, Object> CASE_MAP_AFTER = Map.of("id", 1L, "caseName", "Test");
        private static final CaseDetails CASE_DETAILS_AFTER = CaseDetails.builder()
            .state(State.CASE_MANAGEMENT.getValue()).data(CASE_MAP_AFTER).build();
        private static final CafcassApiCase CAFCASS_API_CASE_BEFORE = CafcassApiCase.builder()
            .id(1L).state(State.GATEKEEPING.getValue()).build();
        private static final CafcassApiCase CAFCASS_API_CASE_AFTER = CafcassApiCase.builder()
            .id(1L).state(State.CASE_MANAGEMENT.getValue()).build();

        @Test
        void shouldReturnTrueWhenMethodParameterMatch() throws NoSuchMethodException {
            assertTrue(underTest.supports(HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE, null));
        }

        @Test
        void shouldReturnFalseWhenMethodParameterNotMatch() throws NoSuchMethodException {
            assertFalse(underTest.supports(DUMMY_METHOD_RETURN_TYPE, null));
        }

        @Test
        void shouldUpdateTimestampIfApiResponseChanged() {
            when(requestScopeStorage.getCallbackRequest())
                .thenReturn(CallbackRequest.builder()
                    .caseDetailsBefore(CASE_DETAILS_BEFORE)
                    .caseDetails(CASE_DETAILS_AFTER)
                    .build());
            when(cafcassApiSearchCaseService.convertToCafcassApiCase(CASE_DETAILS_BEFORE))
                .thenReturn(CAFCASS_API_CASE_BEFORE);
            when(cafcassApiSearchCaseService.convertToCafcassApiCase(CASE_DETAILS_AFTER))
                .thenReturn(CAFCASS_API_CASE_AFTER);

            AboutToStartOrSubmitCallbackResponse controllerResponse = AboutToStartOrSubmitCallbackResponse.builder()
                .data(new HashMap<>(CASE_MAP_AFTER))
                .build();

            AboutToStartOrSubmitCallbackResponse interceptedResponse = underTest.beforeBodyWrite(controllerResponse,
                HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE, MediaType.APPLICATION_JSON, null,
                REQUEST_WHITELISTED, RESPONSE);

            assertNotNull(interceptedResponse);
            assertNotNull(interceptedResponse.getData());
            assertNotNull(interceptedResponse.getData().get("lastGenuineUpdateTimed"));
            assertEquals(1L, interceptedResponse.getData().get("id"));
            assertEquals("Test", interceptedResponse.getData().get("caseName"));
        }

        @Test
        void shouldNotUpdateTimestampIfApiResponseUnchanged() {
            when(requestScopeStorage.getCallbackRequest())
                .thenReturn(CallbackRequest.builder()
                    .caseDetailsBefore(CASE_DETAILS_BEFORE)
                    .caseDetails(CASE_DETAILS_AFTER)
                    .build());
            when(cafcassApiSearchCaseService.convertToCafcassApiCase(CASE_DETAILS_BEFORE))
                .thenReturn(CAFCASS_API_CASE_BEFORE);
            when(cafcassApiSearchCaseService.convertToCafcassApiCase(CASE_DETAILS_AFTER))
                .thenReturn(CAFCASS_API_CASE_BEFORE);

            AboutToStartOrSubmitCallbackResponse controllerResponse = AboutToStartOrSubmitCallbackResponse.builder()
                .data(new HashMap<>(CASE_MAP_AFTER))
                .build();

            AboutToStartOrSubmitCallbackResponse interceptedResponse = underTest.beforeBodyWrite(controllerResponse,
                HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE, MediaType.APPLICATION_JSON, null,
                REQUEST_WHITELISTED, RESPONSE);

            assertNotNull(interceptedResponse);
            assertEquals(CASE_MAP_AFTER, interceptedResponse.getData());
        }

        @Test
        void shouldNotUpdateTimestampIfPathExcluded() {
            AboutToStartOrSubmitCallbackResponse controllerResponse = AboutToStartOrSubmitCallbackResponse.builder()
                .data(new HashMap<>(CASE_MAP_AFTER))
                .build();

            AboutToStartOrSubmitCallbackResponse interceptedResponse = underTest.beforeBodyWrite(controllerResponse,
                HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE, MediaType.APPLICATION_JSON, null,
                REQUEST_EXCLUDED, RESPONSE);

            assertNotNull(interceptedResponse);
            assertEquals(CASE_MAP_AFTER, interceptedResponse.getData());
        }

        @Test
        void shouldNotUpdateTimestampIfCaseStateExcluded() {
            when(requestScopeStorage.getCallbackRequest())
                .thenReturn(CallbackRequest.builder()
                    .caseDetailsBefore(CASE_DETAILS_BEFORE)
                    .caseDetails(CASE_DETAILS_AFTER.toBuilder().state(State.DELETED.getValue()).build())
                    .build());

            AboutToStartOrSubmitCallbackResponse controllerResponse = AboutToStartOrSubmitCallbackResponse.builder()
                .data(new HashMap<>(CASE_MAP_AFTER))
                .build();

            AboutToStartOrSubmitCallbackResponse interceptedResponse = underTest.beforeBodyWrite(controllerResponse,
                HANDLE_ABOUT_TO_SUBMIT_RETURN_TYPE, MediaType.APPLICATION_JSON, null,
                REQUEST_WHITELISTED, RESPONSE);

            assertNotNull(interceptedResponse);
            assertEquals(CASE_MAP_AFTER, interceptedResponse.getData());
        }
    }


    @Nested
    class RequestBodyAdviceTest {
        @Test
        void shouldReturnTrueWhenMethodParameterMatch() throws NoSuchMethodException {
            assertTrue(underTest.supports(HANDLE_ABOUT_TO_SUBMIT_PARAMETER_TYPE, null,null));
        }

        @Test
        void shouldReturnFalseWhenMethodParameterNotMatch() throws NoSuchMethodException {
            assertFalse(underTest.supports(DUMMY_METHOD_RETURN_TYPE, null, null));
        }

        @Test
        void shouldSeToRequestScopeStorage() {
            final CallbackRequest callbackRequest = CallbackRequest.builder().eventId("test").build();
            underTest.afterBodyRead(callbackRequest, null, HANDLE_ABOUT_TO_SUBMIT_PARAMETER_TYPE, null, null);
            verify(requestScopeStorage).setCallbackRequest(callbackRequest);
        }

        @Test
        void shouldNotSetToRequestScopeStorageIfClassNotMatch() {
            final String callbackRequest = "test";
            underTest.afterBodyRead(callbackRequest, null, HANDLE_ABOUT_TO_SUBMIT_PARAMETER_TYPE, null, null);
            verifyNoInteractions(requestScopeStorage);
        }

        @Test
        void shouldDoNothingInBeforeBodyRead() throws IOException {
            HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
            assertEquals(httpInputMessage,
                underTest.beforeBodyRead(httpInputMessage, null, null, null));
        }

        @Test
        void shouldDoNothingInHandleEmptyBody() {
            Object body = mock(Object.class);
            HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
            assertEquals(body,
                underTest.handleEmptyBody(body, httpInputMessage, null, null, null));
        }
    }
}
