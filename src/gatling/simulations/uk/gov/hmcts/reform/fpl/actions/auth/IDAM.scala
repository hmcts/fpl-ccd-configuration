package uk.gov.hmcts.reform.fpl.actions.auth

import java.util.{Base64, UUID}

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.HeaderNames.{Authorization, ContentType}
import io.gatling.http.HeaderValues.{ApplicationFormUrlEncoded, ApplicationJson}
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fpl.config.SystemConfig

import scala.concurrent.duration._
import scala.util.Random

object IDAM {
  private val runIdentifier = Random.alphanumeric.take(6).mkString

  private val feeder =
    Iterator.continually({
      val id = UUID.randomUUID()
      val email = s"TEST_FPLA_${runIdentifier}_CASEWORKER_${Random.alphanumeric.take(6).mkString}@fpla.local"
      val password = "Pazzw0rd123"
      Map(
        "id" -> id,
        "email" -> email,
        "password" -> password,
        "encodedCredentials" -> Base64.getEncoder.encodeToString(s"$email:$password".getBytes)
      )
    })

  val registerAndSignIn: ChainBuilder =
    feed(feeder)
      .exec(
        http("Create IDAM account")
          .post(SystemConfig.idamUrl + "/testing-support/accounts")
          .header(ContentType, ApplicationJson)
          .body(StringBody(
            """
              {
                "id": "${id}",
                "email": "${email}",
                "forename": "John",
                "surname": "Smith",
                "password": "${password}",
                "roles": [{
                  "code": "caseworker"
                }, {
                  "code": "caseworker-publiclaw"
                }, {
                  "code": "caseworker-publiclaw-solicitor"
                }],
                "userGroup": {
                  "code": "caseworker"
                }
              }
            """
          ))
          .check(status.is(201))
      )
      .pause(1.second, 4.seconds)
      .exec(
        http("Get authorization code")
          .post(SystemConfig.idamUrl + "/oauth2/authorize")
          .header(ContentType, ApplicationFormUrlEncoded)
          .header(Authorization, "Basic ${encodedCredentials}")
          .formParam("response_type", "code")
          .formParam("client_id", SystemConfig.idamClientId)
          .formParam("redirect_uri", SystemConfig.idamRedirectUri)
          .check(jsonPath("$['code']").notNull.saveAs("auth_code"))
      )
      .exec(
        http("Exchange authorization code for access token")
          .post(SystemConfig.idamUrl + "/oauth2/token")
          .header(ContentType, ApplicationFormUrlEncoded)
          .formParam("grant_type", "authorization_code")
          .formParam("code", "${auth_code}")
          .formParam("client_id", SystemConfig.idamClientId)
          .formParam("client_secret", SystemConfig.idamClientSecret)
          .formParam("redirect_uri", SystemConfig.idamRedirectUri)
          .check(jsonPath("$['access_token']").notNull.saveAs("user_token"))
      )

  val deleteAccount: ChainBuilder =
    exec(
      http("Delete IDAM account")
        .delete(SystemConfig.idamUrl + "/testing-support/accounts/${email}")
    )
}
