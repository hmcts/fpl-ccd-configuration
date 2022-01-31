package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.restassured.http.Headers.headers;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationService {

    private static final String DEFAULT_PASSWORD = "Password12";

    private final ConcurrentMap<User, String> usersAccessTokens = new ConcurrentHashMap<>();

    private final TestConfiguration testConfiguration;
    private final ObjectMapper objectMapper;

    public Headers getAuthorizationHeaders(User user) {
        return headers(new Header("Authorization", this.getAccessToken(user)));
    }

    private String login(User user) {
        try {
            String response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfiguration.getIdamUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .formParam("password", defaultIfNull(user.getPassword(), DEFAULT_PASSWORD))
                .formParam("username", user.getName())
                .post("/loginUser")
                .then()
                .statusCode(HTTP_OK)
                .extract()
                .asString();

            return "Bearer " + objectMapper.readValue(response, TokenResponse.class).accessToken;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getAccessToken(User user) {
        return usersAccessTokens.computeIfAbsent(user, this::login);
    }
}
