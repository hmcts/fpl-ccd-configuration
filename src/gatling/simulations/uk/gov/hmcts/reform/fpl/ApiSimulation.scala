package uk.gov.hmcts.reform.fpl

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fpl.actions.Action.action
import uk.gov.hmcts.reform.fpl.actions.auth.{IDAM, S2S}
import uk.gov.hmcts.reform.fpl.config.{SimulationParams, SystemConfig}

class ApiSimulation extends Simulation {
  private val draftApplication = scenario("Draft EPO application")
      .exec(IDAM.registerAndSignIn)
      .exec(S2S.leaseServiceToken)
      .exec(action)
      .exec(IDAM.deleteAccount)

  setUp(
    draftApplication.inject(rampUsers(SimulationParams.numberOfUsers).during(SimulationParams.rampUpTimeInSeconds))
  ).protocols(
    http.baseUrl(SystemConfig.url).contentTypeHeader("application/json")
  )
}
