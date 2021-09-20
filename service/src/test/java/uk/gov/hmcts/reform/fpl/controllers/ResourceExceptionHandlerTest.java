package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;
import uk.gov.hmcts.reform.fpl.logging.HeaderInformationExtractor;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith({TestLogsExtension.class})
class ResourceExceptionHandlerTest {

    private static final String MESSAGE = "message";
    private static final String USER_ID_HEADER = "user-id";
    private static final String USER_ROLES_HEADER = "user-roles";
    private final ResourceExceptionHandler underTest = new ResourceExceptionHandler(new HeaderInformationExtractor());

    @TestLogs
    private final TestLogger logs = new TestLogger(ResourceExceptionHandler.class);

    @Test
    void testEmptyHeaders() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of("Exception for caller (id='',roles=''). " + MESSAGE));

    }

    @Test
    void testValidHeaders() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(USER_ID_HEADER, "e24fb55f-4911-4705-a07e-4f0ed5f62539");
        httpServletRequest.addHeader(USER_ROLES_HEADER,
            "caseworker-publiclaw-bulkscan,payments,caseworker-publiclaw-courtadmin");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='e24fb55f-4911-4705-a07e-4f0ed5f62539',roles='caseworker-publiclaw-bulkscan,"
                + "payments,caseworker-publiclaw-courtadmin'). " + MESSAGE));

    }

    @Test
    void testSanitisedValidHeaders() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(USER_ID_HEADER, "e24fb55f-4911-4705-a07e-4f0ed5f62539");
        httpServletRequest.addHeader(USER_ROLES_HEADER,
            "caseworker-publiclaw-bulkscan,         payments,caseworker-publiclaw-courtadmin");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='e24fb55f-4911-4705-a07e-4f0ed5f62539',roles='caseworker-publiclaw-bulkscan,"
                + "payments,caseworker-publiclaw-courtadmin'). " + MESSAGE));

    }


    @Test
    void testMultipleValidHeaders() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(USER_ID_HEADER, "e24fb55f-4911-4705-a07e-4f0ed5f62539");
        httpServletRequest.addHeader(USER_ID_HEADER, "e24fb55f-4911-4705-a07e-4f0ed5f62538");
        httpServletRequest.addHeader(USER_ROLES_HEADER,
            "caseworker-publiclaw-bulkscan,payments,caseworker-publiclaw-courtadmin");
        httpServletRequest.addHeader(USER_ROLES_HEADER,
            "caseworker-publiclaw-bulkscan2");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='e24fb55f-4911-4705-a07e-4f0ed5f62539,e24fb55f-4911-4705-a07e-4f0ed5f62538',"
                + "roles='caseworker-publiclaw-bulkscan,payments,caseworker-publiclaw-courtadmin,"
                + "caseworker-publiclaw-bulkscan2'). " + MESSAGE));
    }

    @Test
    void testMalformedHeaderID() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(USER_ID_HEADER, "e24911-4705-a07e-4f0ed5f62539");
        httpServletRequest.addHeader(USER_ROLES_HEADER,
            "caseworker-publiclaw-bulkscan,payments,caseworker-publiclaw-courtadmin");

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> actual =
            underTest.handleAboutToStartOrSubmitCallbackException(
                new UnknownLocalAuthorityDomainException(MESSAGE), httpServletRequest);

        assertThat(actual).isEqualTo(ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build()));
        assertThat(logs.get()).isEqualTo(List.of(
            "Exception for caller (id='e24911-4705-a07e-4f0ed5f62539',roles='caseworker-publiclaw-bulkscan,payments,"
                + "caseworker-publiclaw-courtadmin'). " + MESSAGE));

    }

}
