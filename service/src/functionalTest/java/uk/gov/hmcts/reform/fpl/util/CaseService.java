package uk.gov.hmcts.reform.fpl.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.gargoylesoftware.htmlunit.util.MimeType.APPLICATION_JSON;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.awaitility.Awaitility.await;
import static uk.gov.hmcts.reform.fpl.model.User.user;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {

    private final ObjectMapper objectMapper;
    private final CaseConverter caseConverter;
    private final AuthenticationService authenticationService;


    public CaseData getCase(Long id, User user) {
        CaseDetails caseDetails = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(APPLICATION_JSON)
            .body(Map.of())
            .when()
            .get("/testing-support/case/" + id)
            .then()
            .statusCode(HTTP_OK)
            .and()
            .extract()
            .body()
            .as(CaseDetails.class);

        return caseConverter.convert(caseDetails);
    }


    public CaseData pollCase(Long caseId, User user, Predicate<CaseData> casePredicate) {

        return await()
            .pollDelay(1, TimeUnit.SECONDS)
            .pollInterval(10, TimeUnit.SECONDS)
            .atMost(1, TimeUnit.MINUTES)
            .until(() -> getCase(caseId, user), casePredicate);

    }

    public CaseData createCase(CaseData caseData, User user) {

        CaseDetailsMap data = CaseDetailsMap.caseDetailsMap(caseConverter.toMap(caseData));

        CaseDetails caseDetails = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(APPLICATION_JSON)
            .body(Map.of())
            .when()
            .post("/testing-support/case/create")
            .then()
            .statusCode(HTTP_OK)
            .and()
            .extract()
            .body()
            .as(CaseDetails.class);

        boolean populate = isNotEmpty(data);

        data.putIfNotEmpty(caseDetails.getData());

        System.out.println("Created case with id " + caseDetails.getId());

        if (populate) {
            SerenityRest
                .given()
                .headers(authenticationService.getAuthorizationHeaders(user("hmcts-admin@example.com")))
                .contentType(APPLICATION_JSON)
                .body(Map.of("state", Optional.ofNullable(caseData.getState())
                    .map(State::getValue)
                    .orElse("Open"), "caseData", data))
                .when()
                .post("/testing-support/case/populate/" + caseDetails.getId())
                .then()
                .statusCode(HTTP_OK);
        }

        return caseConverter.convert(caseDetails.toBuilder().data(data).build());

    }

    public CallbackResponse callback(CaseData caseData, User user, String callback) {
        AboutToStartOrSubmitCallbackResponse x = SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(APPLICATION_JSON)
            .body(toCallbackRequest(caseData))
            .when()
            .post(callback)
            .then()
            .statusCode(HTTP_OK)
            .extract()
            .body()
            .as(AboutToStartOrSubmitCallbackResponse.class);


        CaseData c;
        if (x.getData() != null) {
            c = objectMapper.convertValue(x.getData(), CaseData.class).toBuilder()
                .id(caseData.getId())
                .build();
        } else {
            c = caseData;
        }

        return CallbackResponse.builder()
            .errors(x.getErrors())
            .caseData(c)
            .data(x.getData())
            .build();
    }


    public void submitCallback(CaseData caseData, User user, String callback) {
        SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .contentType(APPLICATION_JSON)
            .body(toCallbackRequest(caseData))
            .when()
            .post(callback)
            .then()
            .statusCode(HTTP_OK);
    }

    private CallbackRequest toCallbackRequest(CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
    }

    private CallbackRequest toCallbackRequest(CaseData caseData) {
        return toCallbackRequest(caseConverter.convert(caseData));
    }

    public CaseData convert(CaseDetails caseDetails) {
        return caseConverter.convert(caseDetails);
    }

}

