package uk.gov.hmcts.reform.fpl.actions.auth

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.HeaderNames._
import io.gatling.http.HeaderValues._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fpl.config.SystemConfig
import uk.gov.hmcts.reform.fpl.utils.TOTP

object S2S {
  private val serviceConcreteNameFeeder =
    Iterator.continually(
      Map(
        "totp" -> TOTP.getPassword(SystemConfig.s2sSecret)
      )
    )

  val leaseServiceToken: ChainBuilder =
    feed(serviceConcreteNameFeeder)
      .exec(
        http("Lease service token")
          .post(SystemConfig.s2sUrl + "/lease")
          .header(ContentType, ApplicationJson)
          .header(Accept, TextPlain)
          .body(StringBody(
            """
                {
                  "microservice": "fpl_case_service",
                  "oneTimePassword": "${totp}"
                }
            """
          ))
          .check(bodyString.saveAs("service_token"))
      )
}
