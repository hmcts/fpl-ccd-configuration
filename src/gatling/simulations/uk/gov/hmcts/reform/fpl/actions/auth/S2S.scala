package uk.gov.hmcts.reform.fpl.actions.auth

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.HeaderNames._
import io.gatling.http.HeaderValues._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fpl.config.SystemConfig
import uk.gov.hmcts.reform.fpl.utils.TOTP

object S2S {
  val leaseServiceToken: ChainBuilder =
    exec(
      http("Lease service token")
        .post(SystemConfig.s2sUrl + "/lease")
        .header(ContentType, ApplicationJson)
        .header(Accept, TextPlain)
        .body(StringBody(
          s"""
                {
                  "microservice": "fpl_case_service",
                  "oneTimePassword": "${TOTP.getPassword(SystemConfig.s2sSecret)}"
                }
            """
        ))
        .check(bodyString.notNull.saveAs("service_token"))
    )
}
