package uk.gov.hmcts.reform.fpl.logging;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Component
public class HeaderInformationExtractor {

    public String getCallback(CallbackRequest callbackRequest, MethodParameter parameter) {
        String eventName = callbackRequest.getEventId();
        String callbackType = Optional.ofNullable(parameter.getMethod())
            .map(method -> method.getAnnotation(PostMapping.class))
            .map(PostMapping::value)
            .map(path -> String.join("", path))
            .orElse("");

        return String.format("event='%s',type='%s'", eventName, callbackType);
    }

    public String getUser(HttpHeaders httpHeaders) {
        String userIds = String.join(",", httpHeaders.getOrEmpty("user-id"));
        String userRoles = httpHeaders.getOrEmpty("user-roles").stream()
            .flatMap(roles -> Stream.of(roles.split(",")))
            .map(String::trim)
            .filter(role -> !role.equals("caseworker") && !role.equals("caseworker-publiclaw"))
            .collect(joining(","));

        return String.format("id='%s',roles='%s'", userIds, userRoles);
    }

    public String getCase(CallbackRequest callbackRequest) {
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return String.format("id='%s' state='%s' la='%s' case number='%s'",
            caseDetails.getId(),
            caseDetails.getState(),
            caseDetails.getData().getOrDefault("caseLocalAuthority", ""),
            caseDetails.getData().getOrDefault("familyManCaseNumber", "")
        );
    }

}
