package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith( {TestLogsExtension.class})
class ResourceExceptionHandlerTest {

    private static final String MESSAGE = "message";
    private static final String USER_ID_HEADER = "user-id";
    private static final String USER_ROLES_HEADER = "user-roles";
    private final ResourceExceptionHandler underTest = new ResourceExceptionHandler();

    @TestLogs
    private final TestLogger logs = new TestLogger(ResourceExceptionHandler.class);
    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

    @Test
    void testEmptyHeaders() {

        when(httpServletRequest.getHeader(USER_ID_HEADER)).thenReturn(null);
        when(httpServletRequest.getHeader(USER_ROLES_HEADER)).thenReturn(null);

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of("Exception for caller (id='', roles=''). " + MESSAGE));

    }

    @Test
    void testValidHeaders() {

        when(httpServletRequest.getHeader(USER_ID_HEADER)).thenReturn("e24fb55f-4911-4705-a07e-4f0ed5f62539");
        when(httpServletRequest.getHeader(USER_ROLES_HEADER)).thenReturn(
            "caseworker-publiclaw-bulkscan,payments,caseworker-publiclaw-courtadmin");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='e24fb55f-4911-4705-a07e-4f0ed5f62539', roles='caseworker-publiclaw-bulkscan,"
                + "payments,caseworker-publiclaw-courtadmin'). " + MESSAGE));

    }

    @Test
    void testMalformedHeaderID() {

        when(httpServletRequest.getHeader(USER_ID_HEADER)).thenReturn("e24911-4705-a07e-4f0ed5f62539");
        when(httpServletRequest.getHeader(USER_ROLES_HEADER)).thenReturn(
            "caseworker-publiclaw-bulkscan,payments,caseworker-publiclaw-courtadmin");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='', roles='caseworker-publiclaw-bulkscan,payments,"
                + "caseworker-publiclaw-courtadmin'). " + MESSAGE));

    }

    @Test
    void testMalformedForgedHeaderRoles() {

        when(httpServletRequest.getHeader(USER_ID_HEADER)).thenReturn("e24fb55f-4911-4705-a07e-4f0ed5f62539");
        when(httpServletRequest.getHeader(USER_ROLES_HEADER)).thenReturn(
            "caseworker-publiclaw-bulkscan,payments\n\n,caseworker-publiclaw-courtadmin");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='e24fb55f-4911-4705-a07e-4f0ed5f62539', roles='caseworker-publiclaw-bulkscan,"
                + "payments%0A%0A,caseworker-publiclaw-courtadmin'). " + MESSAGE));

    }

}
