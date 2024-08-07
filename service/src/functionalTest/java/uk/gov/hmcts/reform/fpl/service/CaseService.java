package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.util.Poller.poll;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {

    private final ObjectMapper objectMapper;
    private final CaseConverter caseConverter;
    private final AuthenticationService authenticationService;

    public CaseData pollCase(Long caseId, User user, Predicate<CaseData> casePredicate) {
        return poll(() -> getCase(caseId, user), casePredicate);
    }

    public boolean hasCaseAccess(User user, CaseData caseData) {
        int status = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(ContentType.JSON)
            .get("/testing-support/case/" + caseData.getId())
            .thenReturn()
            .statusCode();

        return status == SC_OK;
    }

    public CaseData createCase(User user) {
        return createCase(CaseData.builder().build(), user);
    }

    public CaseData createCase(CaseData caseData, User user) {

        CaseDetailsMap data = caseDetailsMap(caseConverter.toMap(caseData));

        CaseDetails caseDetails = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(ContentType.JSON)
            .body(Map.of())
            .post("/testing-support/case/create")
            .then()
            .statusCode(HTTP_OK)
            .extract()
            .as(CaseDetails.class);

        log.info(format("Case %s created", caseDetails.getId()));

        boolean populateData = isNotEmpty(data);

        data.putIfNotEmpty(caseDetails.getData());

        if (populateData) {
            SerenityRest
                .given()
                .contentType(ContentType.JSON)
                .headers(authenticationService.getAuthorizationHeaders(user))
                .body(Map.of("state", Optional.ofNullable(caseData.getState())
                    .map(State::getValue)
                    .orElse("Open"), "caseData", data))
                .post("/testing-support/case/populate/" + caseDetails.getId())
                .then()
                .statusCode(HTTP_OK);

            log.info(format("Case %s populated with data", caseDetails.getId()));
        }

        return caseConverter.convert(caseDetails.toBuilder().data(data).build());
    }

    public CallbackResponse callback(CaseData caseData, User user, String callback) {
        AboutToStartOrSubmitCallbackResponse response = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(ContentType.JSON)
            .body(toCallbackRequest(caseData))
            .post(callback)
            .then()
            .statusCode(HTTP_OK)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        CaseData updatedCase = objectMapper.convertValue(response.getData(), CaseData.class).toBuilder()
            .id(caseData.getId())
            .build();

        return CallbackResponse.builder()
            .caseData(updatedCase)
            .errors(response.getErrors())
            .build();
    }

    public void submittedCallback(CaseData caseData, CaseData caseDataBefore, User user, String callback) {
        SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(ContentType.JSON)
            .body(toCallbackRequest(caseData, caseDataBefore))
            .post(callback)
            .then()
            .statusCode(HTTP_OK);
    }

    private CaseData getCase(Long id, User user) {
        CaseDetails caseDetails = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(ContentType.JSON)
            .body(Map.of())
            .get("/testing-support/case/" + id)
            .then()
            .statusCode(HTTP_OK)
            .extract()
            .as(CaseDetails.class);

        return caseConverter.convert(caseDetails);
    }

    private CallbackRequest toCallbackRequest(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    private CallbackRequest toCallbackRequest(CaseData caseData) {
        return toCallbackRequest(caseData, caseData);
    }

    private CallbackRequest toCallbackRequest(CaseData caseData, CaseData caseDataBefore) {
        CaseDetails caseDetails = toCaseDetails(caseData);
        CaseDetails caseDetailsBefore = toCaseDetails(caseDataBefore);

        return toCallbackRequest(caseDetails, caseDetailsBefore);
    }

    private CaseDetails toCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(objectMapper.convertValue(caseData, MAP_TYPE))
            .build();
    }
}

